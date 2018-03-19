package delfos.experiment.casestudy;

import delfos.CommandLineParametersError;
import delfos.ConsoleParameters;
import delfos.common.FileUtilities;
import delfos.constants.DelfosTest;
import delfos.experiment.Experiment;
import delfos.experiment.casestudy.cluster.TuringPreparator;
import delfos.factories.ExperimentFactory;
import delfos.io.xml.experiment.ExperimentXML;
import org.jdom2.JDOMException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomCaseStudyTest extends DelfosTest{

    @BeforeClass
    public static void beforeClassTests(){
        ExperimentFactory.getInstance().addClass(CustomCaseStudyMock.class);
    }


    @Test
    public void test() throws JDOMException, IOException {
        File file = new File(getTemporalDirectoryForTest(this.getClass()).getPath()+
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
        File file = new File(getTemporalDirectoryForTest(this.getClass()).getPath()+
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

    @Test
    public void testWithTuringPreparator() throws CommandLineParametersError {

        List<Experiment> experiments = new ArrayList<>();
        experiments.add(new CustomCaseStudyMock());

        File experimentDirectory = getTemporalDirectoryForTest(this.getClass());
        FileUtilities.deleteDirectoryRecursive(experimentDirectory);

        new TuringPreparator().prepareExperimentGeneral(experiments,experimentDirectory);

        new TuringPreparator().executeExperimentsGeneral(experimentDirectory, ConsoleParameters.parseArguments(new ArrayList<>()));
    }


    @Test
    public void testSaveAndLoadExecutedCaseStudy() throws JDOMException, IOException {
        File experimentDescriptionFile = new File(getTemporalDirectoryForTest(this.getClass()).getPath()+
                File.separator+"CustomCaseStudyMock-testSaveAndLoadExecutedCaseStudy.xml");

        File experimentExecutedFile = new File(getTemporalDirectoryForTest(this.getClass()).getPath()+
                File.separator+"CustomCaseStudyMock-testSaveAndLoadExecutedCaseStudy-executed.xml");

        Experiment originalExperiment = new CustomCaseStudyMock();
        ExperimentXML.saveExperiment(originalExperiment,experimentDescriptionFile);
        Experiment experimentLoaded = ExperimentXML.loadExperiment(experimentDescriptionFile);

        Experiment experimentExecuted = (Experiment) experimentLoaded.clone();
        experimentExecuted.execute();

        ExperimentXML.saveExperiment(experimentExecuted,experimentExecutedFile);

        Experiment experimentExecutedLoaded = ExperimentXML.loadExperiment(experimentExecutedFile);

        Assert.assertTrue(
                "Original and loaded experiment match and they should not because of different NUM_EXECUTIONS",
                experimentExecutedLoaded.equals(experimentExecuted)
        );

    }
}
