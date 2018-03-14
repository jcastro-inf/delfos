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
package delfos.casestudy.fromxmlfiles;

import delfos.ERROR_CODES;
import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.filefilters.FileFilterByExtension;
import delfos.experiment.Experiment;
import delfos.experiment.SeedHolder;
import delfos.experiment.casestudy.CaseStudy;
import delfos.io.excel.casestudy.CaseStudyExcel;
import delfos.io.xml.experiment.ExperimentXML;
import delfos.utils.algorithm.progress.ProgressChangedController;
import delfos.utils.algorithm.progress.ProgressChangedListenerDefault;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import jxl.write.WriteException;
import org.jdom2.JDOMException;

/**
 * Ejecuta los experimentos que hay definidos en el directorio indicado, leyendo los XML que existen para generar los
 * casos de estudio.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 14-May-2013
 */
public class XMLexperimentsExecution {

    private final Optional<Long> seed;
    private final String experimentPath;
    private final Optional<Integer> numExecutions;

    public static final String AGGREGATE_RESULTS_EXCEL_DEFAULT_FILE_NAME = CaseStudyExcel.AGGREGATE_RESULTS_EXCEL_DEFAULT_FILE_NAME;

    public XMLexperimentsExecution(String experimentPath, Optional<Integer> numExecutions, Optional<Long> seed) {
        this.experimentPath = experimentPath;

        if (!new File(this.experimentPath).exists()) {
            throw new IllegalArgumentException("The file '" + this.experimentPath + "' do not exists [" + new File(this.experimentPath).getAbsolutePath() + "]");
        }

        this.numExecutions = numExecutions;
        this.seed = seed;
    }

    File getBaseDirectory(File file){
        String absolutePath = file.getAbsolutePath();

        if(absolutePath.contains("descriptions")){
            return getBaseDirectory(file.getParentFile());
        } else {
            return file;
        }
    }

    public void execute() throws CannotLoadContentDataset, CannotLoadRatingsDataset {

        Global.showMessageTimestamped("Execute XMLExperiment in path provided as '" + experimentPath + "'");

        List<Experiment> allTasks = new ArrayList<>();

        File experimentBaseDirectory = getBaseDirectory(new File(experimentPath));
        Global.showWarning("Experiment base directory is '" + experimentBaseDirectory.getAbsolutePath() + "'");

        File experimentXMLDescriptionsDir = new File(experimentBaseDirectory + File.separator + "descriptions");
        File resultsDirectory = new File(experimentBaseDirectory + File.separator + "results");
        File aggregateResultsDirectory = new File(experimentBaseDirectory + File.separator + "aggregate");


        if(new File(experimentPath).isDirectory()){
            try {
                File[] experimentFiles = experimentXMLDescriptionsDir.
                        listFiles(new FileFilterByExtension(false, "xml"));

                if (experimentFiles.length == 0) {
                    throw new IllegalStateException("No experiments files in '" + experimentXMLDescriptionsDir.getAbsolutePath() + "'");
                }
                Arrays.sort(experimentFiles, Comparator.comparing(File::getName));
                for (File experimentFile : experimentFiles) {
                    Experiment experiment = ExperimentXML.loadExperiment(experimentFile);
                    //experiment.setResultsDirectory(resultsDirectory);
                    allTasks.add(experiment);
                }
            } catch (JDOMException | IOException ex) {
                ERROR_CODES.CANNOT_READ_CASE_STUDY_XML.exit(ex);
            }
        }else{
            try {
                Experiment experiment = ExperimentXML.loadExperiment(new File(experimentPath));
                allTasks=Arrays.asList(experiment);
            } catch (JDOMException | IOException ex) {
                ERROR_CODES.CANNOT_READ_CASE_STUDY_XML.exit(ex);
            }
        }


        allTasks.forEach(experiment -> {
            if(experiment.haveParameter(CaseStudy.NUM_EXECUTIONS)){
                if(numExecutions.isPresent()) {
                    experiment.setParameterValue(CaseStudy.NUM_EXECUTIONS, numExecutions.get());
                }
            }else{
                Global.showWarning("Experiment '"+experiment.getAlias()+"' does not have parameter "+CaseStudy.NUM_EXECUTIONS );
            }

            if(experiment.haveParameter(SeedHolder.SEED)){
                if(seed.isPresent()) {
                    experiment.setParameterValue(SeedHolder.SEED, seed.get());
                }
            }else{
                Global.showWarning("Experiment '"+experiment.getAlias()+"' does not have parameter "+SeedHolder.SEED);
            }
        });

        Experiment_SingleTaskExecute caseStudy_SingleTaskExecute = new Experiment_SingleTaskExecute();

        ProgressChangedController progressChangedController = new ProgressChangedController(
                "Case studies executor",
                allTasks.size(),
                new ProgressChangedListenerDefault(System.out, 10000)
        );

        Stream<Experiment> allTasksStream = Global.isIsParallelExecutionOfExperiments() ?
                allTasks.parallelStream() :
                allTasks.stream();

        allTasksStream.forEach(task -> {
            caseStudy_SingleTaskExecute.accept(task);
            progressChangedController.setTaskFinished();
        });


        boolean isCaseStudy = allTasks.stream().anyMatch(experiment -> experiment instanceof CaseStudy);

        if(isCaseStudy) {
            File aggregateFile = FileUtilities.addSufix(aggregateResultsDirectory, File.separator + AGGREGATE_RESULTS_EXCEL_DEFAULT_FILE_NAME);
            try {
                CaseStudyExcel.aggregateExcels(
                        getMostRelevantExcels(resultsDirectory),
                        aggregateFile);
            } catch (WriteException ex) {
                ERROR_CODES.CANNOT_WRITE_CASE_STUDY_XML.exit(ex);
            }
        }
    }

    private File[] getMostRelevantExcels(File resultsDirectory) {
        return resultsDirectory.listFiles(new FileFilterByExtension(false, "xls"));
    }
}
