package delfos.rs.contentbased.vsm.booleanvsm.symeonidis2007;

import java.io.Serializable;
import java.util.TreeMap;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import delfos.rs.contentbased.vsm.booleanvsm.BooleanFeaturesTransformation;
import delfos.rs.contentbased.vsm.booleanvsm.profile.BooleanUserProfile;

/**
 * Almacena el modelo del sistema {@link Symeonidis2007FeatureWeighted}, que se
 * compone de los perfiles de producto, los perfiles de usuario y de las
 * ponderaciones IUF.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 14-Octubre-2013
 */
public class Symeonidis2007Model implements Serializable {

    private static final long serialVersionUID = -3387516993124229948L;
    private SparseVector allIUF;
    private BooleanFeaturesTransformation booleanFeaturesTransformation;
    private final TreeMap<Integer, Symeonidis2007UserProfile> userProfiles;
    private final TreeMap<Integer, SparseVector> itemProfiles;

    public Symeonidis2007Model(BooleanFeaturesTransformation booleanFeaturesTransformation) {
        this.userProfiles = new TreeMap<Integer, Symeonidis2007UserProfile>();
        this.itemProfiles = new TreeMap<Integer, SparseVector>();
        this.booleanFeaturesTransformation = booleanFeaturesTransformation;
    }

    public BooleanFeaturesTransformation getBooleanFeaturesTransformation() {
        return booleanFeaturesTransformation;
    }

    public void setAllIuf(SparseVector allIuf) {
        this.allIUF = allIuf.mutableCopy().immutable();
    }

    public SparseVector getAllIUF() {
        return allIUF.immutable();
    }

    void putItemProfile(int idItem, SparseVector itemProfile) {
        if (itemProfiles.containsKey(idItem)) {
            throw new IllegalArgumentException("The item " + idItem + " profile had already been assigned the model.");
        } else {
            itemProfiles.put(idItem, itemProfile);
        }
    }

    SparseVector getItemProfile(int idItem) {
        if (itemProfiles.containsKey(idItem)) {
            return itemProfiles.get(idItem).immutable();
        } else {
            throw new IllegalArgumentException("The item " + idItem + " profile not exists");
        }
    }

    void putUserProfile(int idUser, Symeonidis2007UserProfile itemProfile) {
        if (userProfiles.containsKey(idUser)) {
            throw new IllegalArgumentException("The user " + idUser + " profile had already been assigned the model.");
        } else {
            userProfiles.put(idUser, itemProfile);
        }
    }

    BooleanUserProfile getUserProfile(int idUser) {
        if (userProfiles.containsKey(idUser)) {
            return userProfiles.get(idUser);
        } else {
            throw new IllegalArgumentException("The user " + idUser + " profile not exists");
        }
    }

    int numOfItemProfiles() {
        return itemProfiles.size();
    }

    Iterable<Symeonidis2007UserProfile> userProfiles() {
        return userProfiles.values();
    }
}
