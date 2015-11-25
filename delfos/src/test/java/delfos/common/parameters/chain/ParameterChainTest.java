package delfos.common.parameters.chain;

import delfos.common.aggregationoperators.Mean;
import delfos.common.parameters.ParameterOwner;
import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.experiment.casestudy.CaseStudy;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.rs.collaborativefiltering.knn.memorybased.nwr.KnnMemoryBasedNWR;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class ParameterChainTest {

    public ParameterChainTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of obtainDataValidationParameterChains method, of class
     * ParameterChain.
     */
    @Test
    public void testObtainAllParameterChains() {
        System.out.println("testObtainAllParameterChains");
        GroupCaseStudy groupCaseStudy = new GroupCaseStudy(new ConfiguredDatasetLoader("ml-100k"));

        List<ParameterChain> expResult = null;
        List<ParameterChain> result = ParameterChain.obtainAllParameterChains(groupCaseStudy);

        for (ParameterChain chain : result) {
            System.out.println(chain.toString());
        }

        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtainDataValidationParameterChains method, of class
     * ParameterChain.
     */
    @Test
    public void testObtainDataValidationParameterChains() {
        System.out.println("testObtainAllParameterChains");
        GroupCaseStudy groupCaseStudy = new GroupCaseStudy(new ConfiguredDatasetLoader("ml-100k"));

        groupCaseStudy.setGroupRecommenderSystem(new AggregationOfIndividualRatings(new KnnMemoryBasedNWR(), new Mean()));

        List<ParameterChain> expResult = null;
        List<ParameterChain> result = ParameterChain.obtainDataValidationParameterChains(groupCaseStudy);

        for (ParameterChain chain : result) {
            System.out.println(chain.toString());
        }

        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of obtainDataValidationParameterChains method, of class
     * ParameterChain.
     */
    @Test
    public void testObtainAllParameterChains_ParameterOwner() {
        System.out.println("obtainAllParameterChains");
        ParameterOwner parameterOwner = null;
        List<ParameterChain> expResult = null;
        List<ParameterChain> result = ParameterChain.obtainAllParameterChains(parameterOwner);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of areCompatible method, of class ParameterChain.
     */
    @Test
    public void testAreCompatible() {
        System.out.println("areCompatible");
        List<ParameterChain> parameterChains = null;
        boolean expResult = false;
        boolean result = ParameterChain.areCompatible(parameterChains);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of areSame method, of class ParameterChain.
     */
    @Test
    public void testAreSame() {
        System.out.println("areSame");
        List<ParameterChain> parameterChains = null;
        boolean expResult = false;
        boolean result = ParameterChain.areSame(parameterChains);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createAllTerminalParameterChains method, of class ParameterChain.
     */
    @Test
    public void testCreateAllTerminalParameterChains() {
        System.out.println("createAllTerminalParameterChains");
        ParameterOwner parameterOwner = null;
        ParameterChain instance = null;
        List<ParameterChain> expResult = null;
        List<ParameterChain> result = instance.createAllTerminalParameterChains(parameterOwner);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isCompatibleWith method, of class ParameterChain.
     */
    @Test
    public void testIsCompatibleWith() {
        System.out.println("isCompatibleWith");
        ParameterOwner parameterOwner = null;
        ParameterChain instance = null;
        boolean expResult = false;
        boolean result = instance.isCompatibleWith(parameterOwner);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isCompatibleWithCaseStudy method, of class ParameterChain.
     */
    @Test
    public void testIsCompatibleWithCaseStudy() {
        System.out.println("isCompatibleWithCaseStudy");
        CaseStudy caseStudy = null;
        ParameterChain instance = null;
        boolean expResult = false;
        boolean result = instance.isCompatibleWithCaseStudy(caseStudy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isCompatibleWithGroupCaseStudy method, of class ParameterChain.
     */
    @Test
    public void testIsCompatibleWithGroupCaseStudy() {
        System.out.println("isCompatibleWithGroupCaseStudy");
        GroupCaseStudy groupCaseStudy = null;
        ParameterChain instance = null;
        boolean expResult = false;
        boolean result = instance.isCompatibleWithGroupCaseStudy(groupCaseStudy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
