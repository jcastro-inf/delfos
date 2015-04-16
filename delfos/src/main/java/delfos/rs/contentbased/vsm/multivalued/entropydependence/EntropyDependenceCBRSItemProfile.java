package delfos.rs.contentbased.vsm.multivalued.entropydependence;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import delfos.dataset.basic.features.Feature;
import delfos.rs.contentbased.vsm.multivalued.profile.MultivaluedItemProfile;

/**
 * Almacena el perfil de un producto.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 14-oct-2013
 */
public class EntropyDependenceCBRSItemProfile implements MultivaluedItemProfile {

    private static final long serialVersionUID = 1L;
    private final int idItem;
    private final Map<Feature, Object> featureValues;

    private EntropyDependenceCBRSItemProfile() {
        idItem = Integer.MIN_VALUE;
        featureValues = null;
    }

    public EntropyDependenceCBRSItemProfile(int idItem, Map<Feature, Object> featureValue) {
        this.featureValues = new TreeMap<Feature, Object>(featureValue);
        this.idItem = idItem;
    }

    @Override
    public int getId() {
        return idItem;
    }

    @Override
    public Iterable<Feature> getFeatures() {
        return new ArrayList<Feature>(featureValues.keySet());
    }

    @Override
    public Object getFeatureValue(Feature feature) {
        if (featureValues.containsKey(feature)) {
            return featureValues.get(feature);
        } else {
            throw new IllegalArgumentException("The item '" + idItem + "' does not have the feature " + feature);
        }
    }

    @Override
    public String toString() {
        return featureValues.toString();
    }
}
