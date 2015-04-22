package delfos.rs.contentbased.vsm.booleanvsm.symeonidis2007;

import delfos.constants.DelfosTest;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.contentbased.vsm.booleanvsm.BooleanFeaturesTransformation;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Test del sistema de recomendación {@link Symeonidis2007FeatureWeighted}
 * extraido del paper
 * <p>
 * <p>
 * Panagiotis Symeonidis, Alexandros Nanopoulos and Yannis Manolopoulos.
 * "Feature-weighted user model for recommender systems." In User Modeling 2007,
 * pp. 97-106. Springer Berlin Heidelberg, 2007.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2).
 *
 * @version 31-Octubre-2013
 */
public class Symeonidis2007FeatureWeightedTest extends DelfosTest {

    /**
     * Delta que se usa para comparar números reales.
     */
    public static final double delta = 0.001;

    public Symeonidis2007FeatureWeightedTest() {
    }

    @Test
    public void test_makeFFUserProfile_User1Profile() throws Exception {
        System.out.println("Testing the building of User 1 Feature Frequency profile");
        //Step1: Data preparation
        DatasetLoaderSymeonidisMock datasetLoader = new DatasetLoaderSymeonidisMock();
        Symeonidis2007FeatureWeighted instance = new Symeonidis2007FeatureWeighted();

        BooleanFeaturesTransformation featureTransformation = new BooleanFeaturesTransformation(datasetLoader.getContentDataset());
        int idUser = 1;

        //Step2: Execution
        MutableSparseVector ffUserProfile = instance.makeFFUserProfile(idUser, datasetLoader, featureTransformation);

        //Step3: Results check
        {
            //Compruebo el cálculo del IUF.
            SparseVector ffExpected = MutableSparseVector.wrapUnsorted(
                    new long[]{0, 1, 2},
                    new double[]{2, 2, 1}).immutable();
            SparseVector ffActual = ffUserProfile;

            for (long key : ffExpected.keySet()) {
                double expectedValue = ffExpected.get(key);
                double actualValue = ffActual.get(key);
                assertEquals("The ff vector for user " + idUser + " is wrong at " + key + " feature, ", expectedValue, actualValue, delta);
            }
        }

    }

    @Test
    public void testBuild() throws Exception {
        System.out.println("Testing the whole building proccess.");

        //Step1: Data preparation
        DatasetLoader<? extends Rating> datasetLoader = new DatasetLoaderSymeonidisMock();
        Symeonidis2007FeatureWeighted instance = new Symeonidis2007FeatureWeighted();

        //Step2: Execution
        Symeonidis2007Model model = instance.build(datasetLoader);

        //Step3: Results check
        {
            //Compruebo el cálculo del IUF.
            SparseVector iufExpected = MutableSparseVector.wrapUnsorted(
                    new long[]{0, 1, 2, 3},
                    new double[]{0.301, 0.125, 0.301, 0.602}).immutable();
            SparseVector iufActual = model.getAllIUF();

            for (long key : iufExpected.keySet()) {
                double expectedValue = iufExpected.get(key);
                double actualValue = iufActual.get(key);
                assertEquals("The inverse user frequency calculation is wrong, ", expectedValue, actualValue, delta);
            }
        }

        {
            //Compruebo el perfil del User 1.
            final int idUser = 1;
            SparseVector userExpected = MutableSparseVector.wrap(
                    new long[]{0, 1, 2},
                    new double[]{0.602, 0.250, 0.301});
            SparseVector userActual = model.getBooleanFeaturesTransformation().getFloatValuesSparseVector(model.getUserProfile(idUser));

            for (long key : userExpected.keySet()) {
                double expectedValue = userExpected.get(key);
                double actualValue = userActual.get(key);
                assertEquals("The User " + idUser + " profile is wrong, ", expectedValue, actualValue, delta);
            }
        }

        {
            //Compruebo el perfil del User 2.
            final int idUser = 2;
            SparseVector userExpected = MutableSparseVector.wrap(
                    new long[]{0, 1},
                    new double[]{0.301, 0.250});
            SparseVector userActual = model.getBooleanFeaturesTransformation().getFloatValuesSparseVector(model.getUserProfile(idUser));

            for (long key : userExpected.keySet()) {
                double expectedValue = userExpected.get(key);
                double actualValue = userActual.get(key);
                assertEquals("The User " + idUser + " profile is wrong, ", expectedValue, actualValue, delta);
            }
        }

        {
            //Compruebo el perfil del User 3.
            final int idUser = 3;
            SparseVector userExpected = MutableSparseVector.wrap(
                    new long[]{3},
                    new double[]{0.602});
            SparseVector userActual = model.getBooleanFeaturesTransformation().getFloatValuesSparseVector(model.getUserProfile(idUser));

            for (long key : userExpected.keySet()) {
                double expectedValue = userExpected.get(key);
                double actualValue = userActual.get(key);
                assertEquals("The User " + idUser + " profile is wrong, ", expectedValue, actualValue, delta);
            }
        }

        {
            //Compruebo el perfil del User 4.
            final int idUser = 4;
            SparseVector userExpected = MutableSparseVector.wrap(
                    new long[]{1, 2},
                    new double[]{0.25, 0.301});
            SparseVector userActual = model.getBooleanFeaturesTransformation().getFloatValuesSparseVector(model.getUserProfile(idUser));

            for (long key : userExpected.keySet()) {
                double expectedValue = userExpected.get(key);
                double actualValue = userActual.get(key);
                assertEquals("The User " + idUser + " profile is wrong, ", expectedValue, actualValue, delta);
            }
        }
    }

