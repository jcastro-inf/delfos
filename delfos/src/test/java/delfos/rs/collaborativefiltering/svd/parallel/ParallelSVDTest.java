package delfos.rs.collaborativefiltering.svd.parallel;

import delfos.rs.collaborativefiltering.svd.parallel.ParallelSVDModel;
import delfos.rs.collaborativefiltering.svd.parallel.ParallelSVD;
import java.util.LinkedList;
import java.util.List;
import static org.junit.Assert.fail;
import org.junit.Test;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.rs.RecommenderSystemBuildingProgressListener_default;
import delfos.dataset.util.DatasetPrinter;

/**
 *
 * @author Jorge
 */
public class ParallelSVDTest extends DelfosTest {

    public ParallelSVDTest() {
    }

    @Test
    public void testTrainingWithCompleteDataset() throws Exception {

        ParallelSVD parallelSVD = new ParallelSVD(5, 10);
        parallelSVD.setSeedValue(123456789);
        parallelSVD.setParameterValue(ParallelSVD.LEARNING_RATE, 0.1);
        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("complete-5u-10i");

//        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");
//        parallelSVD.setParameterValue(ParallelSVD.LEARNING_RATE, 0.02);
        parallelSVD.addBuildingProgressListener(new RecommenderSystemBuildingProgressListener_default(System.out, 0));
        ParallelSVDModel recommendationModel = parallelSVD.build(datasetLoader);

        {
            double predictRating = parallelSVD.predictRating(datasetLoader, recommendationModel, 1, 5).doubleValue();
            System.out.println("Predicted --> " + predictRating);
        }

        int numCellFullMatrix = datasetLoader.getRatingsDataset().allUsers().size() * datasetLoader.getRatingsDataset().allRatedItems().size();
        if (numCellFullMatrix > 10000) {
            return;
        }

        System.out.print("=======================================\n");
        System.out.print("User features:\n");
        datasetLoader.getRatingsDataset().allUsers().stream().forEach((idUser) -> {
            if (recommendationModel.containsUser(idUser)) {
                System.out.print("User " + idUser + " \t" + recommendationModel.getUserFeatures(idUser).toString() + "\n");
            }
        });
        System.out.print("---------------------------------------\n");
        System.out.print("Item features:\n");

        datasetLoader.getRatingsDataset().allRatedItems().stream().forEach((idItem) -> {
            if (recommendationModel.containsItem(idItem)) {
                System.out.print("Item " + idItem + " \t" + recommendationModel.getItemFeatures(idItem).toString() + "\n");
            }
        });
        System.out.print("=======================================\n");

        List<Rating> ratings = new LinkedList<>();
        for (int idUser : datasetLoader.getRatingsDataset().allUsers()) {
            for (int idItem : datasetLoader.getRatingsDataset().allRatedItems()) {
                try {
                    Number predictRating = parallelSVD.predictRating(datasetLoader, recommendationModel, idUser, idItem);
                    ratings.add(new Rating(idUser, idItem, predictRating));
                } catch (NotEnoughtUserInformation ex) {
                    fail("This recommender system should never have a coverage failure.");
                }
            }
        }

        RatingsDataset<Rating> predictedRatingsDataset = new BothIndexRatingsDataset<>(ratings);
        String matrix = DatasetPrinter.printCompactRatingTable(predictedRatingsDataset);

        System.out.println("============== Prediction matrix =========================");
        System.out.println(matrix);
        System.out.println("==========================================================");
    }
}
