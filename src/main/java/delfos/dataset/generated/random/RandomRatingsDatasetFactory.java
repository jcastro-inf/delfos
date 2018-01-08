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
package delfos.dataset.generated.random;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.domain.Domain;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 5-marzo-2015
 */
public class RandomRatingsDatasetFactory {

    public static final Set<Long> DEFAULT_USER_SET = Collections.unmodifiableSet(LongStream
            .rangeClosed(1,20).boxed().collect(Collectors.toSet()));

    public static final Set<Long> DEFAULT_ITEM_SET = Collections.unmodifiableSet(LongStream
            .rangeClosed(1,20).boxed().collect(Collectors.toSet()));

    public static final double DEFAULT_LOAD_FACTOR = 0.2;

    /**
     * Usuarios, desde 1 hasta numUsers.
     *
     * @param numUsers numero de usuarios
     * @return conjunto de usuarios
     */
    public static Set<Long> createUserSet(int numUsers) {
        TreeSet<Long> users = new TreeSet<>();
        for (long user = 1; user <= numUsers; user++) {
            users.add(user);
        }
        return users;
    }

    /**
     * Usuarios, desde 1 hasta numItems.
     *
     * @param numItems numero de items
     * @return conjunto de items
     */
    public static Set<Long> createItemSet(int numItems) {
        TreeSet<Long> items = new TreeSet<>();
        for (long item = 1; item <= numItems; item++) {
            items.add(item);
        }
        return items;
    }

    private RandomRatingsDatasetFactory() {
    }

    public static RandomRatingsDataset createDefault() {
        return new RandomRatingsDataset();
    }

    public static RandomRatingsDataset createDefault(long seed) {
        return new RandomRatingsDataset(
                RandomRatingsDatasetFactory.DEFAULT_USER_SET,
                RandomRatingsDatasetFactory.DEFAULT_ITEM_SET,
                RandomRatingsDatasetFactory.DEFAULT_LOAD_FACTOR,
                Rating.DEFAULT_DECIMAL_DOMAIN,
                seed
        );
    }

    public static RatingsDataset<? extends Rating> createRatingsDatasetWithLoadFactor(int numUsers, int numItems, double loadFactor) {
        return RandomRatingsDatasetFactory.createRatingsDatasetWithLoadFactor(numUsers, numItems, loadFactor, Rating.DEFAULT_DECIMAL_DOMAIN, numUsers);
    }

    public static RandomRatingsDataset createRatingsDatasetWithLoadFactor(int numUsers, int numItems, double loadFactor, Domain ratingDomain, long seed) {
        return new RandomRatingsDataset(createUserSet(numUsers), createItemSet(numItems), loadFactor, ratingDomain, seed);
    }

    public static RandomRatingsDataset createRatingsDatasetWithLoadFactor(Set<Long> users, Set<Long> items, double loadFactor, Domain ratingDomain, long seed) {
        return new RandomRatingsDataset(users, items, loadFactor, ratingDomain, seed);
    }

    public static RandomRatingsDataset createRatingsDatasetWithNumUserRatings(int numUsers, int numItems, int numRatingsPerUser, Domain ratingDomain, long seed) {
        return new RandomRatingsDataset(createUserSet(numUsers), createItemSet(numItems), numRatingsPerUser, ratingDomain, seed);
    }

    public static RandomRatingsDataset createRatingsDatasetWithNumUserRatings(Set<Long> users, Set<Long> items, int numRatingsPerUser, Domain ratingDomain, long seed) {
        return new RandomRatingsDataset(users, items, numRatingsPerUser, ratingDomain, seed);
    }

}
