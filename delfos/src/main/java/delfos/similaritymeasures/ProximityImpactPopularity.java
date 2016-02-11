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
import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.rs.collaborativefiltering.knn.CommonRating;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.domain.Domain;
import delfos.rs.RecommenderSystemAdapter;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;

/**
 * Clase que implementa la medida del coseno para realizar una medida de
 * similitud de dos vectores
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class ProximityImpactPopularity extends SimilarityMeasureAdapter implements CollaborativeSimilarityMeasure {

    private boolean agree(double r1, double r2, Domain rc) {
        double mean = rc.mean().doubleValue();
        return (r1 > mean && r2 > mean) || (r1 < mean && r2 < mean);
    }

    private double Proximity(double r1, double r2, Domain rc) {
        double ret = (2 * (rc.max().doubleValue() - rc.min().doubleValue()) + 1);
        if (agree(r1, r2, rc)) {
            ret = (double) Math.pow(ret - Math.abs(r1 - r2), 2);
        } else {
            ret = (double) Math.pow(ret - 2 * Math.abs(r1 - r2), 2);
        }

//        if(ret < 0 || ret > 1){
//            Global.showWarning("Error de proximity");
//        }
        return ret;
    }

    private double Impact(double r1, double r2, Domain rc) {
        double Rmed = rc.mean().doubleValue();
        double ret = (Math.abs(r1 - Rmed) + 1) * (Math.abs(r2 - Rmed) + 1);
        if (!agree(r1, r2, rc)) {
            ret = 1 / ret;
        }
//        if(ret < 0 || ret > 1){
//            Global.showWarning("Error de Impact");
//        }
        return ret;
    }

    private double Popularity(double r1, double r2, double mean) {
        double ret;
        if ((r1 > mean && r2 > mean) || (r1 < mean && r2 < mean)) {
            ret = (double) (1 + Math.pow(((r1 + r2) / 2) - mean, 2));
        } else {
            ret = 1;
        }
//        if(ret < 0 || ret > 1){
//            Global.showWarning("Error de Popularity");
//        }

        return ret;
    }

    @Override
    public float similarity(Collection<CommonRating> commonRatings, RatingsDataset<? extends Rating> ratings) throws CouldNotComputeSimilarity {
        double ret = 0;
        for (CommonRating commonRating : commonRatings) {
            if (commonRating.getCommonEntity().equals(RecommendationEntity.ITEM)) {
                double proximity = Proximity(commonRating.getRating1(), commonRating.getRating2(), ratings.getRatingsDomain());
                if (proximity != 0) {
                    double impact = Impact(commonRating.getRating1(), commonRating.getRating2(), ratings.getRatingsDomain());
                    if (impact != 0) {
                        try {
                            double popularity = Popularity(commonRating.getRating1(), commonRating.getRating2(), ratings.getMeanRatingItem(commonRating.getIdCommon()));
                            if (popularity != 0) {
                                ret += proximity * impact * popularity;
                                //Global.showMessage("R1:"+commonRating.getRating1()+"\tR2:"+commonRating.getRating2()+"\t"+proximity+"\t"+impact+"\t"+popularity+"\n");
                            }
                        } catch (ItemNotFound ex) {
                            throw new CouldNotComputeSimilarity(ex.getMessage());
                        }
                    }
                }
            } else {
                throw new UnsupportedOperationException("This measure is meant to be used only in User-based collaborative filtering");
            }
        }
        return (float) ret;
    }

    @Override
    public boolean RSallowed(Class<? extends RecommenderSystemAdapter> rs) {
        return KnnMemoryBasedCFRS.class.isAssignableFrom(rs);
    }
}
