package delfos.rs.contentbased.vsm.booleanvsm;

import delfos.dataset.basic.features.Feature;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 25-Noviembre-2013
 */
public class FeatureValue implements Comparable<Object> {

    public final Feature feature;
    public final Object value;

    public FeatureValue(Feature feature, Object value) {
        this.feature = feature;
        this.value = value;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof FeatureValue) {
            FeatureValue featureValue = (FeatureValue) o;
            int compareFeatures = this.feature.compareTo(featureValue.feature);
            if (compareFeatures == 0) {
                //Las características son iguales, comparo los valores
                Comparable<Object> thisValue = (Comparable<Object>) this.value;
                Comparable<Object> otherValue = (Comparable<Object>) featureValue.value;

                int compareValues = thisValue.compareTo(otherValue);
                return compareValues;
            } else {
                return compareFeatures;
            }

        } else {
            throw new IllegalArgumentException("Cannot compare with " + o);
        }
    }
}
