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

import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.rs.RecommenderSystemAdapter;
import delfos.rs.collaborativefiltering.knn.CommonRating;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.similaritymeasures.CollaborativeSimilarityMeasure;
import delfos.similaritymeasures.EntropyOfDifferences;
import delfos.similaritymeasures.SimilarityMeasureAdapter;
import java.util.Collection;

/**
 *
 * @author jcastro
 */
public class EntropyOfDifferences_asymmetric extends SimilarityMeasureAdapter implements CollaborativeSimilarityMeasure, UserUserSimilarity {

    @Override
    public double similarity(DatasetLoader<? extends Rating> datasetLoader, User user1, User user2) throws UserNotFound, CannotLoadRatingsDataset {
        return similarity(CommonRating.intersection(datasetLoader, user1, user2), datasetLoader.getRatingsDataset());
    }

    private final EntropyOfDifferences innerSimilarity = new EntropyOfDifferences();

    @Override
    public double similarity(Collection<CommonRating> commonRatings, RatingsDataset<? extends Rating> ratings) throws CouldNotComputeSimilarity {
        double intersectionSize = commonRatings.size();

        double ret = commonRatings.stream().findAny().map(commonRating -> {

            int idUser1 = commonRating.getIdR1();
            int idUser2 = commonRating.getIdR2();

            double user1size = ratings.getUserRatingsRated(idUser1).size();
            double user2size = ratings.getUserRatingsRated(idUser2).size();

            double sorensenIndex = 2 * intersectionSize / (user1size + user2size);
            double conditionalProbability = intersectionSize / user1size;

            double similarity = conditionalProbability * sorensenIndex;

            if (Double.isInfinite(similarity)) {
                similarity = 0;
            } else if (Double.isNaN(similarity)) {
                similarity = 0;
            }
            return similarity;
        }).orElse(0.0);

        double entropyOfDifferences = innerSimilarity.similarity(commonRatings, ratings);

        if (Double.isFinite(entropyOfDifferences)) {
            ret = ret * entropyOfDifferences;
        }

        return ret;
    }

    @Override
    public boolean RSallowed(Class<? extends RecommenderSystemAdapter> rs) {
        return KnnMemoryBasedCFRS.class.isAssignableFrom(rs);
    }

    @Override
    public double similarity(DatasetLoader<? extends Rating> datasetLoader, int idUser1, int idUser2) {
        User user1 = datasetLoader.getUsersDataset().get(idUser1);
        User user2 = datasetLoader.getUsersDataset().get(idUser2);

        return similarity(datasetLoader, user1, user2);
    }

}
