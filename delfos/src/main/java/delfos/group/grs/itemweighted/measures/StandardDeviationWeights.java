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
