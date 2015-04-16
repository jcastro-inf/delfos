package delfos.group.grs.preferenceaggregation.order;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import delfos.dataset.basic.rating.Rating;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;

/**
 *
* @author Jorge Castro Gallardo
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
                    this.ratings.put(rating.idItem, rating.ratingValue);
                    break;
                case USER:
                    this.ratings.put(rating.idUser, rating.ratingValue);
                    break;
                default:
                    throw new IllegalStateException("Unrecognized recommendation entity: " + entity);
            }
        }
        this.bipolar = false;
    }

    @Override
    public float preff(Integer e1, Integer e2) {
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
