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
package delfos.experiment.casestudy.cluster;

import delfos.Constants;
import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.common.parameters.ParameterOwner;
import delfos.common.parameters.chain.CaseStudyResultMatrix;
import delfos.common.parameters.chain.ParameterChain;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.experiment.Experiment;
import delfos.experiment.casestudy.CaseStudy;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.io.xml.casestudy.GroupCaseStudyXML;
import delfos.io.xml.casestudy.CaseStudyXML;
import delfos.io.xml.experiment.ExperimentXML;
import delfos.main.Main;
import delfos.main.managers.experiment.ExecuteGroupXML;
import delfos.main.managers.experiment.ExecuteXML;
import delfos.rs.nonpersonalised.randomrecommender.RandomRecommendationModel;
import delfos.rs.nonpersonalised.randomrecommender.RandomRecommender;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class to create and execute experiments with a fixed directory structure that allows the latter execution in other
 * machines.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class TuringPreparator implements ExperimentPreparator {

    private final boolean parallel;

    public TuringPreparator() {
        parallel = false;
    }

    public TuringPreparator(boolean parallel) {
        this.parallel = parallel;
    }

    public Random getRandomToShuffleExperiments() {

        String runtimeXMLBeanName = ManagementFactory.getRuntimeMXBean().getName();

        String hostName = runtimeXMLBeanName.split("@")[1];

        int seed = hostName.hashCode();

        Random random = new Random(seed);

        return random;
    }

    @Override
    public <ExperimentType extends Experiment> void prepareExperimentGeneral(
            List<ExperimentType> experiments, File experimentBaseDirectory
    ) {
        Stream<ExperimentType> experimentsStream =
                parallel ?
                experiments.parallelStream():
                experiments.stream();

        experimentsStream.forEach(experiment -> {
            String aliasForDirName = experiment.getAlias().
                    replace("(", "").
                    replace(")", "").
                    replace(",", ".");

            String fileName = aliasForDirName + ".xml";

            String datasetLoaderAlias = ((DatasetLoader)experiment.getParameterValue(CaseStudy.DATASET_LOADER)).getAlias();

            File experimentDescriptionDirectory = new File(experimentBaseDirectory +
                    File.separator + datasetLoaderAlias +
                    File.separator + "descriptions");

            File experimentResultsDirectory = new File(experimentBaseDirectory +
                    File.separator + datasetLoaderAlias +
                    File.separator + "results");

            if(experimentBaseDirectory.exists()){
                Global.showWarning("Directory '"+experimentDescriptionDirectory+"' already exists.");
            }

            Experiment experimentWithResultsDirectorySet = (Experiment) experiment.clone();
            experimentWithResultsDirectorySet.setResultsDirectory(experimentResultsDirectory);

            File experimentFile = new File(experimentDescriptionDirectory + File.separator + fileName);

            ExperimentXML.saveExperiment(experimentWithResultsDirectorySet, experimentFile);
        });
    }

    public List<File> getXMLsFromDirectory(File directory){
        File descriptionsDirectory = new File(directory.getPath() + File.separator + "descriptions");
        if(!directory.isDirectory()){
            return Arrays.asList(directory);
        } else if(descriptionsDirectory.exists()){
            List<File> xmls = FileUtilities.findInDirectory(descriptionsDirectory);
            return xmls;
        }else {
            return listFiles(directory);
        }
    }

    @Override
    public void executeExperimentsGeneral(File directory) {
        List<File> experimentsToBeExecuted = getXMLsFromDirectory(directory);

        Collections.shuffle(experimentsToBeExecuted, getRandomToShuffleExperiments());

        Stream<File> experimentsToBeExecutedStream
                = parallel
                ? experimentsToBeExecuted.parallelStream()
                : experimentsToBeExecuted.stream();

        experimentsToBeExecutedStream.forEach((path) -> {
            if(path.isDirectory()){
                String[] args = {
                        ExecuteXML.MODE_PARAMETER,
                        ExecuteXML.XML_DIRECTORY, path.getPath(),
                        Constants.PRINT_FULL_XML,
                        Constants.RAW_DATA};
                try {
                    Main.mainWithExceptions(args);
                } catch (Exception ex) {
                    Global.showWarning("Experiment failed in directory '" + path.getAbsolutePath());
                    Global.showError(ex);
                }
            }else{
                String[] args = {
                        ExecuteXML.MODE_PARAMETER,
                        ExecuteXML.XML_FILE, path.getPath(),
                        Constants.PRINT_FULL_XML,
                        Constants.RAW_DATA};
                try {
                    Main.mainWithExceptions(args);
                } catch (Exception ex) {
                    Global.showWarning("Experiment failed in directory '" + path.getAbsolutePath());
                    Global.showError(ex);
                }
            }
        });
    }

    @Override
    public void prepareExperiment(File experimentBaseDirectory, List<CaseStudy> caseStudies, DatasetLoader<? extends Rating> datasetLoader) {

        int i = 0;
        for (CaseStudy caseStudy : caseStudies) {
            String fileName = caseStudy.getRecommenderSystem().getAlias() + ".xml";

            String thisIterationDirectory = caseStudy.getAlias();

            thisIterationDirectory = thisIterationDirectory.replace("(", "");
            thisIterationDirectory = thisIterationDirectory.replace(")", "");
            thisIterationDirectory = thisIterationDirectory.replace(",", ".");

            //Clean directory
            File finalDirectoryRS = new File(experimentBaseDirectory + File.separator + thisIterationDirectory);
            File finalDirectoryDataset = new File(finalDirectoryRS + File.separator + "dataset");
            FileUtilities.deleteDirectoryRecursive(finalDirectoryDataset);
            finalDirectoryDataset.mkdirs();

            File rsConfigFile = new File(finalDirectoryRS + File.separator + fileName);
            CaseStudyXML.saveCaseDescription(caseStudy, rsConfigFile.getAbsolutePath());

            //generateDatasetFile
            {
                CaseStudy<RandomRecommendationModel<Long>, ? extends Rating> datasetLoaderCaseStudy = CaseStudy.create(
                        new RandomRecommender(),
                        datasetLoader
                );

                File datasetConfigFile = new File(finalDirectoryDataset + File.separator + datasetLoaderCaseStudy.getAlias() + ".xml");
                CaseStudyXML.saveCaseDescription(datasetLoaderCaseStudy, datasetConfigFile.getAbsolutePath());
            }
        }
    }

    @Override
    public void prepareGroupExperiment(
            File experimentBaseDirectory, List<GroupCaseStudy> groupCaseStudies, DatasetLoader<? extends Rating>... datasetLoaders) {

        int i = 0;

        for (DatasetLoader<? extends Rating> datasetLoader : datasetLoaders) {
            for (GroupCaseStudy groupCaseStudy : groupCaseStudies) {

                String experimentName
                        = "[" + datasetLoader.getAlias() + "]_"
                        + groupCaseStudy.getAlias();

                experimentName = experimentName.replace("(", "");
                experimentName = experimentName.replace(")", "");
                experimentName = experimentName.replace(",", ".");

                //Clean directory
                File finalDirectoryRS = new File(experimentBaseDirectory.getAbsolutePath() + File.separator + experimentName);
                File finalDirectoryDataset = new File(finalDirectoryRS.getAbsolutePath() + File.separator + "dataset");
                FileUtilities.deleteDirectoryRecursive(finalDirectoryRS);

                File experimentConfigurationFile = new File(finalDirectoryRS + File.separator + experimentName + ".xml");
                File datasetConfiguration = new File(finalDirectoryDataset.getAbsolutePath() + File.separator + datasetLoader.getAlias() + ".xml");

                GroupCaseStudyXML.caseStudyToXMLFile_onlyDescription(groupCaseStudy, experimentConfigurationFile);

                GroupCaseStudy groupCaseStudyWithDataset = new GroupCaseStudy(datasetLoader);
                groupCaseStudyWithDataset.setSeedValue(groupCaseStudy.getSeedValue());
                GroupCaseStudyXML.caseStudyToXMLFile_onlyDescription(groupCaseStudyWithDataset, datasetConfiguration);

            }
        }
    }

    public void executeAllGroupExperimentsInDirectory(File directory) {
        List<File> experimentsToBeExecuted = listFiles(directory);

        Collections.shuffle(experimentsToBeExecuted, getRandomToShuffleExperiments());

        Stream<File> experimentsToBeExecutedStream
                = parallel
                        ? experimentsToBeExecuted.parallelStream()
                        : experimentsToBeExecuted.stream();

        experimentsToBeExecutedStream.forEach((singleExperimentDirectory) -> {
            String[] args = {
                ExecuteGroupXML.MODE_PARAMETER,
                ExecuteGroupXML.XML_DIRECTORY, singleExperimentDirectory.getPath(),
                Constants.PRINT_FULL_XML,
                Constants.RAW_DATA};
            try {
                Main.mainWithExceptions(args);
            } catch (Exception ex) {
                Global.showWarning("Experiment failed in directory '" + singleExperimentDirectory.getAbsolutePath());
                Global.showError(ex);
            }
        });
    }

    public void executeAllGroupExperimentsInDirectory(File directory, int numExec) {
        List<File> experimentsToBeExecuted = listFiles(directory);

        Collections.shuffle(experimentsToBeExecuted, getRandomToShuffleExperiments());

        Stream<File> experimentsToBeExecutedStream
                = parallel
                        ? experimentsToBeExecuted.parallelStream()
                        : experimentsToBeExecuted.stream();

        experimentsToBeExecutedStream.forEach((singleExperimentDirectory) -> {
            String[] args = {
                ExecuteGroupXML.MODE_PARAMETER,
                ExecuteGroupXML.SEED_PARAMETER, "123456",
                ExecuteGroupXML.XML_DIRECTORY, singleExperimentDirectory.getPath(),
                ExecuteGroupXML.NUM_EXEC_PARAMETER, Integer.toString(numExec),
                Constants.PRINT_FULL_XML,
                Constants.RAW_DATA
            };
            try {
                Main.mainWithExceptions(args);
            } catch (Exception ex) {
                Global.showWarning("Experiment failed in directory '" + singleExperimentDirectory.getAbsolutePath());
                Global.showError(ex);
            }

            Global.show("==============================\n");
        });
    }

    public int sizeOfAllExperimentsInDirectory(File directory) {
        return listFiles(directory).size();
    }

    public void executeAllExperimentsInDirectory_withSeed(File directory, int numExec, int seedValue) {
        List<File> experimentsToBeExecuted = listFiles(directory);

        Collections.shuffle(experimentsToBeExecuted, getRandomToShuffleExperiments());

        Stream<File> stream = parallel ? experimentsToBeExecuted.parallelStream() : experimentsToBeExecuted.stream();
        stream.forEach((singleExperimentDirectory) -> {
            String[] args = {
                ExecuteXML.SEED_PARAMETER, Integer.toString(seedValue),
                ExecuteXML.MODE_PARAMETER,
                ExecuteXML.XML_DIRECTORY, singleExperimentDirectory.getPath(),
                ExecuteXML.NUM_EXEC_PARAMETER, Integer.toString(numExec),
                Constants.PRINT_FULL_XML,
                Constants.RAW_DATA};

            try {
                Main.mainWithExceptions(args);
            } catch (Exception ex) {
                Global.showWarning("Experiment failed in directory '" + singleExperimentDirectory.getAbsolutePath());
                Global.showError(ex);
            }
            Global.show("==============================\n");
        });
    }

    /**
     * Renames the case studys to a default alias with the hash of the technique and validation and the alias of the
     * GRS.
     *
     * @param groupCaseStudys
     */
    public void renameGroupCaseStudiesWithDefaultAlias(List<GroupCaseStudy> groupCaseStudys) {
        groupCaseStudys.stream().forEach(groupCaseStudy -> groupCaseStudy.setAlias(
                "_dataValidation=" + groupCaseStudy.hashDataValidation()
                + "_technique=" + groupCaseStudy.hashTechnique()
                + "_" + groupCaseStudy.getGroupRecommenderSystem().getAlias()
                + "_allHash=" + groupCaseStudy.hashCode()
        ));
    }

    public void renameCaseStudyWithTheMinimumDistinctAlias(List<GroupCaseStudy> groupCaseStudys) {

        List<ParameterChain> dataValidationChains = ParameterChain.obtainDifferentChains(groupCaseStudys)
                .stream()
                .filter(chain -> !chain.isAlias())
                .filter(chain -> chain.isDataValidationParameter())
                .collect(Collectors.toList());

        List<ParameterChain> techniqueChains = ParameterChain.obtainDifferentChains(groupCaseStudys)
                .stream()
                .filter(chain -> !chain.isAlias())
                .filter(chain -> chain.isTechniqueParameter())
                .collect(Collectors.toList());

        if (techniqueChains.isEmpty()) {
            ParameterChain grsAliasChain = new ParameterChain(groupCaseStudys.get(0))
                    .createWithNode(GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM, null)
                    .createWithLeaf(ParameterOwner.ALIAS, null);
            techniqueChains.add(grsAliasChain);
        }
        if (dataValidationChains.isEmpty()) {
            ParameterChain datasetLoaderAliasChain = new ParameterChain(groupCaseStudys.get(0))
                    .createWithNode(GroupCaseStudy.DATASET_LOADER, null)
                    .createWithLeaf(ParameterOwner.ALIAS, null);

            ParameterChain groupFormationTechniqueAliasChain = new ParameterChain(groupCaseStudys.get(0))
                    .createWithNode(GroupCaseStudy.GROUP_FORMATION_TECHNIQUE, null)
                    .createWithLeaf(ParameterOwner.ALIAS, null);

            dataValidationChains.add(datasetLoaderAliasChain);
            dataValidationChains.add(groupFormationTechniqueAliasChain);
        }

        CaseStudyResultMatrix caseStudyResultMatrix = new CaseStudyResultMatrix(techniqueChains, dataValidationChains, "null");

        for (GroupCaseStudy groupCaseStudy : groupCaseStudys) {
            String dataValidationAlias = caseStudyResultMatrix.getColumn(groupCaseStudy);
            String techniqueAlias = caseStudyResultMatrix.getRow(groupCaseStudy);

            String newAlias = "dataValidation_" + dataValidationAlias + "__" + "technique_" + techniqueAlias;
            groupCaseStudy.setAlias(newAlias);
        }
    }

    public void executeAllIndividualExperimentsInDirectory_withSeed(File directory, int numExec, int seedValue) {
        List<File> experimentsToBeExecuted = listFiles(directory);

        Collections.shuffle(experimentsToBeExecuted, getRandomToShuffleExperiments());

        Stream<File> stream = parallel ? experimentsToBeExecuted.parallelStream() : experimentsToBeExecuted.stream();
        stream.forEach((singleExperimentDirectory) -> {
            String[] args = {
                ExecuteXML.SEED_PARAMETER, Integer.toString(seedValue),
                ExecuteXML.MODE_PARAMETER,
                ExecuteXML.XML_DIRECTORY, singleExperimentDirectory.getPath(),
                ExecuteXML.NUM_EXEC_PARAMETER, Integer.toString(numExec),
                Constants.PRINT_FULL_XML,
                Constants.RAW_DATA};

            try {
                Main.mainWithExceptions(args);
            } catch (Exception ex) {
                Global.showWarning("Experiment failed in directory '" + singleExperimentDirectory.getAbsolutePath());
                Global.showError(ex);
            }
            Global.show("==============================\n");
        });
    }

    private List<File> listFiles(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            return Arrays.asList(directory.listFiles());
        } else if (!directory.exists()) {
            return Collections.EMPTY_LIST;
        } else {
            throw new IllegalArgumentException("'" + directory.getAbsolutePath() + "' is not a directory");
        }
    }
}
