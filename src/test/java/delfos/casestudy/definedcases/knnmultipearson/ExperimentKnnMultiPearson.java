package delfos.casestudy.definedcases.knnmultipearson;

import delfos.Constants;
import delfos.common.FileUtilities;
import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.experiment.casestudy.CaseStudy;
import delfos.experiment.casestudy.defaultcase.DefaultCaseStudy;
import delfos.experiment.validation.predictionprotocol.NoPredictionProtocol;
import delfos.experiment.validation.predictionprotocol.PredictionProtocol;
import delfos.experiment.validation.validationtechnique.HoldOut_Ratings;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.factories.EvaluationMeasuresFactory;
import delfos.io.xml.casestudy.CaseStudyXML;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.RecommenderSystem;
import delfos.rs.collaborativefiltering.knn.memorybased.multicorrelation.KnnMultiCorrelation;
import delfos.rs.collaborativefiltering.predictiontechniques.WeightedSum;
import delfos.similaritymeasures.BasicSimilarityMeasure;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import delfos.similaritymeasures.useruser.UserUserMultipleCorrelationCoefficient;
import delfos.similaritymeasures.useruser.UserUserSimilarityWrapper;
import delfos.similaritymeasures.useruser.UserUserSimilarityWrapper_relevanceFactor;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import org.junit.Test;

/**
 *
 * @version 12-may-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class ExperimentKnnMultiPearson {

    public static final long seed = 1393231163086L;

    @Test
    public void generateCaseXML() {

        String directoryName = Constants.getTempDirectory().getAbsolutePath() + File.separator
                + "experiments" + File.separator
                + ExperimentKnnMultiPearson.class.getSimpleName() + File.separator;

        File directory = new File(directoryName);
        File datasetDirectory = new File(directoryName + "dataset" + File.separator);
        if (directory.exists()) {
            FileUtilities.deleteDirectoryRecursive(directory);
        }
        FileUtilities.createDirectoryPath(directory);
        FileUtilities.createDirectoryPath(datasetDirectory);

        final DatasetLoader<? extends Rating> datasetLoader = new RandomDatasetLoader();
        final Collection<EvaluationMeasure> evaluationMeasures
                = EvaluationMeasuresFactory.getInstance().getAllClasses();
        final RelevanceCriteria criteria = new RelevanceCriteria(4);
        final PredictionProtocol predictionProtocolValue = new NoPredictionProtocol();
        final ValidationTechnique validationTechniqueValue = new HoldOut_Ratings();

        for (RecommenderSystem recommenderSystem : getRS(new PearsonCorrelationCoefficient())) {
            CaseStudy CaseStudy = new DefaultCaseStudy(
                    recommenderSystem,
                    datasetLoader,
                    validationTechniqueValue,
                    predictionProtocolValue,
                    criteria,
                    evaluationMeasures,
                    1
            );

            CaseStudy.setSeedValue(seed);
            String fileName = recommenderSystem.getAlias() + ".xml";
            File file = new File(directory + File.separator + fileName);
            CaseStudyXML.saveCaseDescription(CaseStudy, file.getAbsolutePath());
        }

        //generateDatasetFile
        {
            CaseStudy caseStudy = new DefaultCaseStudy(
                    new ConfiguredDatasetLoader("ml-100k")
            );

            File file = new File(directory + File.separator + "dataset" + File.separator + "ml-100k.xml");
            CaseStudyXML.saveCaseDescription(caseStudy, file.getAbsolutePath());
        }
    }

    private static Iterable<RecommenderSystem<? extends Object>> getRS(BasicSimilarityMeasure basicSimilarityMeasure) {

        LinkedList<RecommenderSystem<? extends Object>> rsList = new LinkedList<>();

        {
            //Pearson
            KnnMultiCorrelation recommenderSystem
                    = new KnnMultiCorrelation(
                            new UserUserSimilarityWrapper(basicSimilarityMeasure),
                            30,
                            new WeightedSum());
            recommenderSystem.setAlias("KnnMulti_Pearson");
            rsList.add(recommenderSystem);

        }
        {
            //Pearson + relevance
            KnnMultiCorrelation recommenderSystem
                    = new KnnMultiCorrelation(
                            new UserUserSimilarityWrapper_relevanceFactor(new UserUserSimilarityWrapper(new PearsonCorrelationCoefficient())), 30, new WeightedSum()
                    );
            recommenderSystem.setAlias("KnnMulti_Pearson+Relevance(20)");
            rsList.add(recommenderSystem);
        }
        {
            //MultiPearson
            KnnMultiCorrelation recommenderSystem
                    = new KnnMultiCorrelation(
                            new UserUserMultipleCorrelationCoefficient(basicSimilarityMeasure),
                            30,
                            new WeightedSum());
            recommenderSystem.setAlias("KnnMulti_MultiPearson");
            rsList.add(recommenderSystem);

        }
        {
            //Pearson + relevance
            KnnMultiCorrelation recommenderSystem
                    = new KnnMultiCorrelation(
                            new UserUserSimilarityWrapper_relevanceFactor(new UserUserMultipleCorrelationCoefficient(new PearsonCorrelationCoefficient())), 30, new WeightedSum()
                    );
            recommenderSystem.setAlias("KnnMulti_MultiPearson+Relevance(20)");
            rsList.add(recommenderSystem);
        }

        return rsList;
    }

}
