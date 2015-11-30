package delfos.common.parameters.chain;

import delfos.common.aggregationoperators.Mean;
import delfos.common.aggregationoperators.MinimumValue;
import delfos.common.parameters.ParameterOwner;
import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.group.grs.aggregation.AggregationOfIndividualRecommendations;
import delfos.rs.collaborativefiltering.knn.memorybased.nwr.KnnMemoryBasedNWR;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class ParameterChainTest {

    public ParameterChainTest() {
    }

    /**
     * Test of obtainDataValidationParameterChains method, of class
     * ParameterChain.
     */
    @Test
    public void testObtainAllParameterChains() {
        GroupCaseStudy groupCaseStudy = new GroupCaseStudy(new ConfiguredDatasetLoader("ml-100k"));
        groupCaseStudy.setSeedValue(123456L);

        List<ParameterChain> result = ParameterChain.obtainAllParameterChains(groupCaseStudy);

        ParameterChain.printListOfChains(result);
        System.out.println("===================================");
    }

    /**
     * Test of obtainDataValidationParameterChains method, of class
     * ParameterChain.
     */
    @Test
    public void testObtainDataValidationParameterChains() {
        GroupCaseStudy groupCaseStudy = new GroupCaseStudy(new ConfiguredDatasetLoader("ml-100k"));

        List<ParameterChain> result = ParameterChain.obtainDataValidationParameterChains(groupCaseStudy);

        for (ParameterChain chain : result) {
            if (!chain.getNodes().isEmpty()) {
                assertTrue(!(chain.getNodes().get(0).getParameterOwner() instanceof GroupRecommenderSystem));
            }
        }
    }

    @Test
    public void testObtainTechniqueParameterChains() {
        GroupCaseStudy groupCaseStudy = new GroupCaseStudy(new ConfiguredDatasetLoader("ml-100k"));
        groupCaseStudy.setGroupRecommenderSystem(new AggregationOfIndividualRatings(new KnnMemoryBasedNWR(), new Mean()));

        List<ParameterChain> result = ParameterChain.obtainTechniqueParameterChains(groupCaseStudy);

        for (ParameterChain chain : result) {
            assertTrue(chain.getNodes().get(0).getParameterOwner() instanceof GroupRecommenderSystem);
        }
    }

    /**
     * Test of areCompatible method, of class ParameterChain.
     */
    @Test
    public void testAreCompatible() {

        AggregationOfIndividualRatings aoiRatingsMean = new AggregationOfIndividualRatings(new KnnMemoryBasedNWR(), new Mean());
        AggregationOfIndividualRatings aoiRatingsMinimum = new AggregationOfIndividualRatings(new KnnMemoryBasedNWR(), new MinimumValue());
        AggregationOfIndividualRecommendations aoiRecommendationsMinimum = new AggregationOfIndividualRecommendations(new KnnMemoryBasedNWR(), new MinimumValue());

        ParameterChain aoiRatingsMeanChain = new ParameterChain(aoiRatingsMean).createWithLeaf(AggregationOfIndividualRatings.AGGREGATION_OPERATOR, new Mean());
        ParameterChain aoiRatingsMinChain = new ParameterChain(aoiRatingsMinimum).createWithLeaf(AggregationOfIndividualRatings.AGGREGATION_OPERATOR, new MinimumValue());
        ParameterChain aoiRecommendationsMinChain = new ParameterChain(aoiRecommendationsMinimum).createWithLeaf(AggregationOfIndividualRecommendations.AGGREGATION_OPERATOR, new MinimumValue());

        assertTrue(ParameterChain.areCompatible(aoiRatingsMeanChain, aoiRatingsMinChain));
        assertTrue(!ParameterChain.areCompatible(aoiRatingsMeanChain, aoiRecommendationsMinChain));
        assertTrue(!ParameterChain.areCompatible(aoiRatingsMinChain, aoiRecommendationsMinChain));
    }

    /**
     * Test of areCompatible method, of class ParameterChain.
     */
    @Test
    public void testAreCompatibleWithNodes() {

        AggregationOfIndividualRatings aoiRatingsMean = new AggregationOfIndividualRatings(new KnnMemoryBasedNWR(), new Mean());
        AggregationOfIndividualRatings aoiRatingsMin = new AggregationOfIndividualRatings(new KnnMemoryBasedNWR(), new MinimumValue());
        AggregationOfIndividualRecommendations aoiRecommendationsMin = new AggregationOfIndividualRecommendations(new KnnMemoryBasedNWR(), new MinimumValue());

        GroupCaseStudy aoiRatingsMeanGroupCaseStudy = new GroupCaseStudy();
        aoiRatingsMeanGroupCaseStudy.setParameterValue(GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM, aoiRatingsMean);

        GroupCaseStudy aoiRatingsMinGroupCaseStudy = new GroupCaseStudy();
        aoiRatingsMinGroupCaseStudy.setParameterValue(GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM, aoiRatingsMin);

        GroupCaseStudy aoiRecommendationsMinGroupCaseStudy = new GroupCaseStudy();
        aoiRecommendationsMinGroupCaseStudy.setParameterValue(GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM, aoiRecommendationsMin);

        ParameterChain aoiRatingsMeanChain = new ParameterChain(aoiRatingsMeanGroupCaseStudy)
                .createWithNode(GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM, aoiRatingsMean)
                .createWithLeaf(AggregationOfIndividualRatings.AGGREGATION_OPERATOR, new Mean());

        ParameterChain aoiRatingsMinChain = new ParameterChain(aoiRatingsMinGroupCaseStudy)
                .createWithNode(GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM, aoiRatingsMin)
                .createWithLeaf(AggregationOfIndividualRatings.AGGREGATION_OPERATOR, new MinimumValue());

        ParameterChain aoiRecommendationsMinChain = new ParameterChain(aoiRecommendationsMinGroupCaseStudy)
                .createWithNode(GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM, aoiRecommendationsMin)
                .createWithLeaf(AggregationOfIndividualRecommendations.AGGREGATION_OPERATOR, new MinimumValue());

        assertTrue(ParameterChain.areCompatible(aoiRatingsMeanChain, aoiRatingsMinChain));
        assertTrue(!ParameterChain.areCompatible(aoiRatingsMeanChain, aoiRecommendationsMinChain));
        assertTrue(!ParameterChain.areCompatible(aoiRatingsMinChain, aoiRecommendationsMinChain));
    }

    /**
     * Test of areSame method, of class ParameterChain.
     */
    @Test
    public void testAreSame() {
        AggregationOfIndividualRatings aoiRatingsMean = new AggregationOfIndividualRatings(new KnnMemoryBasedNWR(), new Mean());
        AggregationOfIndividualRatings aoiRatingsMean_2 = new AggregationOfIndividualRatings(new KnnMemoryBasedNWR(), new Mean());
        AggregationOfIndividualRatings aoiRatingsMin = new AggregationOfIndividualRatings(new KnnMemoryBasedNWR(), new MinimumValue());

        GroupCaseStudy aoiRatingsMeanGroupCaseStudy = new GroupCaseStudy();
        aoiRatingsMeanGroupCaseStudy.setParameterValue(GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM, aoiRatingsMean);

        GroupCaseStudy aoiRatingsMeanGroupCaseStudy_2 = new GroupCaseStudy();
        aoiRatingsMeanGroupCaseStudy_2.setParameterValue(GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM, aoiRatingsMean);

        GroupCaseStudy aoiRatingsMinGroupCaseStudy = new GroupCaseStudy();
        aoiRatingsMinGroupCaseStudy.setParameterValue(GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM, aoiRatingsMin);

        ParameterChain aoiRatingsMeanChain = new ParameterChain(aoiRatingsMeanGroupCaseStudy)
                .createWithNode(GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM, aoiRatingsMean)
                .createWithLeaf(AggregationOfIndividualRatings.AGGREGATION_OPERATOR, new Mean());

        ParameterChain aoiRatingsMeanChain_2 = new ParameterChain(aoiRatingsMeanGroupCaseStudy_2)
                .createWithNode(GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM, aoiRatingsMean_2)
                .createWithLeaf(AggregationOfIndividualRatings.AGGREGATION_OPERATOR, new Mean());

        ParameterChain aoiRatingsMinChain = new ParameterChain(aoiRatingsMinGroupCaseStudy)
                .createWithNode(GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM, aoiRatingsMin)
                .createWithLeaf(AggregationOfIndividualRatings.AGGREGATION_OPERATOR, new MinimumValue());

        assertTrue("Parameter chains are the same but the method says they are different", ParameterChain.areSame(aoiRatingsMeanChain, aoiRatingsMeanChain_2));
        assertTrue("Parameter chains are the different  but the method says they are the same", !ParameterChain.areSame(aoiRatingsMeanChain, aoiRatingsMinChain));
    }

    /**
     * Test of createAllTerminalParameterChains method, of class ParameterChain.
     */
    public void testCreateAllTerminalParameterChains() {
        ParameterOwner parameterOwner = null;
        ParameterChain instance = null;
        List<ParameterChain> expResult = null;
        List<ParameterChain> result = instance.createAllTerminalParameterChains(parameterOwner);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isCompatibleWithGroupCaseStudy method, of class ParameterChain.
     */
    @Test
    public void isCompatibleWith() {
        AggregationOfIndividualRatings aoiRatingsMean = new AggregationOfIndividualRatings(new KnnMemoryBasedNWR(), new Mean());
        AggregationOfIndividualRatings aoiRatingsMin = new AggregationOfIndividualRatings(new KnnMemoryBasedNWR(), new MinimumValue());

        GroupCaseStudy aoiRatingsMeanGroupCaseStudy = new GroupCaseStudy();
        aoiRatingsMeanGroupCaseStudy.setParameterValue(GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM, aoiRatingsMean);

        GroupCaseStudy aoiRatingsMinGroupCaseStudy = new GroupCaseStudy();
        aoiRatingsMinGroupCaseStudy.setParameterValue(GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM, aoiRatingsMin);

        ParameterChain aoiRatingsMeanChain = new ParameterChain(aoiRatingsMeanGroupCaseStudy)
                .createWithNode(GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM, aoiRatingsMean)
                .createWithLeaf(AggregationOfIndividualRatings.AGGREGATION_OPERATOR, new Mean());

        assertTrue(aoiRatingsMeanChain.isApplicableTo(aoiRatingsMinGroupCaseStudy));

    }
}
