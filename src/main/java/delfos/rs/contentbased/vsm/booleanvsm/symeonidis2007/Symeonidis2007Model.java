/*
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package delfos.rs.contentbased.vsm.booleanvsm.symeonidis2007;

import delfos.rs.contentbased.vsm.booleanvsm.BooleanFeaturesTransformation;
import delfos.rs.contentbased.vsm.booleanvsm.SparseVector;
import delfos.rs.contentbased.vsm.booleanvsm.profile.BooleanUserProfile;
import java.io.Serializable;
import java.util.TreeMap;

/**
 * Almacena el modelo del sistema {@link Symeonidis2007FeatureWeighted}, que se compone de los perfiles de producto, los
 * perfiles de usuario y de las ponderaciones IUF.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 14-Octubre-2013
 */
public class Symeonidis2007Model implements Serializable {

    private static final long serialVersionUID = -3387516993124229948L;
    private SparseVector<Long> allIUF;
    private final BooleanFeaturesTransformation booleanFeaturesTransformation;
    private final TreeMap<Long, Symeonidis2007UserProfile> userProfiles;
    private final TreeMap<Long, SparseVector<Long>> itemProfiles;

    public Symeonidis2007Model(BooleanFeaturesTransformation booleanFeaturesTransformation) {
        this.userProfiles = new TreeMap<>();
        this.itemProfiles = new TreeMap<>();
        this.booleanFeaturesTransformation = booleanFeaturesTransformation;
    }

    public BooleanFeaturesTransformation getBooleanFeaturesTransformation() {
        return booleanFeaturesTransformation;
    }

    public void setAllIuf(SparseVector<Long> allIuf) {
        this.allIUF = allIuf.clone();
    }

    public SparseVector<Long> getAllIUF() {
        return allIUF.clone();
    }

    void putItemProfile(long idItem, SparseVector<Long> itemProfile) {
        if (itemProfiles.containsKey(idItem)) {
            throw new IllegalArgumentException("The item " + idItem + " profile had already been assigned the model.");
        } else {
            itemProfiles.put(idItem, itemProfile);
        }
    }

    SparseVector<Long> getItemProfile(long idItem) {
        if (itemProfiles.containsKey(idItem)) {
            return itemProfiles.get(idItem).clone();
        } else {
            throw new IllegalArgumentException("The item " + idItem + " profile not exists");
        }
    }

    void putUserProfile(long idUser, Symeonidis2007UserProfile itemProfile) {
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
