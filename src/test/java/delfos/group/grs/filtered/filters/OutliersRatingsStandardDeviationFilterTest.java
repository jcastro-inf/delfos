package delfos.group.grs.filtered.filters;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class OutliersRatingsStandardDeviationFilterTest {

    public OutliersRatingsStandardDeviationFilterTest() {
    }

    @Test
    public void testGetFilteredRatingsComplete5u10i() {

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("complete-5u-10i");
        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        OutliersRatingsStandardDeviationFilter instance = new OutliersRatingsStandardDeviationFilter();

        GroupOfUsers group = new GroupOfUsers(1l, 2l, 5l);
        //Fetch dataset.
        Map<Long, Map<Long, ? extends Rating>> groupRatings = new TreeMap<>();
        TreeSet<Long> items = new TreeSet<>();
        for (long idUser : group) {
            try {
                groupRatings.put(idUser, ratingsDataset.getUserRatingsRated(idUser));
                items.addAll(groupRatings.get(idUser).keySet());
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }

        Map<Long, Map<Long, Rating>> filteredRatings = instance.getFilteredRatings(ratingsDataset, group);
        assertNotNull(filteredRatings);
    }

    @Test
    public void testGetFilteredRatingsRandomDataset() {

        RandomDatasetLoader datasetLoader = new RandomDatasetLoader(10, 10, 0.8);
        datasetLoader.setSeedValue(0);

        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        OutliersRatingsStandardDeviationFilter instance = new OutliersRatingsStandardDeviationFilter();

        GroupOfUsers group = new GroupOfUsers(1l, 2l, 3l, 4l, 5l);
        //Fetch dataset.
        Map<Long, Map<Long, ? extends Rating>> groupRatings = new TreeMap<>();
        TreeSet<Long> items = new TreeSet<>();
        for (long idUser : group) {
            try {
                groupRatings.put(idUser, ratingsDataset.getUserRatingsRated(idUser));
                items.addAll(groupRatings.get(idUser).keySet());
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }

        Map<Long, Map<Long, Rating>> filteredRatings = instance.getFilteredRatings(ratingsDataset, group);
        assertNotNull(filteredRatings);
    }
}
