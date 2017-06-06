package delfos.rs.contentbased.vsm.booleanvsm.symeonidis2007;

import delfos.constants.DelfosTest;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.contentbased.vsm.booleanvsm.BooleanFeaturesTransformation;
import delfos.rs.contentbased.vsm.booleanvsm.SparseVector;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.math4.util.Pair;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test del sistema de recomendación {@link Symeonidis2007FeatureWeighted} extraido del paper
 * <p>
 * <p>
 * Panagiotis Symeonidis, Alexandros Nanopoulos and Yannis Manolopoulos. "Feature-weighted user model for recommender
 * systems." In User Modeling 2007, pp. 97-106. Springer Berlin Heidelberg, 2007.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 31-Octubre-2013
 */
public class Symeonidis2007FeatureWeightedTest extends DelfosTest {

    /**
     * Delta que se usa para comparar números reales.
     */
    public static final double DELTA = 0.001;

    public Symeonidis2007FeatureWeightedTest() {
    }

    @Test
    public void test_makeFFUserProfile_User1Profile() throws Exception {
        //Step1: Data preparation
        DatasetLoaderSymeonidisMock datasetLoader = new DatasetLoaderSymeonidisMock();
        Symeonidis2007FeatureWeighted instance = new Symeonidis2007FeatureWeighted();

        BooleanFeaturesTransformation featureTransformation = new BooleanFeaturesTransformation(datasetLoader.getContentDataset());
        int idUser = 1;

        //Step2: Execution
        SparseVector<Long> ffUserProfile = instance.makeFFUserProfile(idUser, datasetLoader, featureTransformation);

        //Step3: Results check
        {
            //Compruebo el cálculo del IUF.
            SparseVector<Long> ffExpected
                    = SparseVector.createFromPairs(
                            new Pair<>(0l, 2.0),
                            new Pair<>(1l, 2.0),
                            new Pair<>(2l, 1.0)
                    );

            SparseVector<Long> ffActual = ffUserProfile;

            for (long key : ffExpected.keySet()) {
                double expectedValue = ffExpected.get(key);
                double actualValue = ffActual.get(key);
                assertEquals("The ff vector for user " + idUser + " is wrong at " + key + " feature, ", expectedValue, actualValue, DELTA);
            }
        }

    }

    @Test
    public void testBuild() throws Exception {

        //Step1: Data preparation
        DatasetLoader<? extends Rating> datasetLoader = new DatasetLoaderSymeonidisMock();
        Symeonidis2007FeatureWeighted instance = new Symeonidis2007FeatureWeighted();

        //Step2: Execution
        Symeonidis2007Model model = instance.buildRecommendationModel(datasetLoader);

        //Step3: Results check
        {
            //Compruebo el cálculo del IUF.
            SparseVector<Long> iufExpected = SparseVector.createFromPairs(
                    new Pair<>(0l, 0.301),
                    new Pair<>(1l, 0.125),
                    new Pair<>(2l, 0.301),
                    new Pair<>(3l, 0.602)
            );

            SparseVector<Long> iufActual = model.getAllIUF();

            for (long key : iufExpected.keySet()) {
                double expectedValue = iufExpected.get(key);
                double actualValue = iufActual.get(key);
                assertEquals("The inverse user frequency calculation is wrong, ", expectedValue, actualValue, DELTA);
            }
        }

        {
            //Compruebo el perfil del User 1.
            final int idUser = 1;
            SparseVector<Long> userExpected = SparseVector.createFromPairs(
                    new Pair<>(0l, 0.602),
                    new Pair<>(1l, 0.250),
                    new Pair<>(2l, 0.301)
            );

            SparseVector<Long> userActual = model.getBooleanFeaturesTransformation().getDoubleValuesSparseVector(model.getUserProfile(idUser));

            for (long key : userExpected.keySet()) {
                double expectedValue = userExpected.get(key);
                double actualValue = userActual.get(key);
                assertEquals("The User " + idUser + " profile is wrong, ", expectedValue, actualValue, DELTA);
            }
        }

        {
            //Compruebo el perfil del User 2.
            final int idUser = 2;
            SparseVector<Long> userExpected = SparseVector.createFromPairs(
                    new Pair<>(0l, 0.301),
                    new Pair<>(1l, 0.250)
            );

            SparseVector<Long> userActual = model.getBooleanFeaturesTransformation().getDoubleValuesSparseVector(model.getUserProfile(idUser));

            for (long key : userExpected.keySet()) {
                double expectedValue = userExpected.get(key);
                double actualValue = userActual.get(key);
                assertEquals("The User " + idUser + " profile is wrong, ", expectedValue, actualValue, DELTA);
            }
        }

        {
            //Compruebo el perfil del User 3.
            final int idUser = 3;
            SparseVector<Long> userExpected = SparseVector.createFromPairs(
                    new Pair<>(3l, 0.602)
            );
            SparseVector<Long> userActual = model.getBooleanFeaturesTransformation().getDoubleValuesSparseVector(model.getUserProfile(idUser));

            for (long key : userExpected.keySet()) {
                double expectedValue = userExpected.get(key);
                double actualValue = userActual.get(key);
                assertEquals("The User " + idUser + " profile is wrong, ", expectedValue, actualValue, DELTA);
            }
        }

        {
            //Compruebo el perfil del User 4.
            final int idUser = 4;
            SparseVector<Long> userExpected = SparseVector.createFromPairs(
                    new Pair<>(1l, 0.250),
                    new Pair<>(2l, 0.301)
            );

            SparseVector<Long> userActual = model.getBooleanFeaturesTransformation().getDoubleValuesSparseVector(model.getUserProfile(idUser));

            for (long key : userExpected.keySet()) {
                double expectedValue = userExpected.get(key);
                double actualValue = userActual.get(key);
                assertEquals("The User " + idUser + " profile is wrong, ", expectedValue, actualValue, DELTA);
            }
        }
    }

