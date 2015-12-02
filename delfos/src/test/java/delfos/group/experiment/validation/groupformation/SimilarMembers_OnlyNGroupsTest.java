package delfos.group.experiment.validation.groupformation;

import delfos.common.DateCollapse;
import delfos.common.Global;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.constants.DelfosTest;
import delfos.constants.TestConstants;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @version 10-abr-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class SimilarMembers_OnlyNGroupsTest extends DelfosTest {

    private static final long seed = 65465654;
    private final static String similarityMeasureBufferDirectory = TestConstants.TEST_DATA_DIRECTORY + SimilarMembers_OnlyNGroupsTest.class.getSimpleName() + File.separator;

    public SimilarMembers_OnlyNGroupsTest() {
    }

    /**
     * Test of shuffle method, of class SimilarMembers_OnlyNGroups.
     */
    @Test
    public void testShuffle() {
        final int groupSizeValue = 5;
        final int numGroupsValue = 1;

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");
        datasetLoader.getRatingsDataset();

        SimilarMembers_OnlyNGroups instance = new SimilarMembers_OnlyNGroups(numGroupsValue, groupSizeValue);
        instance.addListener((String message, int progress, long remainingTimeInMS) -> {
            Global.showln(progress + "% " + message + " remainingTime: " + DateCollapse.collapse(remainingTimeInMS));
        });
        instance.setSeedValue(seed);
        Collection<GroupOfUsers> result = instance.shuffle(datasetLoader);

        Collection<GroupOfUsers> expResult = new ArrayList<>();
        expResult.add(new GroupOfUsers(121, 541, 568, 582, 886));
        assertEquals(expResult, result);
    }

    /**
     * Test of shuffle method, of class SimilarMembers_OnlyNGroups.
     */
    @Test
    public void testSharingUsers() {
        final int groupSizeValue = 5;
        final int numGroupsValue = 10;

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");
        datasetLoader.getRatingsDataset();

        SimilarMembers_OnlyNGroups instance = new SimilarMembers_OnlyNGroups(numGroupsValue, groupSizeValue);
        instance.addListener((String message, int progress, long remainingTimeInMS) -> {
            Global.showln(progress + "% " + message + " remainingTime: " + DateCollapse.collapse(remainingTimeInMS));
        });
        instance.setSeedValue(seed);

        List<GroupOfUsers> groups;
        {
            Collection<GroupOfUsers> result = instance.shuffle(datasetLoader);
            groups = new ArrayList<>(result);
        }

        //Collections.sort(groups);
        Global.showln("Grupos generados: ");
        groups.stream().forEach((group) -> {
            Global.showln("\t" + group);
        });

        groups.stream().forEach((group1) -> {
            groups.stream().filter((group2) -> !(group1 == group2)).forEach((group2) -> {
                Set<Integer> intersection = new TreeSet<>();
                intersection.addAll(group1.getIdMembers());
                intersection.retainAll(group2.getIdMembers());
                if (!intersection.isEmpty()) {
                    Assert.fail("Group " + group1 + " is sharing users with group " + group2 + "  ( intersection" + intersection + ")");
                }
            });
        });
    }

    /**
     * Test of shuffle method, of class SimilarMembers_OnlyNGroups.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testTooManyGroupsAsked() {
        final int groupSizeValue = 5;
        final int numGroupsValue = 189;

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");
        datasetLoader.getRatingsDataset();

        SimilarMembers_OnlyNGroups instance = new SimilarMembers_OnlyNGroups(numGroupsValue, groupSizeValue);

        Collection<GroupOfUsers> result = instance.shuffle(datasetLoader);
    }

    /**
     * Test of shuffle method, of class SimilarMembers_OnlyNGroups.
     */
    @Test
    public void testExactUserPartitionInGroupsAsked() {
        final int groupSizeValue = 23;
        final int numGroupsValue = 41;

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");
        datasetLoader.getRatingsDataset();

        SimilarMembers_OnlyNGroups instance = new SimilarMembers_OnlyNGroups(numGroupsValue, groupSizeValue);

        Collection<GroupOfUsers> result = instance.shuffle(datasetLoader);

        assertTrue(result.size() == numGroupsValue);

        for (GroupOfUsers groupOfUsers : result) {
            assertTrue(groupOfUsers.size() == groupSizeValue);
        }
    }

    /**
     * Test of shuffle method, of class SimilarMembers_OnlyNGroups.
     */
    @Test
    public void testExactUserPartitionInGroupsAsked_changed() {
        final int groupSizeValue = 41;
        final int numGroupsValue = 23;

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");
        datasetLoader.getRatingsDataset();

        SimilarMembers_OnlyNGroups instance = new SimilarMembers_OnlyNGroups(numGroupsValue, groupSizeValue);

        Collection<GroupOfUsers> result = instance.shuffle(datasetLoader);

        assertTrue(result.size() == numGroupsValue);

        for (GroupOfUsers groupOfUsers : result) {
            assertTrue(groupOfUsers.size() == groupSizeValue);
        }
    }

    /**
     * Test of shuffle method, of class SimilarMembers_OnlyNGroups.
     */
    @Test
    public void testAGroupForEachUser() {
        final int groupSizeValue = 1;
        final int numGroupsValue = 943;

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");
        datasetLoader.getRatingsDataset();

        SimilarMembers_OnlyNGroups instance = new SimilarMembers_OnlyNGroups(numGroupsValue, groupSizeValue);

        Collection<GroupOfUsers> result = instance.shuffle(datasetLoader);

        assertTrue(result.size() == numGroupsValue);

        for (GroupOfUsers groupOfUsers : result) {
            assertTrue(groupOfUsers.size() == groupSizeValue);
        }
    }
}
