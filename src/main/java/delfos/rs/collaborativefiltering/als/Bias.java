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
package delfos.rs.collaborativefiltering.als;

import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author jcastro
 */
public class Bias implements Serializable {

    private static final long serialVersionUID = 108L;

    private final double generalBias;
    private final Map<Long, Double> usersBias;
    private final Map<Long, Double> itemsBias;

    public Bias(double generalBias,
            Map<User, Double> usersBias,
            Map<Item, Double> itemsBias) {

        this.generalBias = generalBias;
        this.usersBias = usersBias.entrySet().parallelStream().collect(Collectors.toMap(entry -> entry.getKey().getId(), entry -> entry.getValue()));
        this.itemsBias = itemsBias.entrySet().parallelStream().collect(Collectors.toMap(entry -> entry.getKey().getId(), entry -> entry.getValue()));
    }

    public Function<Rating, Rating> getBiasApplier() {
        return (rating
                -> {
            double userBias = usersBias.get(rating.getUser().getId());
            double itemBias = itemsBias.get(rating.getItem().getId());
            double unbiasedValue = rating.getRatingValue().doubleValue()
                    - this.generalBias
                    - userBias
                    - itemBias;

            return rating.copyWithRatingValue(unbiasedValue);
        });
    }

    public Function<Rating, Rating> getBiasRestorer() {
        return (rating
                -> {
            double userBias = usersBias.get(rating.getUser().getId());
            double itemBias = itemsBias.get(rating.getItem().getId());
            double unbiasedValue = rating.getRatingValue().doubleValue()
                    + this.generalBias
                    + userBias
                    + itemBias;

            return rating.copyWithRatingValue(unbiasedValue);
        });
    }

    public double removeBias(int idUser, int idItem, double rating) {
        double userBias = usersBias.get(idUser);
        double itemBias = itemsBias.get(idItem);
        double unbiasedValue = rating
                - this.generalBias
                - userBias
                - itemBias;

        return unbiasedValue;
    }

    public double restoreBias(long idUser, long idItem, double unbiased) {
        double userBias = usersBias.get(idUser);
        double itemBias = itemsBias.get(idItem);
        double unbiasedValue = unbiased
                + this.generalBias
                + userBias
                + itemBias;

        return unbiasedValue;
    }

    public Bias(DatasetLoader<? extends Rating> datasetLoader) {

        generalBias = datasetLoader.getRatingsDataset().getMeanRating();

        usersBias = datasetLoader.getUsersDataset().parallelStream().collect(Collectors.toMap(user -> user.getId(), user -> {
            double userBias;

            try {
                userBias = datasetLoader.getRatingsDataset()
                        .getUserRatingsRated(user.getId()).values()
                        .parallelStream()
                        .mapToDouble(rating -> rating.getRatingValue().doubleValue() - generalBias)
                        .average()
                        .orElse(0);
                return userBias;
            } catch (UserNotFound ex) {
                return 0.0;
            }
        }));

        itemsBias = datasetLoader.getContentDataset().parallelStream().collect(Collectors.toMap(item -> item.getId(), item -> {
            double itemBias;
            try {
                itemBias = datasetLoader.getRatingsDataset()
                        .getItemRatingsRated(item.getId()).values()
                        .parallelStream()
                        .mapToDouble(rating -> rating.getRatingValue().doubleValue() - generalBias - usersBias.get(rating.getUser().getId()))
                        .average()
                        .orElse(0);
                return itemBias;
            } catch (ItemNotFound ex) {
                return 0.0;
            }
        }));
    }

    public double restoreBias(User user, Item item, double value) {
        double userBias = usersBias.get(user.getId());
        double itemBias = itemsBias.get(item.getId());

        double originalRating = value + generalBias + userBias + itemBias;

        return originalRating;
    }

    public double getUserBias(User user) {
        return usersBias.get(user.getId());
    }

    public double getItemBias(Item item) {
        return itemsBias.get(item.getId());
    }

    public double getGeneralBias() {
        return generalBias;
    }

}
