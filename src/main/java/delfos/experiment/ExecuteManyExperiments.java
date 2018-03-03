/*
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package delfos.experiment;

import delfos.Constants;
import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.configfile.rs.single.RecommenderSystemConfiguration;
import delfos.configfile.rs.single.RecommenderSystemConfigurationFileParser;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.experiment.casestudy.CaseStudyConfiguration;
import delfos.experiment.casestudy.CaseStudy;
import delfos.io.xml.casestudy.CaseStudyXML;
import delfos.rs.RecommenderSystem;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Ejecuta todos los XML de caso de estudio que hay en el directorio indicado.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 15-Noviembre-2013
 */
public class ExecuteManyExperiments {

    private static DatasetLoader[] datasets;
    /**
     * ============== CONSTANTS ====================
     */
    private final long SEED;
    private final String inputDirectory;
    private final int numExecutions;
    private final String outputDirectory;

    public ExecuteManyExperiments(
            String directory, long SEED, int numExecutions) {

        this.SEED = SEED;
        this.inputDirectory = directory;
        this.numExecutions = numExecutions;
        this.outputDirectory = "experiments-traditional";

        if (numExecutions <= 0) {
            throw new IllegalArgumentException("Cannot execute " + numExecutions + " times, should be a positive number");
        }
        if (!new File(directory).isDirectory()) {
            throw new IllegalArgumentException("'" + directory + "' is not a directory.");
        }

        init();
    }

    private static ArrayList<CaseStudyConfiguration> getExperiments(File directory) {
        ArrayList<CaseStudyConfiguration> caseStudyConfigurationsList = new ArrayList<>();

        if (!directory.exists()) {
            throw new IllegalArgumentException("The file " + directory.getAbsolutePath() + " does not exists.");
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("The file " + directory.getAbsolutePath() + " is not a directory.");
        }
        File[] listOfFiles = directory.listFiles(
                (File pathname) -> pathname.getName().endsWith(".xml"));

        Collections.sort(Arrays.asList(listOfFiles));

        for (File configurationFile : listOfFiles) {
            try {
                CaseStudyConfiguration caseStudyConfiguration = CaseStudyXML.loadCase(configurationFile);

                caseStudyConfiguration.getRecommenderSystem().setAlias(configurationFile.getName());

                if (caseStudyConfiguration.getRecommenderSystem() instanceof RecommenderSystem) {
                    caseStudyConfigurationsList.add(caseStudyConfiguration);
                } else {
                    throw new IllegalArgumentException("The recommender in " + configurationFile.getAbsolutePath() + " is not a traditional recommender system.");
                }
            } catch (Throwable ex) {
                ex.printStackTrace(System.err);
            }

        }
        return caseStudyConfigurationsList;
    }

    private static ArrayList<DatasetLoader<? extends Rating>> getDatasets(File directory) {
        ArrayList<DatasetLoader<? extends Rating>> ret = new ArrayList<>();
        if (!directory.exists()) {
            throw new IllegalArgumentException("The file " + directory.getAbsolutePath() + " does not exists.");
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("The file " + directory.getAbsolutePath() + " is not a directory.");
        }
        File[] listOfFiles = directory.listFiles(
                (File pathname) -> pathname.getName().endsWith(".xml"));

        for (File configurationFile : listOfFiles) {
            try {
                RecommenderSystemConfiguration loadConfigFile = RecommenderSystemConfigurationFileParser.loadConfigFile(configurationFile.getAbsolutePath());
                DatasetLoader<? extends Rating> datasetLoader = loadConfigFile.datasetLoader;
                ret.add(datasetLoader);
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
                throw new IllegalArgumentException(ex);
            }

        }
        return ret;
    }

    private void init() {
        File datasetsDirectory = new File(inputDirectory + "dataset" + File.separator + "dumbFile.txt").getParentFile();
        datasets = getDatasets(datasetsDirectory).toArray(new DatasetLoader[0]);
    }

    private void execute() throws CannotLoadRatingsDataset, CannotLoadContentDataset, UserNotFound, ItemNotFound {

        Global.showInfoMessage("SEED OF THIS EXPERIMENT = " + SEED + "\n");

        ArrayList<CaseStudyConfiguration> caseStudyConfigurations;
        if (inputDirectory != null) {
            caseStudyConfigurations = getExperiments(new File(inputDirectory));
        } else {
            caseStudyConfigurations = getExperiments(new File(Constants.getTempDirectory().getAbsolutePath() + File.separator + "experiments" + File.separator));
        }

        int i = 1;
        for (CaseStudyConfiguration caseStudyConfiguration : caseStudyConfigurations) {
            if (caseStudyConfiguration.getRecommenderSystem() instanceof RecommenderSystem) {
                RecommenderSystem<Object> recommenderSystem = (RecommenderSystem<Object>) caseStudyConfiguration.getRecommenderSystem();
                for (DatasetLoader<? extends Rating> datasetLoader : datasets) {

                    CaseStudy caseStudy = CaseStudy.create(
                            recommenderSystem,
                            datasetLoader,
                            caseStudyConfiguration.getValidationTechnique(),
                            caseStudyConfiguration.getPredictionProtocol(), datasetLoader.getDefaultRelevanceCriteria(), caseStudyConfiguration.getEvaluationMeasures(), numExecutions);

                    caseStudy.addExperimentListener(new ExperimentListener_default(System.out, 10000));

                    String defaultFileName = CaseStudyXML.getDefaultFileName(caseStudy);
                    File file = FileUtilities.addPrefix(new File(defaultFileName), caseStudy.getRecommenderSystem().getAlias() + " -- ");
                    CaseStudyXML.saveCaseDescription(caseStudy, file.getAbsolutePath() + ".tmp");
                    caseStudy.execute();
                    CaseStudyXML.saveCaseResults(caseStudy, file);

                    Global.showInfoMessage("================ FIN Sistema " + i + " de " + caseStudyConfigurations.size() + "=================== \n");
                    i++;
                }
            }
        }
    }

    public void run() {
        try {
            execute();
        } catch (Throwable ex) {
            ex.printStackTrace(System.err);
            throw new IllegalArgumentException(ex);
        }
    }
}
