package delfos.experiment.casestudy;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.factories.EvaluationMeasuresFactory;
import delfos.experiment.validation.predictionprotocol.PredictionProtocol;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.GenericRecommenderSystem;

/**
 * Almacena los valores y resultados de un caso de estudio de sistemas de
 * recomendación tradicionales ya ejecutado.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 19-Noviembre-2013
 */
public class CaseStudyResults {

    private final GenericRecommenderSystem<Object> recommenderSystem;
    private final DatasetLoader<? extends Rating> datasetLoader;
    private final ValidationTechnique validationTechnique;
    private final PredictionProtocol predictionProtocol;
    private final Map<EvaluationMeasure, Double> evaluationMeasuresResults;
    private final long buildTime;
    private final long recommendationTime;

    public CaseStudyResults(
            GenericRecommenderSystem<Object> recommenderSystem,
            DatasetLoader<? extends Rating> datasetLoader,
            ValidationTechnique validationTechnique,
            PredictionProtocol predictionProtocol,
            Map<EvaluationMeasure, Double> evaluationMeasuresResults,
            long buildTime,
            long recommendationTime) {

        this.recommenderSystem = recommenderSystem;
        this.validationTechnique = validationTechnique;
        this.predictionProtocol = predictionProtocol;
        this.datasetLoader = datasetLoader;
        this.evaluationMeasuresResults = evaluationMeasuresResults;
        this.buildTime = buildTime;
        this.recommendationTime = recommendationTime;
    }

    public GenericRecommenderSystem<Object> getRecommenderSystem() {
        return recommenderSystem;
    }

    public ValidationTechnique getValidationTechnique() {
        return validationTechnique;
    }

    public PredictionProtocol getPredictionProtocol() {
        return predictionProtocol;
    }

    public Collection<EvaluationMeasure> getEvaluationMeasures() {
        return EvaluationMeasuresFactory.getInstance().getAllClasses();
    }

    public DatasetLoader<? extends Rating> getDatasetLoader() {
        return datasetLoader;
    }

    public Map<EvaluationMeasure, Double> getEvaluationMeasuresResults() {
        return new TreeMap<EvaluationMeasure, Double>(evaluationMeasuresResults);
    }

    public long getBuildTime() {
        return buildTime;
    }

    public long getRecommendationTime() {
        return recommendationTime;
    }
}
