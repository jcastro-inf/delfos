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
package delfos.dataset.generated.modifieddatasets.userreductor;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RatingsDatasetAdapter;
import delfos.dataset.basic.rating.domain.Domain;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
    private final Set<Long> allowedUsers;
    private TreeSet<Long> allRatedItems;

    public UserReductor_allowedUsers(RatingsDataset<RatingType> originalDataset, Set<Long> allowedUsers) {
        super();
        this.originalDataset = originalDataset;
        this.allowedUsers = allowedUsers;
    }

    private boolean isAllowed(long idUser) {
        return allowedUsers.contains(idUser);
    }

    @Override
    public RatingType getRating(long idUser, long idItem) throws UserNotFound, ItemNotFound {
        if (isAllowed(idUser)) {
            return originalDataset.getRating(idUser, idItem);
        } else {
            return null;
        }
    }

    @Override
    public Set<Long> allUsers() {
        Set<Long> ret = new TreeSet<>();
        for (long idUser : allowedUsers) {
            ret.add(idUser);
        }
        return ret;
    }

    @Override
    public Set<Long> allRatedItems() {
        if (this.allRatedItems == null || allRatedItems.isEmpty()) {
            allRatedItems = new TreeSet<>();

            for (Long idUser : allowedUsers) {
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
    public Set<Long> getUserRated(long idUser) throws UserNotFound {
        if (isAllowed(idUser)) {
            return originalDataset.getUserRated(idUser);
        } else {
            return null;
        }
    }

    @Override
    public Set<Long> getItemRated(long idItem) throws ItemNotFound {

        Set<Long> ret = new TreeSet<>();
        for (long idUser : originalDataset.getItemRated(idItem)) {
            if (isAllowed(idUser)) {
                ret.add(idUser);
            }
        }
        return ret;
    }

    @Override
    public Map<Long, RatingType> getUserRatingsRated(long idUser) throws UserNotFound {
        if (isAllowed(idUser)) {
            return originalDataset.getUserRatingsRated(idUser);
        } else {
            throw new UserNotFound(idUser);
        }
    }

    @Override
    public Map<Long, RatingType> getItemRatingsRated(long idItem) throws ItemNotFound {
        Map<Long, RatingType> ret = new TreeMap<>();

        for (long idUser : getItemRated(idItem)) {
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
