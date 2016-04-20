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
package delfos.rs.collaborativefiltering.factorization;

import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.user.User;
import delfos.rs.collaborativefiltering.als.Bias;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 */
public class MatrixFactorizationModel implements Serializable {

    private static final long serialVersionUID = 108L;

    private final Map<User, List<Double>> userFeatures;
    private final Map<Item, List<Double>> itemFeatures;
    private final Bias bias;

    public MatrixFactorizationModel(Map<User, List<Double>> userFeatures, Map<Item, List<Double>> itemFeatures, Bias bias) {

        this.userFeatures = userFeatures;

        this.itemFeatures = itemFeatures;
        this.bias = bias;

    }

    public List<Double> getUserFeatures(User user) {
        return userFeatures.get(user);
    }

    public List<Double> getItemFeatures(Item item) {
        return itemFeatures.get(item);
    }

    public double predict(User user, Item item) {
        List<Double> userVector = getUserFeatures(user);
        List<Double> itemVector = getItemFeatures(item);

        return IntStream.range(0, userVector.size())
                .mapToDouble(index -> userVector.get(index) * itemVector.get(index))
                .sum();
    }

    public double predictRating(User user, Item item) {

        double predict = predict(user, item);

        if (bias == null) {
            return predict;
        } else {
            return bias.restoreBias(user, item, predict);
        }
    }

    public Bias getBias() {
        return bias;
    }

}
