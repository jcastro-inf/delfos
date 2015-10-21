package delfos.common.aggregationoperators.userratingsaggregation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.common.aggregationoperators.AggregationOperator;
import delfos.common.aggregationoperators.Mean;
import delfos.common.aggregationoperators.MinimumValue;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.common.parameters.restriction.FloatParameter;

/**
 * Agrega las valoraciones de un grupo de usuarios sobre un producto indicado.
 * Esta técnica utiliza una técnica u otra dependiendo de la diferencia entre el
 * rating máximo y mínimo sobre el producto sobre el que se agrega, utilizando
 * leasy missery cuando la diferencia es mayor que el umbral establecido y
 * average cuando es menor.
 *
 * <p>
 * <p>
 * Xun Hu, Xiangwu Meng, Licai Wang: SVD-based group recommendation approaches:
 * an experimental study of Moviepilot. CAMRa '11 Proceedings of the 2nd
 * Challenge on Context-Aware Movie Recommendation Pages 23-28 ACM New York, NY,
 * USA ©2011
 *
* @author Jorge Castro Gallardo
 * @version 1.0 05-Julio-2013
 */
public class SwitchingAggregationLeastMisseryAndAverage extends ParameterOwnerAdapter implements UserRatingsAggregation {

    private final static long serialVersionUID = 1L;
    public static final Parameter threshold = new Parameter(
            "threshold",
            new FloatParameter(0, Float.MAX_VALUE, 2.0f));
    private final AggregationOperator leastMissery = new MinimumValue();
    private final AggregationOperator average = new Mean();

    @Override
    public Number aggregateRatings(RatingsDataset<? extends Rating> rd, Collection<Integer> users, int idItem)
            throws UserNotFound, ItemNotFound {

        List<Number> values = new ArrayList<Number>(users.size());

        Double max = null;
        Double min = null;

        for (int idUser : users) {
            Double rating = rd.getRating(idUser, idItem).getRatingValue().doubleValue();

            if (max == null) {
                max = rating.doubleValue();
            }
            if (min == null) {
                min = rating;
            }

            if (rating < min) {
                min = rating;
            }

            if (rating > max) {
                max = rating;
            }
            values.add(rating);
        }

        if (values.isEmpty()) {
            throw new IllegalArgumentException("The users do not have ratings over item " + idItem);
        }

        float thresholdValue = getThreshold();
        if (max - min >= thresholdValue) {
            return leastMissery.aggregateValues(values);
        } else {
            return average.aggregateValues(values);
        }
    }

    private float getThreshold() {
        return (Float) getParameterValue(threshold);
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
