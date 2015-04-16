package delfos.rs.contentbased.vsm.multivalued.entropydependence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import delfos.dataset.basic.features.Feature;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 14-oct-2013
 */
public class EntropyDependenceCBRSModel extends TreeMap<Integer, EntropyDependenceCBRSItemProfile> implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * Inter user weights.
     */
    private final Map<Feature, Number> _weights;

    public EntropyDependenceCBRSModel(Map<Integer, EntropyDependenceCBRSItemProfile> items, Map<Feature, Number> weights) {
        super(items);
        this._weights = weights;
    }

    public void getItemProfile(int idItem) {
        if (this.containsKey(idItem)) {
            this.get(idItem);
        } else {
            throw new IllegalArgumentException("The item " + idItem + " has no profile in the model.");
        }
    }

    /**
     * Devuelve la ponderación (inter-user) de la característica indicada.
     *
     * @param feature Característica para la que se busca la ponderación.
     * @return Ponderación de la característica. Si el perfil no contiene la
     * característica, devuelve cero.
     */
    public float getEntropy(Feature feature) {
        if (_weights.containsKey(feature)) {
            return _weights.get(feature).floatValue();
        } else {
            throw new IllegalArgumentException("The model does not have the feature " + feature);
        }
    }

    protected List<Feature> getAllFeatures() {
        return new ArrayList<Feature>(_weights.keySet());
    }
}
