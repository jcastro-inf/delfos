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
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.similaritymeasures.BasicSimilarityMeasure;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import delfos.similaritymeasures.SimilarityMeasure;
import delfos.similaritymeasures.SimilarityMeasureAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @version 08-may-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
    public double similarity(DatasetLoader<? extends Rating> datasetLoader, long idUser1, long idUser2) {

        Map<Long, ? extends Rating> user1Ratings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser1);
        Map<Long, ? extends Rating> user2Ratings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser2);

        Set<Long> commonItems = new TreeSet<>(user1Ratings.keySet());
        commonItems.retainAll(user2Ratings.keySet());

        List<Double> v1 = new ArrayList<>();
        List<Double> v2 = new ArrayList<>();

        commonItems.stream().map((idItem) -> {
            v1.add(user1Ratings.get(idItem).getRatingValue().doubleValue());
            return idItem;
        }).forEach((idItem) -> {
            v2.add(user2Ratings.get(idItem).getRatingValue().doubleValue());
        });

        double similarity = basicSimilarityMeasure.similarity(v1, v2);

        return similarity;
    }

    @Override
    public double similarity(DatasetLoader<? extends Rating> datasetLoader, User user1, User user2) {
        return similarity(datasetLoader, user1.getId(), user2.getId());
    }
}
