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
package delfos.group.grs.itemweighted.measures;

import java.util.Map;
import java.util.TreeMap;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Debe devolver los pesos normalizados.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public abstract class GroupItemWeight extends ParameterOwnerAdapter {

    private static final long serialVersionUID = 1L;

    public abstract Map<Integer, ? extends Number> getItemWeights(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers groupOfUsers) throws CannotLoadRatingsDataset, UserNotFound;

    @Override
    public final ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.GROUP_ITEM_WEIGHT;
    }

    public static Map<Integer, Number> normaliseWeights(Map<Integer, ? extends Number> weights) {
        double sumOfValues = 0;
        sumOfValues = weights
                .values()
                .stream()
                .map((value) -> value.doubleValue())
                .reduce(sumOfValues, (accumulator, _item) -> accumulator + _item);

        Map<Integer, Number> normalisedWeights = new TreeMap<>();

        for (int key : weights.keySet()) {
            double weight = weights.get(key).doubleValue();
            double normalisedWeight = weight / sumOfValues;
            normalisedWeights.put(key, normalisedWeight);
        }

        return normalisedWeights;
    }

}
