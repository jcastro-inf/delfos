package delfos.rs.contentbased.vsm.booleanvsm.tfidf;

import java.util.TreeMap;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import delfos.rs.contentbased.vsm.booleanvsm.BooleanFeaturesTransformation;

/**
 * Almacena el modelo del sistema {@link TfIdfCBRS}. En su implementación de
 * {@link TreeMap} almacena los perfiles de los productos.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 14-oct-2013
 */
public class TfIdfCBRSModel extends TreeMap<Integer, SparseVector> {

    private static final long serialVersionUID = -3387516993124229948L;

    private final BooleanFeaturesTransformation booleanFeaturesTransformation;
    private SparseVector allIUF;

    public TfIdfCBRSModel(BooleanFeaturesTransformation booleanFeaturesTransformation) {
        this.booleanFeaturesTransformation = booleanFeaturesTransformation;
    }

    public BooleanFeaturesTransformation getBooleanFeaturesTransformation() {
        return booleanFeaturesTransformation;
    }

    public void setAllIuf(SparseVector allIuf) {
        MutableSparseVector aux = booleanFeaturesTransformation.newProfile();
        aux.fill(0);
        aux.add(allIuf);
        this.allIUF = aux;
    }

    public SparseVector getAllIUF() {
        return allIUF;
    }
}
