package delfos.experiment.casestudy;

import delfos.experiment.casestudy.CaseStudy;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.experiment.validation.predictionprotocol.NoPredictionProtocol;
import delfos.experiment.validation.validationtechnique.HoldOut_Ratings;
import delfos.factories.EvaluationMeasuresFactory;
import delfos.io.xml.casestudy.CaseStudyXML;
import delfos.io.xml.experiment.ExperimentXML;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import org.jdom2.JDOMException;
import org.junit.*;

import java.io.File;
import java.io.IOException;

/**
 *
 * @version 29-may-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class CaseStudyTest extends DelfosTest {

    public CaseStudyTest() {
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
     * Test of execute method, of class CaseStudy.
     */
    @Test
    public void testExecute() throws JDOMException, IOException {
        CaseStudy caseStudyKnnMemoryExecuted = CaseStudy.create(
                new KnnMemoryBasedCFRS(),
                new RandomDatasetLoader(),
                new HoldOut_Ratings(),
                new NoPredictionProtocol(),
                new RelevanceCriteria(),
                EvaluationMeasuresFactory.getInstance().getAllContentBasedEvaluationMeasures(),
                10);
        caseStudyKnnMemoryExecuted.execute();

        File resultsFile = new File(getTemporalDirectoryForTest(this.getClass()) + File.separator + caseStudyKnnMemoryExecuted.getAlias());

        ExperimentXML.saveExperiment(caseStudyKnnMemoryExecuted,resultsFile);

        CaseStudy caseStudyLoaded = (CaseStudy) ExperimentXML.loadExperiment(resultsFile);

        Assert.assertTrue("Case studies do not match!",caseStudyLoaded.equals(caseStudyKnnMemoryExecuted));
    }
}
