package delfos.group.grs.filtered.filters;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.dataset.util.DatasetPrinterDeprecated;
import delfos.dataset.util.DatasetUtilities;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class OutliersItemsStandardDeviationThresholdFilterTest {

    public OutliersItemsStandardDeviationThresholdFilterTest() {
    }

    @Test
    public void testGetFilteredRatingsRandomDataset() {

        RandomDatasetLoader datasetLoader = new RandomDatasetLoader(10, 10, 0.8);
        datasetLoader.setSeedValue(0);

        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        OutliersItemsStandardDeviationThresholdFilter instance = new OutliersItemsStandardDeviationThresholdFilter();

        GroupOfUsers group = new GroupOfUsers(1, 2, 3, 4, 5);
        //Fetch dataset.
        Map<Integer, Map<Integer, ? extends Rating>> groupRatings = new TreeMap<>();
        TreeSet<Integer> items = new TreeSet<>();
        for (int idUser : group) {
            try {
                groupRatings.put(idUser, ratingsDataset.getUserRatingsRated(idUser));
                items.addAll(groupRatings.get(idUser).keySet());
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }
        Global.showInfoMessage("Original ratings of group\n");
        DatasetPrinterDeprecated.printCompactRatingTable(new BothIndexRatingsDataset(groupRatings), group.getIdMembers(), items);

        Map<Integer, Map<Integer, Rating>> filteredRatings = instance.getFilteredRatings(ratingsDataset, group);
        assertNotNull(filteredRatings);

        Global.showInfoMessage("Original ratings of group\n");
        DatasetPrinterDeprecated.printCompactRatingTable(new BothIndexRatingsDataset(groupRatings), group.getIdMembers(), items);

        Global.showInfoMessage("Filtered ratings of group\n");
        DatasetPrinterDeprecated.printCompactRatingTable(DatasetUtilities.getMapOfMaps_Number(filteredRatings), group.getIdMembers(), items);
    }
}
