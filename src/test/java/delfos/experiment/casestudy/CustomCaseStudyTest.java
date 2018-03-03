package delfos.experiment.casestudy;

import delfos.constants.DelfosTest;
import delfos.experiment.Experiment;
import delfos.factories.ExperimentFactory;
import delfos.io.xml.experiment.ExperimentXML;
import org.jdom2.JDOMException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class CustomCaseStudyTest extends DelfosTest{

    @BeforeClass
    public static void beforeClassTests(){
        ExperimentFactory.getInstance().addClass(CustomCaseStudyMock.class);
    }


    @Test
    public void test() throws JDOMException, IOException {
        File file = new File(getTemporalDirectoryForTest().getPath()+
                File.separator+"CustomCaseStudyMock.xml");

        Experiment originalExperiment = new CustomCaseStudyMock();
        ExperimentXML.saveExperiment(originalExperiment,file);

        Experiment experimentLoaded = ExperimentXML.loadExperiment(file);

        Assert.assertTrue(
                "Original and loaded experiment do not match",
                experimentLoaded.equals(originalExperiment)
        );
    }

    @Test
    public void testDifferent() throws JDOMException, IOException {
        File file = new File(getTemporalDirectoryForTest().getPath()+
                File.separator+"CustomCaseStudyMock.xml");

        Experiment originalExperiment = new CustomCaseStudyMock();
        originalExperiment.setParameterValue(CaseStudy.NUM_EXECUTIONS,100);


        ExperimentXML.saveExperiment(originalExperiment,file);

        originalExperiment.setParameterValue(CaseStudy.NUM_EXECUTIONS,50);

        Experiment experimentLoaded = ExperimentXML.loadExperiment(file);

        Assert.assertFalse(
                "Original and loaded experiment match and they should not because of different NUM_EXECUTIONS",
                experimentLoaded.equals(originalExperiment)
        );
    }
}
