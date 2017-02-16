package delfos.casestudy.definedcases;

import delfos.Constants;
import delfos.common.FileUtilities;
import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.experiment.casestudy.CaseStudy;
import delfos.experiment.casestudy.cluster.TuringPreparator;
import delfos.experiment.casestudy.CaseStudy;
import delfos.experiment.validation.predictionprotocol.AllButOne;
import delfos.experiment.validation.predictionprotocol.PredictionProtocol;
import delfos.experiment.validation.validationtechnique.NoPartitions;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.factories.EvaluationMeasuresFactory;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.RecommenderSystem;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.collaborativefiltering.predictiontechniques.PredictionTechnique;
import delfos.rs.collaborativefiltering.predictiontechniques.WeightedSum;
import delfos.similaritymeasures.CollaborativeSimilarityMeasure;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;

/**
 * Crea los experimentos del congreso ISKE 2014 (dentro de flins).
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 22-ene-2014
 */
public class OptimizationKnnMemoryBasedNWR {

    public static final long seed = 1393231163086L;

    @Test
    public void generateCaseXML() {

        String experimentBaseDirectoryString = Constants.getTempDirectory().getAbsolutePath() + File.separator + "experiments" + File.separator + "OptimizationKnnMemoryBasedNWR" + File.separator;
        File experimentBaseDirectory = new File(experimentBaseDirectoryString);

        FileUtilities.cleanDirectory(experimentBaseDirectory);

        final DatasetLoader<? extends Rating> datasetLoader = new RandomDatasetLoader();
        final int numEjecuciones = 1;
        final Collection<EvaluationMeasure> evaluationMeasures = EvaluationMeasuresFactory.getInstance().getAllClasses();
        final RelevanceCriteria criteria = new RelevanceCriteria(4);
        final PredictionProtocol predictionProtocol = new AllButOne();
        final ValidationTechnique validationTechniqueValue = new NoPartitions();
        Iterable<RecommenderSystem> recommenderSystems = getRS();

        List<CaseStudy> cases = new ArrayList<>();

        for (RecommenderSystem recommenderSystem : recommenderSystems) {

            CaseStudy recommenderSystemCaseStudy = new CaseStudy(
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
        LinkedList<RecommenderSystem> recommenders = new LinkedList<>();

        int i = 0;

        int stringLength = 3;

        Collection<CollaborativeSimilarityMeasure> similarityMeasures = getSimilarityMeasures();
        Integer[] relevanceFactors = {30};
        boolean[] inverseFrequencies = {false};
        Double[] defaultRatings = {null};
        double[] caseAmplificationValues = {1};
        int[] neighborhoodSizes = {1, 5, 10, 20, 30, 40, 50, 75, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};
        //int[] neighborhoodSizes = {50};
        List<PredictionTechnique> predictionTechniques = getPredictionTechniques();

        for (CollaborativeSimilarityMeasure similarityMeasure : similarityMeasures) {
            for (Integer relevanceFactor : relevanceFactors) {
                for (Double defaultRating : defaultRatings) {
                    for (boolean inverseFrequency : inverseFrequencies) {
                        for (double caseAmplification : caseAmplificationValues) {
                            for (int neighborhoodSize : neighborhoodSizes) {
                                for (PredictionTechnique predictionTechnique : predictionTechniques) {

                                    KnnMemoryBasedCFRS rs = new KnnMemoryBasedCFRS();

                                    rs.setSIMILARITY_MEASURE(similarityMeasure);
                                    rs.setRELEVANCE_FACTOR_VALUE(relevanceFactor);
                                    rs.setDEFAULT_RATING_VALUE(defaultRating);
                                    rs.setINVERSE_FREQUENCY(inverseFrequency);
                                    rs.setCASE_AMPLIFICATION(caseAmplification);
                                    rs.setNeighborhoodSize(neighborhoodSize);
                                    rs.setNeighborhoodSizeStore(999999);
                                    rs.setPREDICTION_TECHNIQUE(predictionTechnique);

                                    recommenders.add(rs);

                                    String number = Integer.toString(i);
                                    i++;
                                    while (number.length() < stringLength) {
                                        number = "0" + number;
                                    }

                                    StringBuilder str = new StringBuilder();

                                    str.append(number).append("_");
                                    str.append(similarityMeasure.getName()).append("_");
                                    str.append(relevanceFactor).append("_");
                                    str.append(defaultRating).append("_");
                                    str.append(inverseFrequency).append("_");
                                    str.append(caseAmplification).append("_");
                                    str.append(neighborhoodSize).append("_");
                                    str.append(predictionTechnique).append("_");

                                    recommenders.getLast().setAlias(str.toString());
                                }
                            }
                        }
                    }
                }
            }
        }

        return recommenders;
    }

    public static List<PredictionTechnique> getPredictionTechniques() {
        List<PredictionTechnique> predictionTechniques;
//        predictionTechniques = PredictionTechniquesFactory.getInstance().getAllClasses();

        predictionTechniques = new ArrayList<>();
        predictionTechniques.add(new WeightedSum());

        return predictionTechniques;
    }

    public static Collection<CollaborativeSimilarityMeasure> getSimilarityMeasures() {
        Collection<CollaborativeSimilarityMeasure> similarityMeasures;
//        similarityMeasures = SimilarityMeasuresFactory.getInstance().getCollaborativeSimilarityMeasures(KnnMemoryBasedNWR.class);
//        for (Iterator<CollaborativeSimilarityMeasure> it = similarityMeasures.iterator(); it.hasNext();) {
//            CollaborativeSimilarityMeasure collaborativeSimilarityMeasure = it.next();
//            if (collaborativeSimilarityMeasure instanceof Tanimoto) {
//                it.remove();
//            }
//        }

        similarityMeasures = new ArrayList<>();
        similarityMeasures.add(new PearsonCorrelationCoefficient());
//        similarityMeasures.add(new CosineCoefficient());
        return similarityMeasures;
    }
}
