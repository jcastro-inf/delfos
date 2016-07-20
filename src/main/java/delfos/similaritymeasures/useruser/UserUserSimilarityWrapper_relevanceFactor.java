/*
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package delfos.similaritymeasures.useruser;

import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.similaritymeasures.SimilarityMeasureAdapter;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @version 08-may-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class UserUserSimilarityWrapper_relevanceFactor extends SimilarityMeasureAdapter implements UserUserSimilarity {

    /**
     * Almacena el valor del factor de relevancia aplicado si el parámetro {@link KnnModelBasedCFRS#relevanceFactor}
     * indica que se debe usar factor de relevancia (true). El valor por defecto del factor de relevancia es 50.
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

        if (idUser1 == idUser2) {
            return 1;
        }

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

    @Override
    public double similarity(DatasetLoader<? extends Rating> datasetLoader, User user1, User user2) {
        return similarity(datasetLoader, user1.getId(), user2.getId());
    }
}
