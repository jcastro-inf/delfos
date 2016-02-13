package delfos.io.xml.casestudy;

import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.ExperimentListerner_default;
import delfos.experiment.casestudy.CaseStudy;
import delfos.experiment.casestudy.defaultcase.DefaultCaseStudy;
import delfos.experiment.validation.predictionprotocol.NoPredictionProtocol;
import delfos.experiment.validation.validationtechnique.CrossFoldValidation_Ratings;
import delfos.experiment.validation.validationtechnique.HoldOut_Ratings;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.factories.EvaluationMeasuresFactory;
import delfos.rs.RecommenderSystem;
import delfos.rs.bias.PredictUserItemBias;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class CaseStudyXMLTest {

    public CaseStudyXMLTest() {
    }

    @Test
    public void testCaseStudyToXMLFile_OneExecutionOneSplit() {

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        RecommenderSystem recommenderSystem = new PredictUserItemBias();

        ValidationTechnique validationTechnique = new HoldOut_Ratings();
        validationTechnique.setSeedValue(123456);

        CaseStudy caseStudy = new DefaultCaseStudy(
                recommenderSystem,
                datasetLoader,
                validationTechnique,
                new NoPredictionProtocol(),
                new RelevanceCriteria(4),
                EvaluationMeasuresFactory.getInstance().getAllClasses(),
                1);
        caseStudy.addExperimentListener(new ExperimentListerner_default(System.out, 5000));
        caseStudy.execute();

        CaseStudyXML.saveCaseResults(caseStudy);

    }

    @Test
    public void testCaseStudyToXMLFile_MultipleExecutionsOneSplit() {

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        RecommenderSystem recommenderSystem = new PredictUserItemBias();

        ValidationTechnique validationTechnique = new HoldOut_Ratings();
        validationTechnique.setSeedValue(123456);

        CaseStudy caseStudy = new DefaultCaseStudy(
                recommenderSystem,
                datasetLoader,
                validationTechnique,
                new NoPredictionProtocol(),
                new RelevanceCriteria(4),
                EvaluationMeasuresFactory.getInstance().getAllClasses(),
                2);
        caseStudy.addExperimentListener(new ExperimentListerner_default(System.out, 5000));
        caseStudy.execute();

        CaseStudyXML.saveCaseResults(caseStudy);

    }

    @Test
    public void testCaseStudyToXMLFile_OneExecutionMultipleSplits() {

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        RecommenderSystem recommenderSystem = new PredictUserItemBias();

        ValidationTechnique validationTechnique = new CrossFoldValidation_Ratings();
        validationTechnique.setParameterValue(CrossFoldValidation_Ratings.NUM_PARTITIONS, 2);
        validationTechnique.setSeedValue(123456);

        CaseStudy caseStudy = new DefaultCaseStudy(
                recommenderSystem,
                datasetLoader,
                validationTechnique,
                new NoPredictionProtocol(),
                new RelevanceCriteria(4),
                EvaluationMeasuresFactory.getInstance().getAllClasses(),
                1);
        caseStudy.addExperimentListener(new ExperimentListerner_default(System.out, 5000));
        caseStudy.execute();

        CaseStudyXML.saveCaseResults(caseStudy);

    }

    @Test
    public void testCaseStudyToXMLFile_MultipleExecutionsMultipleSplits() {

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        RecommenderSystem recommenderSystem = new PredictUserItemBias();

        ValidationTechnique validationTechnique = new CrossFoldValidation_Ratings();
        validationTechnique.setParameterValue(CrossFoldValidation_Ratings.NUM_PARTITIONS, 2);
        validationTechnique.setSeedValue(123456);

        CaseStudy caseStudy = new DefaultCaseStudy(
                recommenderSystem,
                datasetLoader,
                validationTechnique,
                new NoPredictionProtocol(),
                new RelevanceCriteria(4),
                EvaluationMeasuresFactory.getInstance().getAllClasses(),
                2);
        caseStudy.addExperimentListener(new ExperimentListerner_default(System.out, 5000));
        caseStudy.execute();

        CaseStudyXML.saveCaseResults(caseStudy);

    }
}