    @Test
    public void testGetUserNeighbors() throws Exception {

        System.out.println("recommendOnly");

        //Step1: Data preparation
        final int idUser = 2;
        DatasetLoader<? extends Rating> datasetLoader = new DatasetLoaderSymeonidisMock();
        Symeonidis2007FeatureWeighted instance = new Symeonidis2007FeatureWeighted();
        Symeonidis2007Model model = instance.build(datasetLoader);
        Symeonidis2007UserProfile userProfile = instance.makeUserProfile(idUser, datasetLoader, model);

        //Step2: Execution
        List<Neighbor> result = instance.getUserNeighbors(model, userProfile);

        //Step3: Results check
        //Check the item order
        assertEquals("The neighbor in the first place should be User", 4, result.get(0).getIdNeighbor());
        assertEquals("The neighbor in the second place should be User", 1, result.get(1).getIdNeighbor());

        //Check the item preference value
        assertEquals("The similarity with User 4 is wrong,", 1.0, result.get(0).getSimilarity(), delta);
        assertEquals("The similarity with User 1 is wrong,", 0.9555106, result.get(1).getSimilarity(), delta);
    }

    @Test
    public void testRecommendOnly() throws Exception {

        System.out.println("recommendOnly");

        //Step1: Data preparation
        final int idUser = 2;
        DatasetLoader<? extends Rating> datasetLoader = new DatasetLoaderSymeonidisMock();
        Symeonidis2007FeatureWeighted instance = new Symeonidis2007FeatureWeighted();
        Symeonidis2007Model model = instance.build(datasetLoader);
        Set<Integer> idItemList = new TreeSet<>();
        idItemList.add(1);
        idItemList.add(3);
        idItemList.add(5);

        //Step2: Execution
        List<Recommendation> result = new ArrayList<>(instance.recommendOnly(datasetLoader, model, idUser, idItemList));

        //Step3: Results check
        //Check the item order
        assertEquals("The item recommended in the first place should be Item", 5, result.get(0).getIdItem());
        assertEquals("The item recommended in the second place should be Item", 3, result.get(1).getIdItem());
        assertEquals("The item recommended in the third place should be Item", 1, result.get(2).getIdItem());

        //Check the item preference value
        assertEquals("For Item 5, preference value", 6, result.get(0).getPreference().doubleValue(), delta);
        assertEquals("For Item 3, preference value", 5, result.get(1).getPreference().doubleValue(), delta);
        assertEquals("For Item 1, preference value", 3, result.get(2).getPreference().doubleValue(), delta);
    }
}
