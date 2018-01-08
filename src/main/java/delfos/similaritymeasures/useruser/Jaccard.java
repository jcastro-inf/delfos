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
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.similaritymeasures.CollaborativeSimilarityMeasure;
import delfos.similaritymeasures.SimilarityMeasureAdapter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 *
 * @author jcastro
 */
public class Jaccard extends SimilarityMeasureAdapter implements CollaborativeSimilarityMeasure {

    public double similarity(DatasetLoader<? extends Rating> datasetLoader, User user1, User user2) throws UserNotFound, CannotLoadRatingsDataset {

        Set<Long> user1ratings = datasetLoader.getRatingsDataset().getUserRated(user1.getId());
        Set<Long> user2ratings = datasetLoader.getRatingsDataset().getUserRated(user2.getId());

        Set<Long> union = new TreeSet<>();
        union.addAll(user1ratings);
        union.addAll(user2ratings);

        Set<Long> intersection = user1ratings.parallelStream()
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
        if(intersectionSize==0)
            return Double.NaN;

        RecommendationEntity ratingEntity = commonRatings.stream().findAny().map(commonRating -> commonRating.getRatingEntity()).orElse(null);

        long entity1 = commonRatings.stream().findAny().map(cr-> cr.getIdR1()).orElse(-1l);
        long entity2 = commonRatings.stream().findAny().map(cr-> cr.getIdR2()).orElse(-1l);

        Set<Long> intersection= new HashSet<>();
        Set<Long> union =new HashSet<>();
        switch (ratingEntity){
            case ITEM:
                intersection.addAll(ratings.getItemRated(entity1));
                intersection.retainAll(ratings.getItemRated(entity2));
                union.addAll(ratings.getItemRated(entity1));
                union.addAll(ratings.getItemRated(entity2));
                break;
            case USER:
                intersection.addAll(ratings.getUserRated(entity1));
                intersection.retainAll(ratings.getUserRated(entity2));
                union.addAll(ratings.getUserRated(entity1));
                union.addAll(ratings.getUserRated(entity2));
                break;
            default:
                throw new IllegalStateException();
        }

        double numerator = intersection.size();
        double denominator = union.size();
        double jaccard = numerator / denominator;

        return jaccard;
    }

    @Override
    public boolean RSallowed(Class<? extends RecommenderSystemAdapter> rs) {
        return KnnMemoryBasedCFRS.class.isAssignableFrom(rs);
    }

}
