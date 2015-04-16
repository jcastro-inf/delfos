package delfos.experiment.casestudy.defaultcase;

import delfos.experiment.casestudy.defaultcase.DefaultCaseStudy;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.factories.EvaluationMeasuresFactory;
import delfos.experiment.validation.predictionprotocol.NoPredictionProtocol;
import delfos.experiment.validation.validationtechnique.HoldOut_Ratings;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.collaborativefiltering.knn.memorybased.nwr.KnnMemoryBasedNWR;

/**
 *
 * @version 29-may-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class DefaultCaseStudyTest extends DelfosTest {

    public DefaultCaseStudyTest() {
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
     * Test of execute method, of class DefaultCaseStudy.
     */
    @Test
    public void testExecute() {
        System.out.println("DefaultCaseStudyTest.execute()");
        DefaultCaseStudy instance = new DefaultCaseStudy(
                new KnnMemoryBasedNWR(),
                new RandomDatasetLoader(),
                new HoldOut_Ratings(),
                new NoPredictionProtocol(),
                new RelevanceCriteria(),
                EvaluationMeasuresFactory.getInstance().getAllContentBasedEvaluationMeasures(),
                10);
        instance.execute();
    }
}
