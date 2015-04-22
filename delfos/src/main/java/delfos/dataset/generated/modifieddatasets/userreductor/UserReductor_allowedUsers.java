package delfos.dataset.generated.modifieddatasets.userreductor;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RatingsDatasetAdapter;
import delfos.dataset.basic.rating.domain.Domain;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknow date
 *
 * @version 1.1 (21-01-2013) Ahora implementa de {@link RatingsDatasetAdapter}
 * @version 21-Enero-2014 Clase renombrada para claridad de su funcionamiento.
 *
 * @param <RatingType>
 */
public class UserReductor_allowedUsers<RatingType extends Rating> extends RatingsDatasetAdapter<RatingType> {

    private final RatingsDataset<RatingType> originalDataset;
    private final Set<Integer> allowedUsers;
    private TreeSet<Integer> allRatedItems;

    public UserReductor_allowedUsers(RatingsDataset<RatingType> originalDataset, Set<Integer> allowedUsers) {
        super();
        this.originalDataset = originalDataset;
        this.allowedUsers = allowedUsers;
    }

    private boolean isAllowed(int idUser) {
        return allowedUsers.contains(idUser);
    }

    @Override
    public RatingType getRating(int idUser, int idItem) throws UserNotFound, ItemNotFound {
        if (isAllowed(idUser)) {
            return originalDataset.getRating(idUser, idItem);
        } else {
            return null;
        }
    }

    @Override
    public Set<Integer> allUsers() {
        Set<Integer> ret = new TreeSet<>();
        for (int idUser : allowedUsers) {
            ret.add(idUser);
        }
        return ret;
    }

    @Override
    public Set<Integer> allRatedItems() {
        if (this.allRatedItems == null || allRatedItems.isEmpty()) {
            allRatedItems = new TreeSet<>();

            for (Integer idUser : allowedUsers) {
                try {
                    allRatedItems.addAll(originalDataset.getUserRated(idUser));
                } catch (UserNotFound ex) {
                    Global.showError(ex);
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                }
            }
        }

        return Collections.unmodifiableSet(allRatedItems);
    }

    @Override
    public Collection<Integer> getUserRated(Integer idUser) throws UserNotFound {
        if (isAllowed(idUser)) {
            return originalDataset.getUserRated(idUser);
        } else {
            return null;
        }
    }

    @Override
    public Collection<Integer> getItemRated(Integer idItem) throws ItemNotFound {

        Collection<Integer> ret = new LinkedList<>();
        for (int idUser : originalDataset.getItemRated(idItem)) {
            if (isAllowed(idUser)) {
                ret.add(idUser);
            }
        }
        return ret;
    }

    @Override
    public Map<Integer, RatingType> getUserRatingsRated(Integer idUser) throws UserNotFound {
        if (isAllowed(idUser)) {
            return originalDataset.getUserRatingsRated(idUser);
        } else {
            throw new UserNotFound(idUser);
        }
    }

    @Override
    public Map<Integer, RatingType> getItemRatingsRated(Integer idItem) throws ItemNotFound {
        Map<Integer, RatingType> ret = new TreeMap<>();

        for (int idUser : getItemRated(idItem)) {
            try {
                RatingType rating = originalDataset.getRating(idUser, idItem);
                ret.put(idUser, rating);
            } catch (UserNotFound ex) {
                Global.showError(ex);
                return null;
            }
        }
        return ret;
    }

    @Override
    public Domain getRatingsDomain() {
        return originalDataset.getRatingsDomain();
    }
}
