package delfos.experiment.casestudy.cluster;

import delfos.common.FileUtilities;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.experiment.casestudy.CaseStudy;
import delfos.experiment.validation.predictionprotocol.NoPredictionProtocol;
import delfos.experiment.validation.validationtechnique.HoldOut_Ratings;
import delfos.factories.EvaluationMeasuresFactory;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.collaborativefiltering.knn.modelbased.KnnModelBasedCFRS;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TuringPreparatorTest extends DelfosTest {

    @Test
    public void test(){
        File experimentDirectory = getTemporalDirectoryForTest(this.getClass());

        FileUtilities.deleteDirectoryRecursive(experimentDirectory);

        CaseStudy caseStudyKnnMemoryExecuted = CaseStudy.create(
                new KnnMemoryBasedCFRS(),
                new RandomDatasetLoader(),
                new HoldOut_Ratings(),
                new NoPredictionProtocol(),
                new RelevanceCriteria(),
                EvaluationMeasuresFactory.getInstance().getAllContentBasedEvaluationMeasures(),
                10);

        CaseStudy caseStudyKnnModelExecuted = CaseStudy.create(
                new KnnModelBasedCFRS(),
                new RandomDatasetLoader(),
                new HoldOut_Ratings(),
                new NoPredictionProtocol(),
                new RelevanceCriteria(),
                EvaluationMeasuresFactory.getInstance().getAllContentBasedEvaluationMeasures(),
                10);

        TuringPreparator turingPreparator = new TuringPreparator();

        List<CaseStudy> caseStudies = Arrays.asList(caseStudyKnnMemoryExecuted, caseStudyKnnModelExecuted);

        turingPreparator.prepareExperimentGeneral(caseStudies,experimentDirectory);
        turingPreparator.executeExperimentsGeneral(experimentDirectory);
    }

    @Test
    public void testExecuteTwice(){
        File experimentDirectory = getTemporalDirectoryForTest(this.getClass());

        FileUtilities.deleteDirectoryRecursive(experimentDirectory);

        CaseStudy caseStudyKnnMemoryExecuted = CaseStudy.create(
                new KnnMemoryBasedCFRS(),
                new RandomDatasetLoader(),
                new HoldOut_Ratings(),
                new NoPredictionProtocol(),
                new RelevanceCriteria(),
                EvaluationMeasuresFactory.getInstance().getAllContentBasedEvaluationMeasures(),
                10);

        CaseStudy caseStudyKnnModelExecuted = CaseStudy.create(
                new KnnModelBasedCFRS(),
                new RandomDatasetLoader(),
                new HoldOut_Ratings(),
                new NoPredictionProtocol(),
                new RelevanceCriteria(),
                EvaluationMeasuresFactory.getInstance().getAllContentBasedEvaluationMeasures(),
                10);

        TuringPreparator turingPreparator = new TuringPreparator();

        List<CaseStudy> caseStudies = Arrays.asList(caseStudyKnnMemoryExecuted, caseStudyKnnModelExecuted);

        turingPreparator.prepareExperimentGeneral(caseStudies,experimentDirectory);

        turingPreparator.executeExperimentsGeneral(experimentDirectory);

        turingPreparator.executeExperimentsGeneral(experimentDirectory);
    }

}