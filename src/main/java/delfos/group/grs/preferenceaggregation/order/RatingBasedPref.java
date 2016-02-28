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
package delfos.group.grs.preferenceaggregation.order;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import delfos.dataset.basic.rating.Rating;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class RatingBasedPref implements Preff<Integer> {

    private final Map<Integer, Number> ratings;
    private final boolean bipolar;

    public RatingBasedPref(Map<Integer, Number> ratings, boolean bipolar) {
        this.ratings = ratings;
        this.bipolar = bipolar;
    }

    public RatingBasedPref(Map<Integer, Number> ratings) {
        this.ratings = ratings;
        this.bipolar = false;
    }

    /**
     * Crea la estructura a partir de una colección de ratings. Hay que indicar
     * si se indexa por usuarios o por productos, por medio del parámetro
     * entity.
     *
     * @param entity
     * @param ratings
     */
    public RatingBasedPref(RecommendationEntity entity, Collection<? extends Rating> ratings) {
        this.ratings = new TreeMap<Integer, Number>();
        for (Rating rating : ratings) {
            switch (entity) {
                case ITEM:
                    this.ratings.put(rating.getIdItem(), rating.getRatingValue());
                    break;
                case USER:
                    this.ratings.put(rating.getIdUser(), rating.getRatingValue());
                    break;
                default:
                    throw new IllegalStateException("Unrecognized recommendation entity: " + entity);
            }
        }
        this.bipolar = false;
    }

    @Override
    public double preff(Integer e1, Integer e2) {
        if (!ratings.containsKey(e1)) {
            throw new IllegalArgumentException("Element '" + e1 + "' not in rating set");
        }

        if (!ratings.containsKey(e2)) {
            throw new IllegalArgumentException("Element '" + e2 + "' not in rating set");
        }

        double r1 = ratings.get(e1).doubleValue();
        double r2 = ratings.get(e2).doubleValue();
        if (r1 > r2) {
            return 1;
        } else {
            if (r1 < r2) {
                return 0;
            } else {
                if (bipolar) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }
}
