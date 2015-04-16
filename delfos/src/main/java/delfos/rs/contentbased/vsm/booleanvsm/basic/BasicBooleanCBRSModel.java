package delfos.rs.contentbased.vsm.booleanvsm.basic;

import java.util.TreeMap;
import org.grouplens.lenskit.vectors.SparseVector;
import delfos.rs.contentbased.vsm.booleanvsm.BooleanFeaturesTransformation;

/**
 * Almacena el modelo del sistema {@link BasicBooleanCBRS}.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 09-oct-2013
 */
public class BasicBooleanCBRSModel extends TreeMap<Integer, SparseVector> {

    private static final long serialVersionUID = 1L;
    protected BooleanFeaturesTransformation booleanFeaturesTransformation;

    public BasicBooleanCBRSModel(BooleanFeaturesTransformation booleanFeaturesTransformation) {
        this.booleanFeaturesTransformation = booleanFeaturesTransformation;
    }

    public BooleanFeaturesTransformation getBooleanFeaturesTransformation() {
        return booleanFeaturesTransformation;
    }
}
