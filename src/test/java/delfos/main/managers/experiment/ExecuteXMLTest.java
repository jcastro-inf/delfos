package delfos.main.managers.experiment;

import delfos.CommandLineParametersError;
import delfos.ConsoleParameters;
import delfos.common.FileUtilities;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.experiment.casestudy.CaseStudy;
import delfos.experiment.casestudy.cluster.TuringPreparator;
import delfos.experiment.validation.predictionprotocol.NoPredictionProtocol;
import delfos.experiment.validation.validationtechnique.HoldOut_Ratings;
import delfos.factories.EvaluationMeasuresFactory;
import delfos.main.Main;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.collaborativefiltering.knn.modelbased.KnnModelBasedCFRS;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class ExecuteXMLTest extends DelfosTest {

    @Test
    public void executeMultipleFiles() throws CommandLineParametersError {
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

        List<File> experimentsXMLs = FileUtilities.findInDirectory(experimentDirectory).stream()
                .filter(file -> file.getPath().contains(File.separator + "descriptions" + File.separator))
                .collect(Collectors.toList());

        List<String> args = new ArrayList<>();

        args.add(ExecuteXML.MODE_PARAMETER);
        args.add(ExecuteXML.XML_FILE);
        args.addAll(experimentsXMLs.stream().map(file-> file.getPath()).collect(Collectors.toList()));

        args.add(ExecuteXML.PARALLEL_EXPERIMENT);
        args.add(ExecuteXML.PARALLEL_EXECUTIONS_WITHIN_EXPERIMENT);

        Main.mainWithExceptions(args.toArray(new String[0]));
    }

    @Test
    public void executeMultipleDirectories() throws CommandLineParametersError {
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

        List<String> experimentsXMLs = FileUtilities.findInDirectory(experimentDirectory).stream()
                .filter(file -> file.getPath().contains(File.separator + "descriptions" + File.separator))
                .map(file -> file.getParent())
                .collect(Collectors.toList());

        List<String> args = new ArrayList<>();

        args.add(ExecuteXML.MODE_PARAMETER);
        args.add(ExecuteXML.XML_DIRECTORY);
        args.addAll(experimentsXMLs);

        args.add(ExecuteXML.PARALLEL_EXPERIMENT);
        args.add(ExecuteXML.PARALLEL_EXECUTIONS_WITHIN_EXPERIMENT);

        Main.mainWithExceptions(args.toArray(new String[0]));
    }

}