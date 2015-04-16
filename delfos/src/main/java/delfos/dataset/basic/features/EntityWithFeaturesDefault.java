package delfos.dataset.basic.features;

/**
 * Determina los métodos estáticos que una entidad con características necesita
 * consultar.
 *
* @author Jorge Castro Gallardo
 *
 * @version 28-Enero-2014
 */
public class EntityWithFeaturesDefault {

    private EntityWithFeaturesDefault() {
    }

    /**
     * Comprueba que los vectores son correctos.
     *
     * @param features
     * @param values
     */
    public final static void checkFeatureAndFeatureValuesArrays(Feature[] features, Object[] values) {
        if (features == null) {
            throw new IllegalArgumentException("The feature vector cannot be null.");
        }

        if (values == null) {
            throw new IllegalArgumentException("The feature values vector cannot be null.");
        }

        if (features.length != values.length) {
            throw new IllegalArgumentException("The feature vector cannot have a different lenght than feature values vector "
                    + "(" + features.length + " != " + values.length + ").");
        }

        for (int i = 0; i < features.length; i++) {
            Feature feature = features[i];
            Object value = values[i];

            if (feature == null) {
                throw new IllegalArgumentException("Cannot have a null feature (position=" + i + ").");
            }

            if (value == null) {
                //No pasa nada, se considera que no tiene valor asociado a la característica
            }
        }
    }
}
