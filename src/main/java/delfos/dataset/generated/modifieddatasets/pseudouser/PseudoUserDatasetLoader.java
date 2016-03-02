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
package delfos.dataset.generated.modifieddatasets.pseudouser;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.basic.user.UsersDatasetAdapter;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @param <RatingType>
 */
public class PseudoUserDatasetLoader<RatingType extends Rating> extends DatasetLoaderAbstract<RatingType> {

    private final DatasetLoader<RatingType> originalDatasetLoader;

    private int idPseudoUserNext = -1;

    Map<User, Map<Item, RatingType>> pseudoUsersRatings;

    public PseudoUserDatasetLoader(DatasetLoader<RatingType> originalDatasetLoader) {
        this.originalDatasetLoader = originalDatasetLoader;
        pseudoUsersRatings = new TreeMap<>();
    }

    public synchronized User addPseudoUser(Map<Item, RatingType> pseudoUserRatings) {
        if (isFrozen) {
            throw new IllegalStateException("Cannot add users to a frozen dataset.");
        }

        User pseudoUser = new User(idPseudoUserNext);
        idPseudoUserNext++;

        Map<Item, RatingType> pseudoUserRatingsConverted = pseudoUserRatings.values().stream()
                .map(rating -> {
                    return (RatingType) rating.copyWithUser(pseudoUser);
                })
                .collect(Collectors.toMap(
                                rating -> rating.getItem(),
                                Function.identity()));

        pseudoUsersRatings.put(pseudoUser, pseudoUserRatingsConverted);

        return pseudoUser;
    }

    private PseudoUserRatingsDataset<RatingType> ratingsDataset = null;
    private UsersDataset usersDataset = null;

    @Override
    public RatingsDataset<RatingType> getRatingsDataset() throws CannotLoadRatingsDataset {
        freeze();
        return (RatingsDataset<RatingType>) ratingsDataset;
    }

    @Override
    public UsersDataset getUsersDataset() {
        freeze();
        return usersDataset;
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {
        freeze();
        return originalDatasetLoader.getContentDataset();
    }

    boolean isFrozen = false;

    public synchronized void freeze() {
        isFrozen = true;

        ratingsDataset = new PseudoUserRatingsDataset<>(
                originalDatasetLoader,
                pseudoUsersRatings);

        Set<User> allUsers = new TreeSet<>(originalDatasetLoader.getUsersDataset());
        allUsers.addAll(pseudoUsersRatings.keySet());
        usersDataset = new UsersDatasetAdapter(allUsers);
    }

}
