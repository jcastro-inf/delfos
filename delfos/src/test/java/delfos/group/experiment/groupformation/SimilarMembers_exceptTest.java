package delfos.group.experiment.groupformation;

import delfos.common.Global;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.constants.DelfosTest;
import delfos.constants.TestConstants;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.experiment.validation.groupformation.SimilarMembers_except;
import delfos.group.groupsofusers.GroupOfUsers;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @version 10-abr-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class SimilarMembers_exceptTest extends DelfosTest {

    private static final long seed = 65465654;

    private final static String similarityMeasureBufferDirectory = TestConstants.TEST_DATA_DIRECTORY + SimilarMembers_exceptTest.class.getSimpleName() + File.separator;

    public SimilarMembers_exceptTest() {
    }

    /**
     * Test of shuffle method, of class SimilarMembers_except.
     */
    @Test
    public void testShuffle() {
        final int groupSizeValue = 5;
        final int numGroupsValue = 1;
        final int numMembersCandidate = 1;

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");
        datasetLoader.getRatingsDataset();

        SimilarMembers_except instance = new SimilarMembers_except(numGroupsValue, groupSizeValue, numMembersCandidate);
        instance.addListener((String message, int progress) -> {
            Global.showln(progress + "% " + message);
        });
        instance.setSeedValue(seed);
        Collection<GroupOfUsers> result = instance.shuffle(datasetLoader);

        Collection<GroupOfUsers> expResult = new ArrayList<>();
        expResult.add(new GroupOfUsers(5, 12, 16, 260, 886));
        assertEquals(expResult, result);
    }

    /**
     * Test of shuffle method, of class SimilarMembers_except.
     */
    @Test
    public void testSharingUsers() {
        final int groupSizeValue = 5;
        final int numGroupsValue = 10;
        final int numMembersCandidate = 1;

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");
        datasetLoader.getRatingsDataset();

        SimilarMembers_except instance = new SimilarMembers_except(numGroupsValue, groupSizeValue, numMembersCandidate);
        instance.addListener((String message, int progress) -> {
            Global.showln(progress + "% " + message);
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
     * Test of shuffle method, of class SimilarMembers_except.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testTooManyGroupsAsked() {
        final int groupSizeValue = 5;
        final int numGroupsValue = 189;
        final int numMembersCandidate = 1;

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");
        datasetLoader.getRatingsDataset();

        SimilarMembers_except instance = new SimilarMembers_except(numGroupsValue, groupSizeValue, numMembersCandidate);

        Collection<GroupOfUsers> result = instance.shuffle(datasetLoader);
    }
}
