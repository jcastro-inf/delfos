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
package delfos.rs.collaborativefiltering.svd.parallel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import delfos.common.Global;
import delfos.common.exceptions.ratings.NotEnoughtItemInformation;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;

/**
 *
 * @version 24-jul-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class ParallelSVDModel implements Serializable {

    private static final long serialVersionUID = 43L;

    private final Map<Integer, ArrayList<Double>> usersFeatures;
    private final Map<Integer, ArrayList<Double>> itemsFeatures;

    ParallelSVDModel(Map<Integer, ArrayList<Double>> usersFeatures, Map<Integer, ArrayList<Double>> itemsFeatures) {
        this.usersFeatures = usersFeatures;
        this.itemsFeatures = itemsFeatures;
    }

    public boolean containsUser(int idUser) {
        return usersFeatures.containsKey(idUser);
    }

    public boolean containsItem(int idItem) {
        return itemsFeatures.containsKey(idItem);
    }

    void setUserFeatureValue(int idUser, int feature, double value) {
        usersFeatures.get(idUser).set(feature, value);
    }

    void setItemFeatureValue(int idItem, int feature, double value) {
        itemsFeatures.get(idItem).set(feature, value);
    }

    public ArrayList<Double> getUserFeatures(int idUser) {
        ArrayList<Double> userFeatures = new ArrayList<>(usersFeatures.get(idUser));
        return userFeatures;
    }

    public ArrayList<Double> getItemFeatures(int idItem) {
        ArrayList<Double> itemFeatures = new ArrayList<>(itemsFeatures.get(idItem));
        return itemFeatures;
    }

    private final Set<Integer> itemsWarned = new TreeSet<>();

    public void warningItemNotInModel(int idItem, String message, NotEnoughtItemInformation ex) {
        if (!itemsWarned.contains(idItem)) {
            Global.showWarning(message);
            itemsWarned.add(idItem);
        }
    }

    private final Set<Integer> usersWarned = new TreeSet<>();

    public void warningUserNotInModel(int idUser, String message, NotEnoughtUserInformation ex) {
        if (!usersWarned.contains(idUser)) {
            Global.showWarning(message);
            usersWarned.add(idUser);
        }
    }
}
