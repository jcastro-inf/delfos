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
* @author Jorge Castro Gallardo
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

            Map<Integer, Integer> numRatingsUsers_byIterator = new TreeMap<Integer, Integer>();
            Map<Integer, Integer> numRatingsItems_byIterator = new TreeMap<Integer, Integer>();

            for (Rating r : ratingsDataset) {
                int idUser = r.idUser;
                int idItem = r.idItem;

                ratingsDataset.getRating(idUser, idItem);

                if (numRatingsUsers_byIterator.containsKey(idUser)) {
                    numRatingsUsers_byIterator.put(idUser, numRatingsUsers_byIterator.get(idUser) + 1);
                } else {
                    numRatingsUsers_byIterator.put(idUser, 1);
                }

                if (numRatingsItems_byIterator.containsKey(idItem)) {
                    numRatingsItems_byIterator.put(idItem, numRatingsItems_byIterator.get(idItem) + 1);
                } else {
                    numRatingsItems_byIterator.put(idItem, 1);
                }

            }

            for (int idUser : ratingsDataset.allUsers()) {
                int numRatingsUser_byGetRatings = ratingsDataset.getUserRatingsRated(idUser).size();
                int numRatingsUser_ByIterator = numRatingsUsers_byIterator.get(idUser);

                if (numRatingsUser_byGetRatings != numRatingsUser_ByIterator) {
                    Global.showWarning("El usuario " + idUser + " tiene:\n"
                            + "\t" + numRatingsUser_byGetRatings + " valoraciones según el método getUserRatingsRated(idUser) \n"
                            + "\t" + numRatingsUser_ByIterator + " valoraciones según el iterador");
                    errors++;
                }
            }

            final Set<Integer> allRatedItems = new TreeSet<Integer>(ratingsDataset.allRatedItems());

            for (int idItem : allRatedItems) {

                int numRatingsItem_byGetRatings = ratingsDataset.getItemRatingsRated(idItem).size();
                int numRatingsItem_ByIterator = numRatingsItems_byIterator.get(idItem);

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

                Set<Integer> AMinusB = new TreeSet<Integer>(numRatingsItems_byIterator.keySet());
                AMinusB.removeAll(allRatedItems);

                Set<Integer> BMinusA = new TreeSet<Integer>(allRatedItems);
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
