package delfos.group.experiment.groupformation;

import delfos.group.experiment.validation.groupformation.SimilarMembers;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.constants.DelfosTest;
import delfos.constants.TestConstants;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 *
 * @version 10-abr-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class SimilarMembersTest extends DelfosTest {

    private static final long seed = 65465654;

    private final static String similarityMeasureBufferDirectory = TestConstants.TEST_DATA_DIRECTORY + SimilarMembersTest.class.getSimpleName() + File.separator;

    public SimilarMembersTest() {
    }

    /**
     * Test of shuffle method, of class SimilarMembers.
     */
    @Test
    public void testShuffle() {
        final int groupSizeValue = 5;
        final int numGroupsValue = 1;
        final int numMembersCandidate = 1;

        System.out.println(SimilarMembersTest.class + ".testShuffle()");

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");
        datasetLoader.getRatingsDataset();

        SimilarMembers instance = new SimilarMembers(numGroupsValue, groupSizeValue, numMembersCandidate);
        instance.addListener((String message, int progress) -> {
            System.out.println(progress + "% " + message);
        });
        instance.setSeedValue(seed);
        Collection<GroupOfUsers> result = instance.shuffle(datasetLoader);

        Collection<GroupOfUsers> expResult = new ArrayList<>();
        //expResult.add(new GroupOfUsers(368, 541, 576, 686, 886));
        //expResult.add(new GroupOfUsers(121, 129, 568, 886, 894));
        expResult.add(new GroupOfUsers(33, 88, 245, 571, 886));
        assertEquals(expResult, result);
    }

    /**
     * Test of shuffle method, of class SimilarMembers.
     */
    @Test
    public void testSharingUsers() {
        final int groupSizeValue = 5;
        final int numGroupsValue = 10;
        final int numMembersCandidate = 1;

        System.out.println(SimilarMembersTest.class + ".testShuffle()");

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");
        datasetLoader.getRatingsDataset();

        SimilarMembers instance = new SimilarMembers(numGroupsValue, groupSizeValue, numMembersCandidate);

//        UserUserSimilarity bufferedSimilarity = new UserUserSimilarity_buffered(
//                new File(similarityMeasureBufferDirectory),
//                (UserUserSimilarity) instance.getParameterValue(SimilarMembers.SIMILARITY_MEASURE)
//        );
//        instance.setParameterValue(SimilarMembers.SIMILARITY_MEASURE, bufferedSimilarity);
        instance.addListener((String message, int progress) -> {
            System.out.println(progress + "% " + message);
        });
        instance.setSeedValue(seed);

        List<GroupOfUsers> groups;
        {
            Collection<GroupOfUsers> result = instance.shuffle(datasetLoader);
            groups = new ArrayList<>(result);
        }

        //Collections.sort(groups);
        System.out.println("Grupos generados: ");
        groups.stream().forEach((group) -> {
            System.out.println("\t" + group);
        });

        groups.stream().forEach((group1) -> {
            groups.stream().filter((group2) -> !(group1 == group2)).forEach((group2) -> {
                Set<Integer> intersection = new TreeSet<>();
                intersection.addAll(group1.getGroupMembers());
                intersection.retainAll(group2.getGroupMembers());
                if (!intersection.isEmpty()) {
                    Assert.fail("Group " + group1 + " is sharing users with group " + group2 + "  ( intersection" + intersection + ")");
                }
            });
        });
    }

    /**
     * Test of shuffle method, of class SimilarMembers.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testTooManyGroupsAsked() {
        final int groupSizeValue = 5;
        final int numGroupsValue = 189;
        final int numMembersCandidate = 1;

        System.out.println(SimilarMembersTest.class + ".testShuffle()");

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");
        datasetLoader.getRatingsDataset();

        SimilarMembers instance = new SimilarMembers(numGroupsValue, groupSizeValue, numMembersCandidate);

        Collection<GroupOfUsers> result = instance.shuffle(datasetLoader);
    }
}
