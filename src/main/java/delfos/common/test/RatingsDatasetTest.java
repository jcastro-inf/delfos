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
package delfos.common.test;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.Global;

/**
 * Test para comprobar que funciona correctamente un dataset.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 15-Julio-2013
 */
public class RatingsDatasetTest {

    /**
     * Test of getRating method, of class RatingsDataset.
     *
     * @param ratingsDataset
     */
    public static void testIteratorEqualsOtherMethods(RatingsDataset<Rating> ratingsDataset) {

        int errors = 0;
        try {

            Map<Long, Long> numRatingsUsers_byIterator = new TreeMap<Long, Long>();
            Map<Long, Long> numRatingsItems_byIterator = new TreeMap<Long, Long>();

            for (Rating r : ratingsDataset) {
                long idUser = r.getIdUser();
                long idItem = r.getIdItem();

                ratingsDataset.getRating(idUser, idItem);

                if (numRatingsUsers_byIterator.containsKey(idUser)) {
                    numRatingsUsers_byIterator.put(idUser, numRatingsUsers_byIterator.get(idUser) + 1);
                } else {
                    numRatingsUsers_byIterator.put(idUser, 1l);
                }

                if (numRatingsItems_byIterator.containsKey(idItem)) {
                    numRatingsItems_byIterator.put(idItem, numRatingsItems_byIterator.get(idItem) + 1);
                } else {
                    numRatingsItems_byIterator.put(idItem, 1l);
                }

            }

            for (long idUser : ratingsDataset.allUsers()) {
                long numRatingsUser_byGetRatings = ratingsDataset.getUserRatingsRated(idUser).size();
                long numRatingsUser_ByIterator = numRatingsUsers_byIterator.get(idUser);

                if (numRatingsUser_byGetRatings != numRatingsUser_ByIterator) {
                    Global.showWarning("El usuario " + idUser + " tiene:\n"
                            + "\t" + numRatingsUser_byGetRatings + " valoraciones según el método getUserRatingsRated(idUser) \n"
                            + "\t" + numRatingsUser_ByIterator + " valoraciones según el iterador");
                    errors++;
                }
            }

            final Set<Long> allRatedItems = new TreeSet<Long>(ratingsDataset.allRatedItems());

            for (long idItem : allRatedItems) {

                long numRatingsItem_byGetRatings = ratingsDataset.getItemRatingsRated(idItem).size();
                long numRatingsItem_ByIterator = numRatingsItems_byIterator.get(idItem);

                if (numRatingsItem_byGetRatings != numRatingsItem_ByIterator) {
                    Global.showWarning("El producto " + idItem + " tiene:\n"
                            + "\t" + numRatingsItem_byGetRatings + " valoraciones según el método getItemRatingsRated(idItem) \n"
                            + "\t" + numRatingsItem_ByIterator + " valoraciones según el iterador");
                    errors++;
                }
            }

            if (numRatingsItems_byIterator.keySet().equals(allRatedItems)) {
            } else {
                errors++;
                Global.showWarning("El conjunto de items valorados es distinto.");

                Set<Long> AMinusB = new TreeSet<Long>(numRatingsItems_byIterator.keySet());
                AMinusB.removeAll(allRatedItems);

                Set<Long> BMinusA = new TreeSet<Long>(allRatedItems);
                BMinusA.removeAll(numRatingsItems_byIterator.keySet());

                if (!AMinusB.isEmpty()) {
                    Global.showWarning("Produtos que no están en el allItemsRated --> " + AMinusB.toString());
                }
                if (!BMinusA.isEmpty()) {
                    Global.showWarning("Produtos que no están según el iterador   --> " + BMinusA.toString());
                }
            }

            if (numRatingsUsers_byIterator.keySet().equals(ratingsDataset.allUsers())) {
            } else {
                errors++;
                Global.showWarning("El conjunto de usuarios es distinto.");
            }

        } catch (UserNotFound ex) {
            ERROR_CODES.USER_NOT_FOUND.exit(ex);
        } catch (ItemNotFound ex) {
            ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
        }
        if (errors == 0) {
            Global.showWarning("Finished the dataset test.");
        } else {
            Global.showWarning("Finished the dataset test with " + errors + " errors.");
        }
    }
}
