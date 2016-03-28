package delfos.group.grs.filtered.filters;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 * Test para comprobar el correcto funcionamiento de la técnica de filtrado
 * {@link OutliersRatingsFilter}.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 03-May-2013
 */
public class OutliersRatingsFilterTest extends DelfosTest {

    public OutliersRatingsFilterTest() {
    }

    /**
     * Test of getFilteredRatings method, of class OutliersRatingsFilter.
     */
    @Test
    public void testGetFilteredRatings() {

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("complete-5u-10i");
        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        OutliersRatingsFilter instance = new OutliersRatingsFilter();

        GroupOfUsers group = new GroupOfUsers(1, 2, 5);
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

        Map<Integer, Map<Integer, Rating>> filteredRatings = instance.getFilteredRatings(ratingsDataset, group);
        assertNotNull(filteredRatings);
    }

    /**
     * Test of getFilteredRatings method, of class OutliersRatingsFilter.
     */
    @Test
    public void testGetFilteredRatings2() {

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("complete-5u-10i");
        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

        OutliersRatingsFilter instance = new OutliersRatingsFilter();

        Random random = new Random(0);

        for (int i = 0; i < 5; i++) {
            //Random generation of group.
            Set<Integer> members = new TreeSet<>();

            while (members.size() < 4) {
                Integer[] users = ratingsDataset.allUsers().toArray(new Integer[0]);
                members.add(users[random.nextInt(users.length)]);
            }

            GroupOfUsers group = new GroupOfUsers(members.toArray(new Integer[0]));
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

            Map<Integer, Map<Integer, Rating>> filteredRatings = instance.getFilteredRatings(ratingsDataset, group);
            assertNotNull(filteredRatings);
        }
    }
}
