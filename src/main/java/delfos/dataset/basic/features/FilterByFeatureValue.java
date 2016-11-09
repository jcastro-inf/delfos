/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delfos.dataset.basic.features;

import java.util.function.Predicate;

/**
 * Filters the entities with features to keep only those that have the feature and also have the specified value.
 *
 * @author jcastro
 */
public class FilterByFeatureValue implements Predicate<EntityWithFeatures> {

    private final Feature feature;
    private final Object featureValue;

    /**
     *
     * @param feature Feature that entities must have.
     * @param featureValue Feature value that entities must have.
     */
    public FilterByFeatureValue(Feature feature, Object featureValue) {
        this.feature = feature;
        this.featureValue = featureValue;
    }

    @Override
    public boolean test(EntityWithFeatures entityWithFeatures) {
        if (!entityWithFeatures.getFeatures().contains(feature)) {
            return false;
        }

        final Object featureValueOfItem = entityWithFeatures.getFeatureValue(feature);

        switch (feature.getType()) {
            case Numerical:
                double valueOfItemDouble = ((Number) featureValueOfItem).doubleValue();
                double valueSpecifiedDouble = ((Number) featureValue).doubleValue();

                boolean equals = Double.compare(valueOfItemDouble, valueSpecifiedDouble) == 0;

                if (equals) {
                    return true;
                } else {
                    return false;
                }

            default:
                return featureValueOfItem.equals(featureValue);
        }

    }

    public Feature getFeature() {
        return feature;
    }

    public Object getFeatureValue() {
        return featureValue;
    }

}
