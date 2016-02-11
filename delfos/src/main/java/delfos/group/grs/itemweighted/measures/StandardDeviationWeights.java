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

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.statisticalfuncions.StandardDeviation;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.util.DatasetUtilities;
import delfos.group.groupsofusers.GroupOfUsers;

public class StandardDeviationWeights extends GroupItemWeight {

    private static final long serialVersionUID = 1L;

    @Override
    public Map<Integer, ? extends Number> getItemWeights(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers groupOfUsers) throws CannotLoadRatingsDataset, UserNotFound {

        Map<Integer, Number> itemWeights = new TreeMap<>();
        final double ratingRangeWidth = datasetLoader.getRatingsDataset().getRatingsDomain().width().doubleValue();

        Map<Integer, Map<Integer, Number>> membersRatings = DatasetUtilities.getMembersRatings_byUser(groupOfUsers, datasetLoader);
        Map<Integer, Map<Integer, Number>> membersRatings_byItem = DatasetUtilities.transformIndexedByUsersToIndexedByItems_Map(membersRatings);

        for (int idItem : membersRatings_byItem.keySet()) {
            Collection<Number> membersRatingsOverThisItem = membersRatings_byItem.get(idItem).values();
            StandardDeviation stdDev = new StandardDeviation(membersRatingsOverThisItem);
            double standardDeviation = stdDev.getStandardDeviation();

            double weight = ratingRangeWidth - standardDeviation;
            itemWeights.put(idItem, weight);
        }

        itemWeights = normaliseWeights(itemWeights);
        return itemWeights;
    }
}
