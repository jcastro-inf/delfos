package delfos.rs.trustbased;

import delfos.constants.DelfosTest;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.papertestdatasets.ImplicitTrustDataset;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 24-Apr-2013
 */
public class HybridUserItemTrustBasedTest extends DelfosTest {

    public HybridUserItemTrustBasedTest() {
    }

    /**
     * Test que implementa la ejecución del ejemplo provisto en el paper en que
     * se describe el algoritmo.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void paperTest() throws Exception {

        DatasetLoader<? extends Rating> datasetLoader = new ImplicitTrustDataset();

        HybridUserItemTrustBased recommenderSystem = new HybridUserItemTrustBased();

        HybridUserItemTrustBasedModel model = recommenderSystem.buildRecommendationModel(datasetLoader);

        Map<Long, Map<Long, Number>> finalPredictions = new TreeMap<>();

        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

        for (long idUser : ratingsDataset.allUsers()) {
            Set<Long> items = new TreeSet<>(datasetLoader.getRatingsDataset().allRatedItems());

            items.removeAll(datasetLoader.getRatingsDataset().getUserRated(idUser));

            Collection<Recommendation> recommendations = recommenderSystem.recommendToUser(datasetLoader, model, idUser, items);

            finalPredictions.put(idUser, new TreeMap<>());

            recommendations.stream().forEach((r) -> {
                finalPredictions.get(idUser).put(r.getIdItem(), r.getPreference());
            });
        }

        double delta = 0.01;

        List<Rating> predictions = new ArrayList<>(12);

        //User 1 predictions
        double predictionUser1Item1 = recommenderSystem.predictRating(datasetLoader, model, 1, 1).doubleValue();
        predictions.add(new Rating(1, 1, predictionUser1Item1));

        double predictionUser1Item4 = recommenderSystem.predictRating(datasetLoader, model, 1, 4).doubleValue();
        predictions.add(new Rating(1, 4, predictionUser1Item4));

        double predictionUser1Item5 = recommenderSystem.predictRating(datasetLoader, model, 1, 5).doubleValue();
        predictions.add(new Rating(1, 5, predictionUser1Item5));

        //User 2 predictions
        double predictionUser2Item2 = recommenderSystem.predictRating(datasetLoader, model, 2, 2).doubleValue();
        predictions.add(new Rating(2, 2, predictionUser2Item2));

        double predictionUser2Item3 = recommenderSystem.predictRating(datasetLoader, model, 2, 3).doubleValue();
        predictions.add(new Rating(2, 3, predictionUser2Item3));

        //User 3 predictions
        double predictionUser3Item1 = recommenderSystem.predictRating(datasetLoader, model, 3, 1).doubleValue();
        predictions.add(new Rating(3, 1, predictionUser3Item1));

        double predictionUser3Item3 = recommenderSystem.predictRating(datasetLoader, model, 3, 3).doubleValue();
        predictions.add(new Rating(3, 3, predictionUser3Item3));

        //User 4 predictions
        double predictionUser4Item2 = recommenderSystem.predictRating(datasetLoader, model, 4, 2).doubleValue();
        predictions.add(new Rating(4, 2, predictionUser4Item2));

        double predictionUser4Item3 = recommenderSystem.predictRating(datasetLoader, model, 4, 3).doubleValue();
        predictions.add(new Rating(4, 3, predictionUser4Item3));

        double predictionUser4Item4 = recommenderSystem.predictRating(datasetLoader, model, 4, 4).doubleValue();
        predictions.add(new Rating(4, 4, predictionUser4Item4));

        double predictionUser4Item5 = recommenderSystem.predictRating(datasetLoader, model, 4, 5).doubleValue();
        predictions.add(new Rating(4, 5, predictionUser4Item5));

        double predictionUser4Item6 = recommenderSystem.predictRating(datasetLoader, model, 4, 6).doubleValue();
        predictions.add(new Rating(4, 6, predictionUser4Item6));

        assertEquals("The data predicted does not match the paper data.", 3.19, predictionUser1Item1, delta);
        assertEquals("The data predicted does not match the paper data.", 2.80, predictionUser1Item4, delta);
        assertEquals("The data predicted does not match the paper data.", 3.80, predictionUser1Item5, delta);
        assertEquals("The data predicted does not match the paper data.", 4.01, predictionUser2Item2, delta);
        assertEquals("The data predicted does not match the paper data.", 4.01, predictionUser2Item3, delta);
        assertEquals("The data predicted does not match the paper data.", 3.70, predictionUser3Item1, delta);
        assertEquals("The data predicted does not match the paper data.", 4.39, predictionUser3Item3, delta);
        assertEquals("The data predicted does not match the paper data.", 2.63, predictionUser4Item2, delta);
        assertEquals("The data predicted does not match the paper data.", 2.67, predictionUser4Item3, delta);
        assertEquals("The data predicted does not match the paper data.", 1.78, predictionUser4Item4, delta);
        assertEquals("The data predicted does not match the paper data.", 2.71, predictionUser4Item5, delta);
        assertEquals("The data predicted does not match the paper data.", 1.33, predictionUser4Item6, delta);

    }
}
