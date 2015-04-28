package delfos.similaritymeasures.useruser;

import delfos.similaritymeasures.useruser.UserUserMultipleCorrelationCoefficient;
import delfos.similaritymeasures.useruser.UserUserSimilarityWrapper;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;
import delfos.common.Chronometer;
import delfos.common.FileUtilities;
import delfos.common.datastructures.histograms.HistogramNumbersSmart;
import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.constants.TestConstants;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.util.DatasetPrinter;
import delfos.similaritymeasures.BasicSimilarityMeasure;
import delfos.similaritymeasures.CosineCoefficient;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;

/**
 *
 * @version 08-may-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class UserUserMultipleCorrelationCoefficientTest {

    private static MockDatasetLoader_UserUserSimilarity datasetLoader;

    public UserUserMultipleCorrelationCoefficientTest() {
    }

    @BeforeClass
    public static void beforeClass() {
        datasetLoader = new MockDatasetLoader_UserUserSimilarity();
    }

    @Test
    public void testWrapper_PearsonCorrelationCoefficient() throws Exception {
        System.out.println("testWrapper_PearsonCorrelationCoefficient");

        PearsonCorrelationCoefficient pcc = new PearsonCorrelationCoefficient();
        int idUser1 = 54;
        int idUser2 = 643;
        UserUserMultipleCorrelationCoefficient instance = new UserUserMultipleCorrelationCoefficient(pcc);
        double expResult = 0.05320826;
        double result = instance.similarity(datasetLoader, idUser1, idUser2);
        assertEquals(expResult, result, 0.000001);
    }

    @Test
    public void testWrapper_CosineCorrelationCoefficient() throws Exception {
        System.out.println("testWrapper_CosineCorrelationCoefficient");

        BasicSimilarityMeasure basicSimilarityMeasure = new CosineCoefficient();
        int idUser1 = 54;
        int idUser2 = 643;
        UserUserMultipleCorrelationCoefficient instance = new UserUserMultipleCorrelationCoefficient(basicSimilarityMeasure);
        double expResult = 0.61131468;
        double result = instance.similarity(datasetLoader, idUser1, idUser2);
        assertEquals(expResult, result, 0.000001);
    }

    public void testWrapper_PearsonCorrelationCoefficient_HISTOGRAMS() throws Exception {
        System.out.println("testWrapper_PearsonCorrelationCoefficient_HISTOGRAMS");

        PearsonCorrelationCoefficient pcc = new PearsonCorrelationCoefficient();

        UserUserMultipleCorrelationCoefficient pcc_multi = new UserUserMultipleCorrelationCoefficient(pcc);
        UserUserSimilarityWrapper pcc_wrapped = new UserUserSimilarityWrapper(pcc);

        HistogramNumbersSmart histogramPccMulti = new HistogramNumbersSmart(.1);
        HistogramNumbersSmart histogramPCC = new HistogramNumbersSmart(.1);

        System.out.println("idUser\tidNeighbor\tpcc\tpccMulti");

        for (int idUser : datasetLoader.getRatingsDataset().allUsers()) {
            for (int idNeighbor : datasetLoader.getRatingsDataset().allUsers()) {
                if (idUser == idNeighbor) {
                    continue;
                }

                double pccMultiValue;
                double pccValue;

                try {
                    pccMultiValue = pcc_multi.similarity(datasetLoader, idUser, idNeighbor);
                } catch (CouldNotComputeSimilarity ex) {
                    pccMultiValue = 0;
                }

                try {
                    pccValue = pcc_wrapped.similarity(datasetLoader, idUser, idNeighbor);
                } catch (CouldNotComputeSimilarity ex) {
                    pccValue = 0;
                }

                System.out.println(idUser + "\t" + idNeighbor + "\t" + pccValue + "\t" + pccMultiValue);
                histogramPCC.addValue(pccValue);
                histogramPccMulti.addValue(pccMultiValue);
            }
        }

        histogramPCC.printHistogram(System.out);
        histogramPccMulti.printHistogram(System.out);
    }

    //@Test
    public void computeRValue_testMetodico() throws IOException, UserNotFound {
        System.out.println("computeRValue_testMetodico");

        PearsonCorrelationCoefficient pcc = new PearsonCorrelationCoefficient();

        UserUserMultipleCorrelationCoefficient pcc_multi = new UserUserMultipleCorrelationCoefficient(pcc);
        UserUserSimilarityWrapper pcc_wrapped = new UserUserSimilarityWrapper(pcc);

        HistogramNumbersSmart histogramPccMulti = new HistogramNumbersSmart(.1);

        File directory = new File(TestConstants.TEST_DATA_DIRECTORY + this.getClass().getSimpleName());
        FileUtilities.deleteDirectoryRecursive(directory);
        directory.mkdirs();

        Chronometer chronometer = new Chronometer();

        String headerLine = "idTargeUser\tidNeighborUser\tidFriendOfNeighborUser\tpccMulti\tsim(a,b)\tsim(a,c)\tsim(b,c)\n";

        try (FileWriter stdOutput = new FileWriter(directory + File.separator + "metodico.txt")) {

            stdOutput.write(headerLine);

            double[] values = {
                -1, -0.95, -0.9, -0.85, -0.8, -0.75, -0.7, -0.65, -0.6, -0.55, -0.5, -0.45, -0.4, -0.35, -0.3, -0.25, -0.2, -0.15, -0.1, -0.05,
                0, 0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.45, 0.5, 0.55, 0.6, 0.65, 0.7, 0.75, 0.8, 0.85, 0.9, 0.95, 1};

            for (double simAB : values) {
                for (double simAC_i : values) {
                    for (double simBC_i : values) {
                        try {
                            double computeRValue = pcc_multi.computeRValueFromSimilarities(simAB, simAC_i, simBC_i);
                            String contentLine = -1 + "\t" + -1 + "\t" + -1 + "\t" + computeRValue + "\t" + simAB + "\t" + simAC_i + "\t" + simBC_i + "\n";
                            stdOutput.write(contentLine);
                        } catch (CouldNotComputeSimilarity ex) {
                            String contentLine = -1 + "\t" + -1 + "\t" + -1 + "\tNaN\t" + simAB + "\t" + simAC_i + "\t" + simBC_i + "\n";
                            stdOutput.write(contentLine);
                        }
                    }
                }
            }
        }

        System.out.println("computeRValue_testMetodico Finished");

    }

    //@Test
    public void computeRValue_test() throws UserNotFound, IOException {
        System.out.println("computeRValue_test");

        PearsonCorrelationCoefficient pcc = new PearsonCorrelationCoefficient();

        UserUserMultipleCorrelationCoefficient pcc_multi = new UserUserMultipleCorrelationCoefficient(pcc);
        UserUserSimilarityWrapper pcc_wrapped = new UserUserSimilarityWrapper(pcc);

        HistogramNumbersSmart histogramPccMulti = new HistogramNumbersSmart(.1);

        File directory = new File(TestConstants.TEST_DATA_DIRECTORY + this.getClass().getSimpleName());
        FileUtilities.deleteDirectoryRecursive(directory);
        directory.mkdirs();

        Chronometer chronometer = new Chronometer();

        String headerLine = "idTargeUser\tidNeighborUser\tidFriendOfNeighborUser\tpccMulti\tsim(a,b)\tsim(a,c)\tsim(b,c)\n";

        try (FileWriter stdOutput = new FileWriter(directory + File.separator + "std.txt"); FileWriter errOutput = new FileWriter(directory + File.separator + "err.txt"); FileWriter errExplainedOutput = new FileWriter(directory + File.separator + "err-explained.txt")) {

            stdOutput.write(headerLine);
            errOutput.write(headerLine);

            final Integer[] allUsers = datasetLoader.getRatingsDataset().allUsers().toArray(new Integer[0]);
            final Random random = new Random(0);

            while (true) {

                int idTargetUser = allUsers[random.nextInt(allUsers.length)];
                int idNeighborUser = allUsers[random.nextInt(allUsers.length)];
                int idFriendOfNeighborUser = allUsers[random.nextInt(allUsers.length)];

                if (idTargetUser == idNeighborUser || idNeighborUser == idFriendOfNeighborUser || idTargetUser == idFriendOfNeighborUser) {
                    continue;
                }

                try {
                    double computeRValue = pcc_multi.computeRValue(datasetLoader, idTargetUser, idNeighborUser, idFriendOfNeighborUser);

                    double simAB = pcc_wrapped.similarity(datasetLoader, idTargetUser, idNeighborUser);
                    double simAC = pcc_wrapped.similarity(datasetLoader, idTargetUser, idFriendOfNeighborUser);
                    double simBC = pcc_wrapped.similarity(datasetLoader, idNeighborUser, idFriendOfNeighborUser);

                    String contentLine = idTargetUser + "\t" + idNeighborUser + "\t" + idFriendOfNeighborUser + "\t" + computeRValue + "\t" + simAB + "\t" + simAC + "\t" + simBC + "\n";

                    if (computeRValue <= 1 && computeRValue >= 0) {
                        stdOutput.write(contentLine);
                        histogramPccMulti.addValue(computeRValue);
                    } else {
                        Collection<Integer> users = new ArrayList<>();
                        users.add(idTargetUser);
                        users.add(idNeighborUser);
                        users.add(idFriendOfNeighborUser);

                        String ratingsTable = DatasetPrinter.printCompactRatingTable(datasetLoader.getRatingsDataset(), users);

                        {
                            errOutput.write(contentLine);
                            errOutput.flush();
                        }
                        {
                            errExplainedOutput.write(headerLine);
                            errExplainedOutput.write(contentLine);
                            errExplainedOutput.write("\n");
                            errExplainedOutput.write(ratingsTable);
                            errExplainedOutput.write("\n......................................................................................\n");
                        }
                    }
                } catch (CouldNotComputeSimilarity ex) {
                    //
                }

                //Ejecuto durante 3 minutos
                if (chronometer.getTotalElapsed() > 180000) {
                    break;
                }
            }
        }
    }

    @Test
    public void testWithFullMatrix() throws UserNotFound {
        DatasetLoader<? extends Rating> randomDataset = new RandomDatasetLoader(50, 50, 1);

        Collection<Integer> users = randomDataset.getRatingsDataset().allUsers();

        PearsonCorrelationCoefficient pcc = new PearsonCorrelationCoefficient();
        UserUserMultipleCorrelationCoefficient pcc_multi = new UserUserMultipleCorrelationCoefficient(pcc);

        UserUserSimilarityWrapper pcc_wrapped = new UserUserSimilarityWrapper(pcc);

        HistogramNumbersSmart histogramPccMulti = new HistogramNumbersSmart(.1);
        for (int idUser : users) {
            for (int idNeighbor : users) {
                for (int idFriendOfNeighbor : users) {
                    if (idUser == idNeighbor || idUser == idFriendOfNeighbor
                            || idNeighbor == idFriendOfNeighbor) {
                        continue;
                    }
                    try {

                        double simAB = pcc_wrapped.similarity(randomDataset, idUser, idNeighbor);
                        double simAC_i = pcc_wrapped.similarity(randomDataset, idUser, idFriendOfNeighbor);
                        double simBC_i = pcc_wrapped.similarity(randomDataset, idNeighbor, idFriendOfNeighbor);

                        double computeRValue = pcc_multi.computeRValueFromSimilarities(simAB, simAC_i, simBC_i);
                        String contentLine = -1 + "\t" + -1 + "\t" + -1 + "\t" + computeRValue + "\t" + simAB + "\t" + simAC_i + "\t" + simBC_i + "\n";
                        System.out.print(contentLine);

                        histogramPccMulti.addValue(computeRValue);
                    } catch (CouldNotComputeSimilarity ex) {
                        String contentLine = -1 + "\t" + -1 + "\t" + -1 + "\tNaN\t??\t??\t??\n";
                        System.out.print(contentLine);
                    }
                }
            }
        }

        histogramPccMulti.printHistogram(System.out);
    }
}
