package delfos.group.grs.itemweighted.measures;

import java.util.Map;
import java.util.TreeMap;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.util.DatasetUtilities;
import delfos.group.groupsofusers.GroupOfUsers;

public class NoWeight extends GroupItemWeight {

    private static final long serialVersionUID = 1L;

    @Override
    public Map<Integer, ? extends Number> getItemWeights(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers groupOfUsers) throws CannotLoadRatingsDataset, UserNotFound {

        Map<Integer, Number> itemWeights = new TreeMap<>();

        Map<Integer, Map<Integer, Number>> membersRatings = DatasetUtilities.getMembersRatings_byUser(groupOfUsers, datasetLoader);
        Map<Integer, Map<Integer, Number>> membersRatings_byItem = DatasetUtilities.transformIndexedByUsersToIndexedByItems_Map(membersRatings);

        for (int idItem : membersRatings_byItem.keySet()) {
            itemWeights.put(idItem, 1);
        }

        itemWeights = normaliseWeights(itemWeights);
        return itemWeights;
    }
}
