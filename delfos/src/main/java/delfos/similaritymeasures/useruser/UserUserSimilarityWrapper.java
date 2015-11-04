package delfos.similaritymeasures.useruser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.similaritymeasures.BasicSimilarityMeasure;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import delfos.similaritymeasures.SimilarityMeasure;
import delfos.similaritymeasures.SimilarityMeasureAdapter;

/**
 *
 * @version 08-may-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class UserUserSimilarityWrapper extends SimilarityMeasureAdapter implements UserUserSimilarity {

    public static final Parameter WRAPPED_SIMILARITY = new Parameter(
            "WrappedSimilarity",
            new ParameterOwnerRestriction(SimilarityMeasure.class,
                    new PearsonCorrelationCoefficient()));

    private BasicSimilarityMeasure basicSimilarityMeasure;

    public UserUserSimilarityWrapper() {
        super();
        addParameter(WRAPPED_SIMILARITY);

        addParammeterListener(() -> {
            UserUserSimilarityWrapper.this.basicSimilarityMeasure = (BasicSimilarityMeasure) getParameterValue(WRAPPED_SIMILARITY);
        });
    }

    public UserUserSimilarityWrapper(BasicSimilarityMeasure basicSimilarityMeasure) {
        this();
        setParameterValue(WRAPPED_SIMILARITY, basicSimilarityMeasure);
    }

    @Override
    public double similarity(DatasetLoader<? extends Rating> datasetLoader, int idUser1, int idUser2) {

        Map<Integer, ? extends Rating> user1Ratings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser1);
        Map<Integer, ? extends Rating> user2Ratings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser2);

        Set<Integer> commonItems = new TreeSet<>(user1Ratings.keySet());
        commonItems.retainAll(user2Ratings.keySet());

        List<Float> v1 = new ArrayList<>();
        List<Float> v2 = new ArrayList<>();

        commonItems.stream().map((idItem) -> {
            v1.add(user1Ratings.get(idItem).getRatingValue().floatValue());
            return idItem;
        }).forEach((idItem) -> {
            v2.add(user2Ratings.get(idItem).getRatingValue().floatValue());
        });

        double similarity = basicSimilarityMeasure.similarity(v1, v2);

        return similarity;
    }
}
