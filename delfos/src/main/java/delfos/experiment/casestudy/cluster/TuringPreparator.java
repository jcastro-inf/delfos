package delfos.experiment.casestudy.cluster;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import delfos.common.FileUtilities;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.experiment.casestudy.CaseStudy;
import delfos.experiment.casestudy.defaultcase.DefaultCaseStudy;
import delfos.group.casestudy.GroupCaseStudy;
import delfos.group.casestudy.defaultcase.DefaultGroupCaseStudy;
import delfos.group.io.xml.casestudy.GroupCaseStudyXML;
import delfos.io.xml.casestudy.CaseStudyXML;
import delfos.main.Main;

/**
 *
 * @version 19-jun-2014
 * @author Jorge Castro Gallardo
 */
public class TuringPreparator implements ExperimentPreparator {

    @Override
    public void prepareExperiment(File experimentBaseFolder, List<CaseStudy> caseStudies, DatasetLoader<? extends Rating> datasetLoader) {

        int i = 0;
        for (CaseStudy caseStudy : caseStudies) {
            String fileName = caseStudy.getRecommenderSystem().getAlias() + ".xml";

            DecimalFormat format = new DecimalFormat("000");

            String experimentNumber = format.format(i++);
            String thisIterationFolder = caseStudy.getAlias();

            //Clean folder
            File finalFolderRS = new File(experimentBaseFolder + File.separator + thisIterationFolder);
            File finalFolderDataset = new File(finalFolderRS + File.separator + "dataset");
            FileUtilities.deleteDirectoryRecursive(finalFolderDataset);
            finalFolderDataset.mkdirs();

            File rsConfigFile = new File(finalFolderRS + File.separator + fileName);
            CaseStudyXML.saveCaseDescription(caseStudy, rsConfigFile.getAbsolutePath());

            //generateDatasetFile
            {
                CaseStudy datasetLoaderCaseStudy = new DefaultCaseStudy(
                        datasetLoader
                );

                File datasetConfigFile = new File(finalFolderDataset + File.separator + datasetLoaderCaseStudy.getAlias() + ".xml");
                CaseStudyXML.saveCaseDescription(datasetLoaderCaseStudy, datasetConfigFile.getAbsolutePath());
            }
        }
    }

    @Override
    public void prepareGroupExperiment(
            File experimentBaseFolder,
            List<GroupCaseStudy> groupCaseStudies,
            DatasetLoader<? extends Rating>... datasetLoaders) {

        int i = 0;

        for (DatasetLoader<? extends Rating> datasetLoader : datasetLoaders) {
            for (GroupCaseStudy groupCaseStudy : groupCaseStudies) {
                String fileName = "[" + datasetLoader.getAlias() + "]" + "_" + groupCaseStudy.getAlias() + ".xml";

                DecimalFormat format = new DecimalFormat("000");

                String experimentNumber = format.format(i++);
//                String thisIterationFolder = "experiment_" + experimentNumber;
                String thisIterationFolder = "[" + datasetLoader.getAlias() + "]" + "_" + groupCaseStudy.getAlias();

                //Clean folder
                File finalFolderRS = new File(experimentBaseFolder + File.separator + thisIterationFolder);
                File finalFolderDataset = new File(finalFolderRS + File.separator + "dataset");
                FileUtilities.deleteDirectoryRecursive(finalFolderDataset);
                finalFolderDataset.mkdirs();

                File rsConfigFile = new File(finalFolderRS + File.separator + fileName);
                GroupCaseStudyXML.saveCaseDescription(groupCaseStudy, rsConfigFile.getAbsolutePath());

                //generateDatasetFile
                {
                    GroupCaseStudy datasetLoaderCaseStudy = new DefaultGroupCaseStudy(
                            datasetLoader
                    );

                    File datasetConfigFile = new File(finalFolderDataset + File.separator + datasetLoaderCaseStudy.getAlias() + ".xml");
                    GroupCaseStudyXML.saveCaseDescription(datasetLoaderCaseStudy, datasetConfigFile.getAbsolutePath());
                }
            }
        }
    }

    public void executeAllExperimentsInDirectory(File directory) {
        Arrays.asList(directory.listFiles())
                .parallelStream()
                .forEach((singleExperimentDirectory) -> {
                    String[] args = {
                        "-seed", "77352653",
                        "-executeGroupXML", singleExperimentDirectory.getPath(),
                        "-numExec", "1"};
                    Main.mainWithExceptions(args);
                });

    }

    public void executeAllExperimentsInDirectory(File directory, int numExec) {
        Arrays.asList(directory.listFiles())
                .parallelStream()
                .forEach((singleExperimentDirectory) -> {
                    String[] args = {
                        "-seed", "77352653",
                        "-executeGroupXML", singleExperimentDirectory.getPath(),
                        "-numExec", Integer.toString(numExec)
                    };

                    Main.mainWithExceptions(args);

                    System.out.println("==============================");
                });
    }

    public void executeAllExperimentsInDirectory(File directory, int numExec, int maxCPU) {
        Arrays.asList(directory.listFiles())
                .stream()
                .forEach((singleExperimentDirectory) -> {
                    String[] args = {
                        "-seed", "77352653",
                        "-executeGroupXML", singleExperimentDirectory.getPath(),
                        "-numExec", Integer.toString(numExec),
                        "-maxCPU", Integer.toString(maxCPU)};

                    Main.mainWithExceptions(args);

                    System.out.println("==============================");
                });
    }
}
