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
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.1 (21-01-2013) Ahora implementa de {@link RatingsDatasetAdapter}
 * @version 21-Enero-2014 Ahora también se permite eliminar usuarios, para dar
 * soporte a GRS.
 * @param <RatingType>
 */
public class PseudoUserRatingsDataset<RatingType extends Rating> extends RatingsDatasetAdapter<RatingType> {

    private final RatingsDataset<RatingType> constructionOriginalDataset;
    private RatingsDataset<RatingType> workingDataset;
    private RatingsDataset<RatingType> pseudoRatings = new BothIndexRatingsDataset<>();
    private long nextIdUser = -1;
    private Set<Long> ratedItems = null;
    private boolean pseudoUserSetted = false;
    private long idPseudoUser;

    public PseudoUserRatingsDataset(
            RatingsDataset<? extends RatingType> ratingsDataset,
            Map<Long, RatingType> pseudoUserRatings,
            Collection<Long> forbiddenUsers) {

        super();
        constructionOriginalDataset = (RatingsDataset<RatingType>) ratingsDataset;

        workingDataset = constructionOriginalDataset;

        if (pseudoUserSetted) {
            throw new IllegalStateException("The method setPseudoUserRatings can conly be called once per object instantiation.");
        }

        pseudoUserSetted = true;

        if (pseudoUserRatings == null) {
            throw new IllegalArgumentException("No se puede crear un usuario sin valoraciones.");
        }

        //Obtengo el siguiente id de usuario.
        idPseudoUser = getNextIdUser();

        while (constructionOriginalDataset.allUsers().contains(idPseudoUser)) {
            idPseudoUser++;
        }

        Map<Long, Map<Long, RatingType>> ratingsToAdd = new TreeMap<>();
        ratingsToAdd.put(idPseudoUser, pseudoUserRatings);

        Set<Long> allowedUsers = new TreeSet<>(workingDataset.allUsers());

        for (long idUser : forbiddenUsers) {
            boolean removed = allowedUsers.remove(idUser);
            if (!removed) {
                throw new IllegalStateException("The original dataset did not contain the user.");
            }
        }

        workingDataset = new UserReductor_allowedUsers<>(workingDataset, allowedUsers);
        pseudoRatings = new BothIndexRatingsDataset<>(pseudoRatings, ratingsToAdd);

        ratedItems = null;

    }

    public PseudoUserRatingsDataset(RatingsDataset<? extends RatingType> ratingsDataset, Map<Long, RatingType> userRatings) {
        this(ratingsDataset, userRatings, new TreeSet<>());
    }

    public long getIdPseudoUser() {
        return idPseudoUser;
    }

    @Override
    public RatingType getRating(long idUser, long idItem) throws UserNotFound, ItemNotFound {
        if (pseudoRatings.allUsers().contains(idUser)) {
            return pseudoRatings.getRating(idUser, idItem);
        } else {
            return workingDataset.getRating(idUser, idItem);
        }
    }

    @Override
    public Set<Long> allUsers() {
        Set<Long> allUsers = new TreeSet<>(workingDataset.allUsers());
        allUsers.addAll(pseudoRatings.allUsers());
        return allUsers;
    }

    @Override
    public Set<Long> allRatedItems() {
        if (ratedItems == null) {
            ratedItems = new TreeSet<>(workingDataset.allRatedItems());
            ratedItems.addAll(pseudoRatings.allRatedItems());
        }
        return Collections.unmodifiableSet(ratedItems);
    }

    @Override
    public Set<Long> getUserRated(long idUser) throws UserNotFound {
        if (pseudoRatings.allUsers().contains(idUser)) {
            return pseudoRatings.getUserRated(idUser);
        } else {
            return workingDataset.getUserRated(idUser);
        }
    }

    @Override
    public Set<Long> getItemRated(long idItem) {
        Set<Long> ret = new TreeSet<>();
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
    public Map<Long, RatingType> getUserRatingsRated(long idUser) throws UserNotFound {
        if (pseudoRatings.allUsers().contains(idUser)) {
            return pseudoRatings.getUserRatingsRated(idUser);
        } else {
            return workingDataset.getUserRatingsRated(idUser);
        }
    }

    @Override
    public Map<Long, RatingType> getItemRatingsRated(long idItem) throws ItemNotFound {
        Map<Long, RatingType> ret = new TreeMap<>();
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

    private long getNextIdUser() {
        return nextIdUser--;
    }
}
