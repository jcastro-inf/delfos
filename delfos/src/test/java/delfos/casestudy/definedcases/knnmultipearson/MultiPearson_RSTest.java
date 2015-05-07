package delfos.casestudy.definedcases.knnmultipearson;

import delfos.Constants;
import delfos.common.FileUtilities;
import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.experiment.casestudy.CaseStudy;
import delfos.experiment.casestudy.cluster.TuringPreparator;
import delfos.experiment.casestudy.defaultcase.DefaultCaseStudy;
import delfos.experiment.validation.predictionprotocol.NoPredictionProtocol;
import delfos.experiment.validation.predictionprotocol.PredictionProtocol;
import delfos.experiment.validation.validationtechnique.HoldOut_Ratings;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.factories.EvaluationMeasuresFactory;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.RecommenderSystem;
import delfos.rs.bufferedrecommenders.RecommenderSystem_bufferedRecommendations;
import delfos.rs.collaborativefiltering.knn.memorybased.multicorrelation.jaccard.RSTest;
import delfos.rs.collaborativefiltering.predictiontechniques.WeightedSum;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import delfos.similaritymeasures.useruser.RelevanceFactor;
import delfos.similaritymeasures.useruser.UserUserMultipleCorrelationCoefficient;
import delfos.similaritymeasures.useruser.UserUserSimilarityWrapper;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;

/**
 * Crea los experimentos del congreso ISKE 2014 (dentro de flins).
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 22-ene-2014
 */
public class MultiPearson_RSTest {

    public static final long seed = 1393231163086L;

    @Test
    public void generateCaseXML() {

        String experimentBaseDirectoryString = Constants.getTempDirectory().getAbsolutePath() + File.separator + "experiments" + File.separator + MultiPearson_RSTest.class.getSimpleName() + File.separator;
        File experimentBaseDirectory = new File(experimentBaseDirectoryString);

        FileUtilities.cleanDirectory(experimentBaseDirectory);

        final DatasetLoader<? extends Rating> datasetLoader = new RandomDatasetLoader();
        final int numEjecuciones = 1;
        final Collection<EvaluationMeasure> evaluationMeasures = EvaluationMeasuresFactory.getInstance().getAllClasses();
        final RelevanceCriteria criteria = new RelevanceCriteria(4);
        final PredictionProtocol predictionProtocol = new NoPredictionProtocol();
        final ValidationTechnique validationTechniqueValue = new HoldOut_Ratings();
        Iterable<RecommenderSystem> recommenderSystems = getRS();

        List<CaseStudy> cases = new ArrayList<>();

        for (RecommenderSystem recommenderSystem : recommenderSystems) {

            CaseStudy recommenderSystemCaseStudy = new DefaultCaseStudy(
                    recommenderSystem,
                    datasetLoader,
                    validationTechniqueValue,
                    predictionProtocol,
                    criteria,
                    evaluationMeasures,
                    numEjecuciones);
            recommenderSystemCaseStudy.setSeedValue(seed);
            cases.add(recommenderSystemCaseStudy);
        }

        TuringPreparator turingPreparator = new TuringPreparator();
        turingPreparator.prepareExperiment(experimentBaseDirectory, cases, new ConfiguredDatasetLoader("ml-100k"));
    }

    private static Iterable<RecommenderSystem> getRS() {
        LinkedList<RecommenderSystem> recommenderSystems = new LinkedList<>();

        DecimalFormat format = new DecimalFormat("000");

        int[] neighborhoodSizes = {100, 150, 200, 250, 300};
        //int[] neighborhoodSizes = {999};

        for (int neighborhoodSize : neighborhoodSizes) {
            String neighborhoodSizeSufix = "_nei=" + format.format(neighborhoodSize);
            {
                //Pearson
                String alias = "KnnMulti_Pearson" + neighborhoodSizeSufix;
                RSTest recommenderSystem
                        = new RSTest(
                                new RelevanceFactor(0),
                                new UserUserSimilarityWrapper(new PearsonCorrelationCoefficient()),
                                neighborhoodSize,
                                new WeightedSum());
                RecommenderSystem_bufferedRecommendations recommenderSystem_bufferedRecommendations
                        = new RecommenderSystem_bufferedRecommendations(
                                new File("." + File.separator + "bufferedRecommendations" + File.separator + alias),
                                recommenderSystem
                        );

                recommenderSystem.setAlias(alias);
                recommenderSystem_bufferedRecommendations.setAlias(alias);

                recommenderSystems.add(recommenderSystem_bufferedRecommendations);
            }
        }

        for (int neighborhoodSize : neighborhoodSizes) {
            String neighborhoodSizeSufix = "_nei=" + format.format(neighborhoodSize);
            {
                //MultiPearson + relevance
                String alias = "KnnMulti_Pearson(rf=30)" + neighborhoodSizeSufix;
                RSTest recommenderSystem
                        = new RSTest(
                                new RelevanceFactor(30),
                                new UserUserSimilarityWrapper(new PearsonCorrelationCoefficient()),
                                neighborhoodSize,
                                new WeightedSum()
                        );

                RecommenderSystem_bufferedRecommendations recommenderSystem_bufferedRecommendations
                        = new RecommenderSystem_bufferedRecommendations(
                                new File("." + File.separator + "bufferedRecommendations" + File.separator + alias),
                                recommenderSystem
                        );

                recommenderSystem.setAlias(alias);
                recommenderSystem_bufferedRecommendations.setAlias(alias);

                recommenderSystems.add(recommenderSystem_bufferedRecommendations);
            }
        }

        for (int neighborhoodSize : neighborhoodSizes) {
            String neighborhoodSizeSufix = "_nei=" + format.format(neighborhoodSize);
            {
                //MultiPearson + relevance
                String alias = "KnnMulti_MultiPearson" + neighborhoodSizeSufix;
                RSTest recommenderSystem
                        = new RSTest(
                                new RelevanceFactor(0),
                                new UserUserMultipleCorrelationCoefficient(new PearsonCorrelationCoefficient()),
                                neighborhoodSize,
                                new WeightedSum()
                        );

                RecommenderSystem_bufferedRecommendations recommenderSystem_bufferedRecommendations
                        = new RecommenderSystem_bufferedRecommendations(
                                new File("." + File.separator + "bufferedRecommendations" + File.separator + alias),
                                recommenderSystem
                        );

                recommenderSystem.setAlias(alias);
                recommenderSystem_bufferedRecommendations.setAlias(alias);

                recommenderSystems.add(recommenderSystem_bufferedRecommendations);
            }
        }

        for (int neighborhoodSize : neighborhoodSizes) {
            String neighborhoodSizeSufix = "_nei=" + format.format(neighborhoodSize);
            {
                //MultiPearson + relevance
                String alias = "KnnMulti_MultiPearson(rf=30)" + neighborhoodSizeSufix;
                RSTest recommenderSystem
                        = new RSTest(
                                new RelevanceFactor(30),
                                new UserUserMultipleCorrelationCoefficient(new PearsonCorrelationCoefficient()),
                                neighborhoodSize,
                                new WeightedSum()
                        );

                RecommenderSystem_bufferedRecommendations recommenderSystem_bufferedRecommendations
                        = new RecommenderSystem_bufferedRecommendations(
                                new File("." + File.separator + "bufferedRecommendations" + File.separator + alias),
                                recommenderSystem
                        );

                recommenderSystem.setAlias(alias);
                recommenderSystem_bufferedRecommendations.setAlias(alias);

                recommenderSystems.add(recommenderSystem_bufferedRecommendations);
            }
        }
        return recommenderSystems;
    }
}
