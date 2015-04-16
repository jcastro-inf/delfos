package delfos.rs.trustbased.similaritymodification;

import delfos.rs.trustbased.similaritymodification.TrustModificationKnnMemory;
import org.junit.Test;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.experiment.ExperimentListerner_default;
import delfos.experiment.casestudy.CaseStudy;
import delfos.experiment.casestudy.defaultcase.DefaultCaseStudy;
import delfos.experiment.validation.predictionprotocol.NoPredictionProtocol;
import delfos.experiment.validation.validationtechnique.CrossFoldValidation_Ratings;
import delfos.factories.EvaluationMeasuresFactory;
import delfos.io.xml.casestudy.CaseStudyXML;
import static delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender.SIMILARITY_MEASURE;
import delfos.rs.trustbased.belieffunctions.LinearBelief;
import static delfos.rs.trustbased.similaritymodification.TrustModificationKnnMemory.BELIEF_DERIVATION;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;

/**
 *
 * @version 14-abr-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class TrustModificationKnnMemoryTest extends DelfosTest {

    public TrustModificationKnnMemoryTest() {
    }

    @Test
    public void linearModification() {
        System.out.println("linearModification");

        DatasetLoader<? extends Rating> datasetLoader;
        //datasetLoaer = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");
        {
            RandomDatasetLoader randomDataset = new RandomDatasetLoader();

            randomDataset.setParameterValue(RandomDatasetLoader.ratings_numUsers, 100);
            randomDataset.setParameterValue(RandomDatasetLoader.ratings_numItems, 100);
            randomDataset.setParameterValue(RandomDatasetLoader.ratings_loadFactor, 0.2);
            randomDataset.setSeedValue(5);

            datasetLoader = randomDataset;
        }

        TrustModificationKnnMemory recommenderSystem = new TrustModificationKnnMemory();
        recommenderSystem.setParameterValue(SIMILARITY_MEASURE, new PearsonCorrelationCoefficient());

        recommenderSystem.setParameterValue(BELIEF_DERIVATION, new LinearBelief());

        CaseStudy caseStudy = new DefaultCaseStudy(
                recommenderSystem,
                datasetLoader,
                new CrossFoldValidation_Ratings(),
                new NoPredictionProtocol(),
                new RelevanceCriteria(4),
                EvaluationMeasuresFactory.getInstance().getAllClasses(),
                1);
        caseStudy.addExperimentListener(new ExperimentListerner_default(System.out, 5000));
        caseStudy.execute();

        CaseStudyXML.saveCaseResults(caseStudy);
    }
}