    @Test
    public void testGetUserNeighbors() throws Exception {

        //Step1: Data preparation
        final int idUser = 2;
        DatasetLoader<? extends Rating> datasetLoader = new DatasetLoaderSymeonidisMock();
        Symeonidis2007FeatureWeighted instance = new Symeonidis2007FeatureWeighted();
        Symeonidis2007Model model = instance.buildRecommendationModel(datasetLoader);
        Symeonidis2007UserProfile userProfile = instance.makeUserProfile(idUser, datasetLoader, model);

        //Step2: Execution
        List<Neighbor> result = instance.getUserNeighbors(model, userProfile);

        //Step3: Results check
        //Check the item order
        assertEquals("The neighbor in the first place should be User", 4, result.get(0).getIdNeighbor());
        assertEquals("The neighbor in the second place should be User", 1, result.get(1).getIdNeighbor());

        //Check the item preference value
        assertEquals("The similarity with User 4 is wrong,", 1.0, result.get(0).getSimilarity(), DELTA);
        assertEquals("The similarity with User 1 is wrong,", 0.9555106, result.get(1).getSimilarity(), DELTA);
    }

    @Test
    public void testRecommendOnly() throws Exception {

        //Step1: Data preparation
        final int idUser = 2;
        DatasetLoader<? extends Rating> datasetLoader = new DatasetLoaderSymeonidisMock();
        Symeonidis2007FeatureWeighted instance = new Symeonidis2007FeatureWeighted();
        Symeonidis2007Model model = instance.buildRecommendationModel(datasetLoader);
        Set<Long> candidateItems = new TreeSet<>();
        candidateItems.add(1l);
        candidateItems.add(3l);
        candidateItems.add(5l);

        //Step2: Execution
        List<Recommendation> sortedRecommendations = new ArrayList<>(instance.recommendToUser(datasetLoader, model, idUser, candidateItems));
        Collections.sort(sortedRecommendations);

        //Step3: Results check
        //Check the item order
        assertEquals("The item recommended in the first place should be Item", 5l, (long) sortedRecommendations.get(0).getIdItem());
        assertEquals("The item recommended in the second place should be Item", 3l, (long) sortedRecommendations.get(1).getIdItem());
        assertEquals("The item recommended in the third place should be Item", 1, (long) sortedRecommendations.get(2).getIdItem());

        //Check the item preference value
        assertEquals("For Item 5, preference value", 6, sortedRecommendations.get(0).getPreference().doubleValue(), DELTA);
        assertEquals("For Item 3, preference value", 5, sortedRecommendations.get(1).getPreference().doubleValue(), DELTA);
        assertEquals("For Item 1, preference value", 3, sortedRecommendations.get(2).getPreference().doubleValue(), DELTA);
    }
}
