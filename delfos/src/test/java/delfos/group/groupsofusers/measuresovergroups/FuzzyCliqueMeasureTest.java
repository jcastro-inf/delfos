package delfos.group.groupsofusers.measuresovergroups;

import delfos.group.groupsofusers.measuresovergroups.FuzzyCliqueMeasure;
import org.junit.Test;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.rs.trustbased.WeightedGraphAdapter;
import delfos.rs.trustbased.implicittrustcomputation.ShambourLu_UserBasedImplicitTrustComputation;

/**
 * Test para evaluar el cálculo de la medida de cohesión del grupo a través de
 * una red de confianza.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén)
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

        Global.setVerbose();
        System.out.println("getMeasure");

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

                    Global.showMessage("Group " + group + " has a clique value of " + result + "\n");
                }
            }
        }
        Global.showMessage("Fin.\n");
    }
}
