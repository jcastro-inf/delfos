package delfos.similaritymeasures.useruser;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.similaritymeasures.SimilarityMeasureAdapter;

/**
 *
 * @version 08-may-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class UserUserSimilarityWrapper_relevanceFactor extends SimilarityMeasureAdapter implements UserUserSimilarity {

    /**
     * Almacena el valor del factor de relevancia aplicado si el parámetro
     * {@link KnnModelBasedCFRS#relevanceFactor} indica que se debe usar factor
     * de relevancia (true). El valor por defecto del factor de relevancia es
     * 50.
     *
     * @see KnnModelBasedCFRS#relevanceFactor
     */
    public static final Parameter RELEVANCE_FACTOR_VALUE = new Parameter("Relevance_factor_value", new IntegerParameter(1, 9999, 20));

    static {
        ParameterOwnerRestriction parameterOwnerRestriction = new ParameterOwnerRestriction(
                UserUserSimilarity.class,
                new UserUserSimilarityWrapper());
        WRAPPED_SIMILARITY = new Parameter(
                "WrappedSimilarity",
                parameterOwnerRestriction);
    }

    public static final Parameter WRAPPED_SIMILARITY;

    private UserUserSimilarity basicSimilarityMeasure;

    public UserUserSimilarityWrapper_relevanceFactor() {
        super();
        addParameter(WRAPPED_SIMILARITY);
        addParameter(RELEVANCE_FACTOR_VALUE);

        addParammeterListener(() -> {
            UserUserSimilarityWrapper_relevanceFactor.this.basicSimilarityMeasure = (UserUserSimilarity) getParameterValue(WRAPPED_SIMILARITY);
        });
    }

    public UserUserSimilarityWrapper_relevanceFactor(UserUserSimilarity userUserSimilarity) {
        this();
        setParameterValue(WRAPPED_SIMILARITY, userUserSimilarity);
    }

    public UserUserSimilarityWrapper_relevanceFactor(UserUserSimilarity userUserSimilarity, int relevanceFactor) {
        this();
        setParameterValue(WRAPPED_SIMILARITY, userUserSimilarity);
        setParameterValue(RELEVANCE_FACTOR_VALUE, relevanceFactor);
    }

    @Override
    public double similarity(DatasetLoader<? extends Rating> datasetLoader, int idUser1, int idUser2) {

        Map<Integer, ? extends Rating> user1Ratings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser1);
        Map<Integer, ? extends Rating> user2Ratings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser2);

        Set<Integer> commonItems = new TreeSet<>(user1Ratings.keySet());
        commonItems.retainAll(user2Ratings.keySet());

        double similarity = basicSimilarityMeasure.similarity(datasetLoader, idUser1, idUser2);

        int relevanceFactorValue = (Integer) getParameterValue(RELEVANCE_FACTOR_VALUE);
        if (commonItems.size() < relevanceFactorValue) {
            similarity = similarity * commonItems.size() / relevanceFactorValue;
        }
        return similarity;

    }
}
