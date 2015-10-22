package delfos.group.experiment.validation.recommendableitems;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Objeto que calcula los items que se pueden recomendar a un grupo de usuarios,
 * dependiendo de los items que cada miembro ha valorado. Esta validación
 * considera que sólo se deben recomendar productos que ningún usuario haya
 * experimentado previamente.
 *
* @author Jorge Castro Gallardo
 */
public class NeverRatedItems extends RecomendableItemTechnique {

    @Override
    public Collection<Integer> getRecommendableItems(GroupOfUsers groupOfUsers, RatingsDataset<? extends Rating> ratingsDataset, ContentDataset contentDataset) {
        Set<Integer> ret = new TreeSet<Integer>(contentDataset.allID());

        for (int idUser : groupOfUsers.getIdMembers()) {
            try {
                ret.removeAll(ratingsDataset.getUserRated(idUser));
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }
        return ret;
    }
}
