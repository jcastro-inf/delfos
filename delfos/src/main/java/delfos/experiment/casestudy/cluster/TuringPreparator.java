package delfos.experiment.casestudy.cluster;

import delfos.Constants;
import delfos.common.FileUtilities;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.experiment.casestudy.CaseStudy;
import delfos.experiment.casestudy.defaultcase.DefaultCaseStudy;
import delfos.group.casestudy.GroupCaseStudy;
import delfos.group.casestudy.defaultcase.DefaultGroupCaseStudy;
import delfos.group.io.xml.casestudy.GroupCaseStudyXML;
import delfos.io.xml.casestudy.CaseStudyXML;
import delfos.main.Main;
import delfos.main.managers.experiment.ExecuteGroupXML;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @version 19-jun-2014
 * @author Jorge Castro Gallardo
 */
public class TuringPreparator implements ExperimentPreparator {

    @Override
    public void prepareExperiment(File experimentBaseDirectory, List<CaseStudy> caseStudies, DatasetLoader<? extends Rating> datasetLoader) {

        int i = 0;
        for (CaseStudy caseStudy : caseStudies) {
            String fileName = caseStudy.getRecommenderSystem().getAlias() + ".xml";

            DecimalFormat format = new DecimalFormat("000");

            String experimentNumber = format.format(i++);
            String thisIterationDirectory = caseStudy.getAlias();

            //Clean directory
            File finalDirectoryRS = new File(experimentBaseDirectory + File.separator + thisIterationDirectory);
            File finalDirectoryDataset = new File(finalDirectoryRS + File.separator + "dataset");
            FileUtilities.deleteDirectoryRecursive(finalDirectoryDataset);
            finalDirectoryDataset.mkdirs();

            File rsConfigFile = new File(finalDirectoryRS + File.separator + fileName);
            CaseStudyXML.saveCaseDescription(caseStudy, rsConfigFile.getAbsolutePath());

            //generateDatasetFile
            {
                CaseStudy datasetLoaderCaseStudy = new DefaultCaseStudy(
                        datasetLoader
                );

                File datasetConfigFile = new File(finalDirectoryDataset + File.separator + datasetLoaderCaseStudy.getAlias() + ".xml");
                CaseStudyXML.saveCaseDescription(datasetLoaderCaseStudy, datasetConfigFile.getAbsolutePath());
            }
        }
    }

    @Override
    public void prepareGroupExperiment(
            File experimentBaseDirectory,
            List<GroupCaseStudy> groupCaseStudies,
            DatasetLoader<? extends Rating>... datasetLoaders) {

        int i = 0;

        for (DatasetLoader<? extends Rating> datasetLoader : datasetLoaders) {
            for (GroupCaseStudy groupCaseStudy : groupCaseStudies) {

                String experimentName
                        = "[" + datasetLoader.getAlias() + "]_"
                        + groupCaseStudy.getAlias()
                        + "_hash=" + groupCaseStudy.hashCode();

                //Clean directory
                File finalDirectoryRS = new File(experimentBaseDirectory.getAbsolutePath() + File.separator + experimentName);
                File finalDirectoryDataset = new File(finalDirectoryRS.getAbsolutePath() + File.separator + "dataset");
                FileUtilities.deleteDirectoryRecursive(finalDirectoryRS);

                File experimentConfigurationFile = new File(finalDirectoryRS + File.separator + experimentName + ".xml");
                File datasetConfiguration = new File(finalDirectoryDataset.getAbsolutePath() + File.separator + datasetLoader.getAlias() + ".xml");

                GroupCaseStudyXML.saveCaseDescription(groupCaseStudy, experimentConfigurationFile);
                GroupCaseStudyXML.saveCaseDescription(new DefaultGroupCaseStudy(datasetLoader), datasetConfiguration);

            }
        }
    }

    public void executeAllExperimentsInDirectory(File directory) {
        List<File> children = Arrays.asList(directory.listFiles());

        children.stream()
                .forEach((singleExperimentDirectory) -> {
                    String[] args = {
                        ExecuteGroupXML.SEED_PARAMETER, "77352653",
                        ExecuteGroupXML.MODE_PARAMETER,
                        ExecuteGroupXML.XML_DIRECTORY, singleExperimentDirectory.getPath(),
                        ExecuteGroupXML.NUM_EXEC_PARAMETER, "1"};
                    Main.mainWithExceptions(args);
                });
    }

    public void executeAllExperimentsInDirectory(File directory, int numExec) {
        Arrays.asList(directory.listFiles())
                .stream()
                .forEach((singleExperimentDirectory) -> {
                    String[] args = {
                        ExecuteGroupXML.MODE_PARAMETER,
                        ExecuteGroupXML.SEED_PARAMETER, "77352653",
                        ExecuteGroupXML.XML_DIRECTORY, singleExperimentDirectory.getPath(),
                        ExecuteGroupXML.NUM_EXEC_PARAMETER, Integer.toString(numExec), Constants.PRINT_FULL_XML
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
                        ExecuteGroupXML.SEED_PARAMETER, "77352653",
                        ExecuteGroupXML.MODE_PARAMETER,
                        ExecuteGroupXML.XML_DIRECTORY, singleExperimentDirectory.getPath(),
                        ExecuteGroupXML.NUM_EXEC_PARAMETER, Integer.toString(numExec),
                        Constants.MAX_CPUS, Integer.toString(maxCPU)};

                    Main.mainWithExceptions(args);

                    System.out.println("==============================");
                });
    }
}
