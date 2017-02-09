package delfos.experiment.casestudy.defaultcase;

import delfos.constants.DelfosTest;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.experiment.validation.predictionprotocol.NoPredictionProtocol;
import delfos.experiment.validation.validationtechnique.HoldOut_Ratings;
import delfos.factories.EvaluationMeasuresFactory;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @version 29-may-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
        DefaultCaseStudy instance = new DefaultCaseStudy(
                new KnnMemoryBasedCFRS(),
                new RandomDatasetLoader(),
                new HoldOut_Ratings(),
                new NoPredictionProtocol(),
                new RelevanceCriteria(),
                EvaluationMeasuresFactory.getInstance().getAllContentBasedEvaluationMeasures(),
                10);
        instance.execute();
    }
}
