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
import delfos.similaritymeasures.SimilarityMeasureAdapter;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 *
 * @author jcastro
 */
public class Jaccard extends SimilarityMeasureAdapter implements CollaborativeSimilarityMeasure {

    public double similarity(DatasetLoader<? extends Rating> datasetLoader, User user1, User user2) throws UserNotFound, CannotLoadRatingsDataset {

        Set<Integer> user1ratings = datasetLoader.getRatingsDataset().getUserRated(user1.getId());
        Set<Integer> user2ratings = datasetLoader.getRatingsDataset().getUserRated(user2.getId());

        Set<Integer> union = new TreeSet<>();
        union.addAll(user1ratings);
        union.addAll(user2ratings);

        Set<Integer> intersection = user1ratings.parallelStream()
                .filter(idItem -> user2ratings.contains(idItem))
                .collect(Collectors.toSet());

        double numerator = intersection.size();
        double denominator = union.size();
        double jaccard = numerator / denominator;

        return jaccard;
    }

    @Override
    public double similarity(Collection<CommonRating> commonRatings, RatingsDataset<? extends Rating> ratings) throws CouldNotComputeSimilarity {
        double intersectionSize = commonRatings.size();

        double ret = commonRatings.stream().findAny().map(commonRating -> {

            int idUser1 = commonRating.getIdR1();
            int idUser2 = commonRating.getIdR2();

            Set<Integer> user1ratings = ratings.getUserRated(idUser1);
            Set<Integer> user2ratings = ratings.getUserRated(idUser2);

            Set<Integer> union = new TreeSet<>();
            union.addAll(user1ratings);
            union.addAll(user2ratings);

            Set<Integer> intersection = user1ratings.parallelStream()
                    .filter(idItem -> user2ratings.contains(idItem))
                    .collect(Collectors.toSet());

            double numerator = intersection.size();
            double denominator = union.size();
            double jaccard = numerator / denominator;

            return jaccard;
        }).orElse(0.0);

        return ret;
    }

    @Override
    public boolean RSallowed(Class<? extends RecommenderSystemAdapter> rs) {
        return KnnMemoryBasedCFRS.class.isAssignableFrom(rs);
    }

}
