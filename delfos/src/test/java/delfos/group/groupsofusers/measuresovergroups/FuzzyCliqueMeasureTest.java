package delfos.group.groupsofusers.measuresovergroups;

import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.rs.trustbased.WeightedGraphAdapter;
import delfos.rs.trustbased.implicittrustcomputation.ShambourLu_UserBasedImplicitTrustComputation;
import org.junit.Test;

/**
 * Test para evaluar el cálculo de la medida de cohesión del grupo a través de
 * una red de confianza.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 03-May-2013
 */
public class FuzzyCliqueMeasureTest {

    public FuzzyCliqueMeasureTest() {
    }

    /**
     * Test of getMeasure method, of class FuzzyCliqueMeasure.
     */
    @Test
    public void testGetMeasure() throws CannotLoadRatingsDataset {

        DatasetLoader<? extends Rating> datasetLoader = new RandomDatasetLoader(5, 5, 0.2);
        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

        ShambourLu_UserBasedImplicitTrustComputation implicitTrustComputation = new ShambourLu_UserBasedImplicitTrustComputation();
        WeightedGraphAdapter trustNetwork = implicitTrustComputation.computeTrustValues(datasetLoader, ratingsDataset.allUsers());
        FuzzyCliqueMeasure fuzzyClique = new FuzzyCliqueMeasure();

        for (int user1 : ratingsDataset.allUsers()) {
            for (int user2 : ratingsDataset.allUsers()) {
                if (user1 >= user2) {
                    continue;
                }
                for (int user3 : ratingsDataset.allUsers()) {
                    if (user2 >= user3) {
                        continue;
                    }

                    GroupOfUsers group = new GroupOfUsers(user1, user2, user3);

                    double result = fuzzyClique.getMeasure(datasetLoader, group);

                    Global.showInfoMessage("Group " + group + " has a clique value of " + result + "\n");
                }
            }
        }
    }
}
