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
package delfos.dataset.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;

/**
 * Implementa operaciones simples entre conjuntos de valoraciones.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 07-May-2013
 */
public class DatasetOperations {

    /**
     * Efectua la operación resta de conjuntos. Devuelve los ratings que solo
     * están en el conjunto a.
     *
     * @param <RatingType>
     * @param a
     * @param b
     * @return
     */
    public static <RatingType> Map<Integer, Map<Integer, RatingType>> minus(Map<Integer, Map<Integer, RatingType>> a, Map<Integer, Map<Integer, RatingType>> b) {

        Map<Integer, Map<Integer, RatingType>> ret = new TreeMap<>();
        {
            //Users only on set b:
            Set<Integer> onlyInA = new TreeSet<>(a.keySet());
            onlyInA.removeAll(b.keySet());

            onlyInA.stream().forEach((idUser) -> {
                ret.put(idUser, new TreeMap<>(b.get(idUser)));
            });
        }

        {
            //Users on both
            Set<Integer> usersCommon = new TreeSet<>(a.keySet());
            usersCommon.retainAll(b.keySet());

            usersCommon.stream().forEach((idUser) -> {
                TreeMap<Integer, RatingType> ratingsOnlyInA = new TreeMap<>(a.get(idUser));
                b.get(idUser).keySet().stream().forEach((idItem) -> {
                    ratingsOnlyInA.remove(idItem);
                });

                ret.put(idUser, ratingsOnlyInA);
            });
        }

        return ret;
    }

    public static Map<Integer, Rating> convertNumberToRatings_singleUser(int idUser, Map<Integer, Number> ratings) {
        TreeMap<Integer, Rating> ret = new TreeMap<>();

        ratings.entrySet().stream().forEach((entry) -> {
            int idItem = entry.getKey();
            Number ratingValue = entry.getValue();
            ret.put(idItem, new Rating(idUser, idItem, ratingValue));
        });
        return ret;
    }

    public static Map<Integer, Map<Integer, Rating>> convertNumberToRatings(Map<Integer, Map<Integer, Number>> rating_number) {
        Map<Integer, Map<Integer, Rating>> rating_rating = new TreeMap<>();
        rating_number.keySet().stream().forEach((idUser) -> {
            rating_rating.put(idUser, convertNumberToRatings_singleUser(idUser, rating_number.get(idUser)));
        });
        return rating_rating;
    }

    public static Map<Integer, Number> convertRatingsToNumber_singleUser(int idUser, Map<Integer, ? extends Rating> ratings) {
        TreeMap<Integer, Number> ret = new TreeMap<>();

        ratings.entrySet().stream().forEach((entry) -> {
            int idItem = entry.getKey();
            Rating rating = entry.getValue();
            ret.put(idItem, rating.getRatingValue());
        });
        return ret;
    }

    public static Map<Integer, Map<Integer, Number>> convertRatingsToNumber(Map<Integer, Map<Integer, ? extends Rating>> rating_rating) {

        Map<Integer, Map<Integer, Number>> rating_number = new TreeMap<>();
        rating_rating.keySet().stream().forEach((idUser) -> {
            rating_number.put(idUser, convertRatingsToNumber_singleUser(idUser, rating_rating.get(idUser)));
        });
        return rating_number;
    }

    public static <RatingType extends Rating> Map<Integer, Map<Integer, RatingType>> selectRatings(RatingsDataset<RatingType> ratingsDataset, Collection<Integer> users) throws UserNotFound {
        Map<Integer, Map<Integer, RatingType>> selectedRatings = new TreeMap<>();

        for (int idUser : users) {
            selectedRatings.put(idUser, new TreeMap<>(ratingsDataset.getUserRatingsRated(idUser)));
        }

        return selectedRatings;
    }
}
