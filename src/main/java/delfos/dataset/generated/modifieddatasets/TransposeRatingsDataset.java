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
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Implementa la transpuesta de un dataset, cambiando items por productos.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 01-May-2013
 * @param <RatingType>
 */
public class TransposeRatingsDataset<RatingType extends Rating> extends RatingsDatasetAdapter<RatingType> {

    private final RatingsDataset<RatingType> originalDataset;

    /**
     * Construye el dataset transpuesto.
     *
     * @param ratingsDataset dataset original
     */
    public TransposeRatingsDataset(RatingsDataset<RatingType> ratingsDataset) {
        this.originalDataset = ratingsDataset;

    }

    @Override
    public RatingType getRating(int idUser, int idItem) throws UserNotFound, ItemNotFound {
        try {
            return originalDataset.getRating(idItem, idUser);
        } catch (UserNotFound ex) {
            throw new ItemNotFound(idItem, ex);
        } catch (ItemNotFound ex) {
            throw new UserNotFound(idUser, ex);
        }
    }

    @Override
    public Set<Integer> allUsers() {
        return originalDataset.allRatedItems();
    }

    @Override
    public Set<Integer> allRatedItems() {
        return originalDataset.allUsers();
    }

    @Override
    public Set<Integer> getUserRated(Integer idUser) throws UserNotFound {
        try {
            return originalDataset.getItemRated(idUser);
        } catch (ItemNotFound ex) {
            throw new UserNotFound(idUser, ex);
        }
    }

    @Override
    public Set<Integer> getItemRated(Integer idItem) throws ItemNotFound {
        try {
            return originalDataset.getUserRated(idItem);
        } catch (UserNotFound ex) {
            throw new ItemNotFound(idItem, ex);
        }
    }

    @Override
    public Map<Integer, RatingType> getUserRatingsRated(Integer idUser) throws UserNotFound {
        try {
            return originalDataset.getItemRatingsRated(idUser);
        } catch (ItemNotFound ex) {
            throw new UserNotFound(idUser, ex);
        }
    }

    @Override
    public Map<Integer, RatingType> getItemRatingsRated(Integer idItem) throws ItemNotFound {
        try {
            return originalDataset.getUserRatingsRated(idItem);
        } catch (UserNotFound ex) {
            throw new ItemNotFound(idItem, ex);
        }
    }

    @Override
    public Domain getRatingsDomain() {
        return originalDataset.getRatingsDomain();
    }

    @Override
    public int getNumRatings() {
        return originalDataset.getNumRatings();
    }
}
