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
package delfos.similaritymeasures;

import java.util.Collection;
import java.util.Iterator;
import delfos.ERROR_CODES;
import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.rs.collaborativefiltering.knn.CommonRating;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.RecommenderSystemAdapter;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;

/**
 * Medida de similitud propuesta en INS-D-12-446 de Information Sciences Title:
 * A Range-based Similarity Measure for Collaborative Filtering Systems Author:
 * Soojung Lee Keywords: similarity measure; web personalization; collaborative
 * filtering; recommender system
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class RatingRangeBased extends SimilarityMeasureAdapter implements CollaborativeSimilarityMeasure {

    private final float alpha = 6;

    private float pos(float ratingMin, float ratingMax, float rating) {
        float ret;
        if (ratingMax > ratingMin) {
            ret = (rating - ratingMin) / (ratingMax - ratingMin);
        } else {
            ret = 1;
        }
        return ret;
    }

    private float Dpos(Collection<CommonRating> commonRatings, RatingsDataset<? extends Rating> ratings) throws CouldNotComputeSimilarity {
        double ret = 0;

        float minU = (float) ratings.getRatingsDomain().max();
        float maxU = (float) ratings.getRatingsDomain().min();
        float minV = (float) ratings.getRatingsDomain().max();
        float maxV = (float) ratings.getRatingsDomain().min();
        CommonRating next = commonRatings.iterator().next();

        if (!next.getCommonEntity().equals(RecommendationEntity.ITEM)) {
            throw new UnsupportedOperationException("This measure is meant to be used only in User-based collaborative filtering");
        }
        try {
            //minimo y maximo del usuario 1
            for (Iterator<? extends Rating> it = ratings.getUserRatingsRated(next.getIdR1()).values().iterator(); it.hasNext();) {
                float r = it.next().getRatingValue().floatValue();
                if (r < minU) {
                    minU = r;
                }
                if (r > maxU) {
                    maxU = r;
                }
            }
        } catch (UserNotFound ex) {
            ERROR_CODES.USER_NOT_FOUND.exit(ex);
        }
        try {
            //minimo y maximo del usuario 2
            for (Iterator<? extends Rating> it = ratings.getUserRatingsRated(next.getIdR2()).values().iterator(); it.hasNext();) {
                float r = it.next().getRatingValue().floatValue();
                if (r < minV) {
                    minV = r;
                }
                if (r > maxV) {
                    maxV = r;
                }
            }
        } catch (UserNotFound ex) {
            ERROR_CODES.USER_NOT_FOUND.exit(ex);
        }
        for (CommonRating commonRating : commonRatings) {
            if (commonRating.getCommonEntity().equals(RecommendationEntity.ITEM)) {

                float aporte = 1.0f / commonRatings.size();

                aporte = aporte * Math.abs(pos(minU, maxU, commonRating.getRating1()) - pos(minV, maxV, commonRating.getRating2()));
                ret += aporte;
            } else {
                throw new UnsupportedOperationException("This measure is meant to be used only in User-based collaborative filtering");
            }
        }

        return (float) ret;
    }

    @Override
    public float similarity(Collection<CommonRating> commonRatings, RatingsDataset<? extends Rating> ratings) throws CouldNotComputeSimilarity {
        if (commonRatings.isEmpty()) {
            throw new CouldNotComputeSimilarity("No common ratings to compute similarity");
        }
        float ret = (float) (2 / (1 + Math.pow(Math.E, alpha * Dpos(commonRatings, ratings))));
        return ret;
    }

    @Override
    public boolean RSallowed(Class<? extends RecommenderSystemAdapter> rs) {
        return KnnMemoryBasedCFRS.class.isAssignableFrom(rs);
    }
}
