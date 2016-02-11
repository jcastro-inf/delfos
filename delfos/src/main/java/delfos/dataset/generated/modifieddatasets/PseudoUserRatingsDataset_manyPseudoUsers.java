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
package delfos.dataset.generated.modifieddatasets;

import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RatingsDatasetAdapter;
import delfos.dataset.basic.rating.domain.Domain;
import delfos.dataset.generated.modifieddatasets.userreductor.UserReductor_allowedUsers;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Dataset utilizado para añadir un usuario que no existe realmente.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.1 (21-01-2013) Ahora implementa de {@link RatingsDatasetAdapter}
 * @version 21-Enero-2014 Ahora también se permite eliminar usuarios, para dar
 * soporte a GRS.
 * @param <RatingType>
 */
public class PseudoUserRatingsDataset_manyPseudoUsers<RatingType extends Rating> extends RatingsDatasetAdapter<RatingType> {

    private final RatingsDataset<RatingType> constructionOriginalDataset;

    private RatingsDataset<RatingType> workingDataset;
    private RatingsDataset<RatingType> pseudoRatings = new BothIndexRatingsDataset<>();

    private Set<Integer> ratedItems = null;

    private static final int valueToAddToIdUserToMakeIdPseudoUser = 88000000;

    public PseudoUserRatingsDataset_manyPseudoUsers(RatingsDataset<? extends RatingType> ratingsDataset) {
        super();
        constructionOriginalDataset = (RatingsDataset<RatingType>) ratingsDataset;

        workingDataset = constructionOriginalDataset;
    }

    /**
     * Establece las valoraciones del pseudo usuario. Añade estas valoracioens a
     * las del dataset original
     *
     * @param userRatings Valoraciones que se añaden
     * @param forbiddenUsers Conjunto de usuarios que se eliminan del dataset
     * final.
     * @return id del usuario que se genera para almacenar las valoraciones
     */
    public synchronized int setPseudoUserRatings(Map<Integer, RatingType> userRatings, Collection<Integer> forbiddenUsers) {

        if (userRatings == null) {
            throw new IllegalArgumentException("No se puede crear un usuario sin valoraciones.");
        }

        //Obtengo el id de pseudo-usuario.
        int idPseudoUser = forbiddenUsers.iterator().next() + valueToAddToIdUserToMakeIdPseudoUser;

        Map<Integer, Map<Integer, RatingType>> ratingsToAdd = new TreeMap<>();
        ratingsToAdd.put(idPseudoUser, userRatings);

        Set<Integer> allowedUsers = new TreeSet<>(workingDataset.allUsers());

        for (int idUser : forbiddenUsers) {
            boolean removed = allowedUsers.remove(idUser);
            if (!removed) {
                throw new IllegalStateException("The original dataset did not contain the user.");
            }
        }

        workingDataset = new UserReductor_allowedUsers<>(workingDataset, allowedUsers);
        pseudoRatings = new BothIndexRatingsDataset<>(pseudoRatings, ratingsToAdd);

        ratedItems = null;

        return idPseudoUser;
    }

    @Override
    public RatingType getRating(int idUser, int idItem) throws UserNotFound, ItemNotFound {
        if (pseudoRatings.allUsers().contains(idUser)) {
            return pseudoRatings.getRating(idUser, idItem);
        } else {
            return workingDataset.getRating(idUser, idItem);
        }
    }

    @Override
    public Set<Integer> allUsers() {
        Set<Integer> allUsers = new TreeSet<>(workingDataset.allUsers());
        allUsers.addAll(pseudoRatings.allUsers());
        return allUsers;
    }

    @Override
    public Set<Integer> allRatedItems() {
        if (ratedItems == null) {
            ratedItems = new TreeSet<>(workingDataset.allRatedItems());
            ratedItems.addAll(pseudoRatings.allRatedItems());
        }
        return Collections.unmodifiableSet(ratedItems);
    }

    @Override
    public Set<Integer> getUserRated(Integer idUser) throws UserNotFound {
        if (pseudoRatings.allUsers().contains(idUser)) {
            return pseudoRatings.getUserRated(idUser);
        } else {
            return workingDataset.getUserRated(idUser);
        }
    }

    @Override
    public Set<Integer> getItemRated(Integer idItem) {
        Set<Integer> ret = new TreeSet<>();
        try {
            ret.addAll(pseudoRatings.getItemRated(idItem));
        } catch (ItemNotFound ex) {
        }

        try {
            ret.addAll(workingDataset.getItemRated(idItem));
        } catch (ItemNotFound ex) {
        }

        return ret;
    }

    @Override
    public Map<Integer, RatingType> getUserRatingsRated(Integer idUser) throws UserNotFound {
        if (pseudoRatings.allUsers().contains(idUser)) {
            return pseudoRatings.getUserRatingsRated(idUser);
        } else {
            return workingDataset.getUserRatingsRated(idUser);
        }
    }

    @Override
    public Map<Integer, RatingType> getItemRatingsRated(Integer idItem) throws ItemNotFound {
        Map<Integer, RatingType> ret = new TreeMap<>();
        try {
            ret.putAll(pseudoRatings.getItemRatingsRated(idItem));
        } catch (ItemNotFound ex) {
        }

        try {
            ret.putAll(workingDataset.getItemRatingsRated(idItem));
        } catch (ItemNotFound ex) {
        }

        return ret;
    }

    @Override
    public Domain getRatingsDomain() {
        return workingDataset.getRatingsDomain();
    }

    public int setPseudoUserRatings(Map<Integer, RatingType> userRatings, int idUser) {

        Set<Integer> users = new TreeSet<>();
        users.add(idUser);

        return setPseudoUserRatings(userRatings, users);
    }
}
