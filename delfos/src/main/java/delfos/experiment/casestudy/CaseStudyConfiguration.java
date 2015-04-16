package delfos.experiment.casestudy;

import java.util.Collection;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.factories.EvaluationMeasuresFactory;
import delfos.experiment.validation.predictionprotocol.PredictionProtocol;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.GenericRecommenderSystem;

/**
 * Almacena los valores de un caso de estudio de sistemas de recomendación
 * tradicionales.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 15-Noviembre-2013
 */
public class CaseStudyConfiguration {

    private final GenericRecommenderSystem<Object> recommenderSystem;
    private final ValidationTechnique validationTechnique;
    private final PredictionProtocol predictionProtocol;
    private final DatasetLoader<? extends Rating> datasetLoader;
    private final RelevanceCriteria relevanceCriteria;

    public CaseStudyConfiguration(
            GenericRecommenderSystem<Object> recommenderSystem,
            DatasetLoader<? extends Rating> datasetLoader,
            ValidationTechnique validationTechnique,
            PredictionProtocol predictionProtocol,
            RelevanceCriteria relevanceCriteria) {

        this.recommenderSystem = recommenderSystem;
        this.validationTechnique = validationTechnique;
        this.predictionProtocol = predictionProtocol;
        this.datasetLoader = datasetLoader;
        this.relevanceCriteria = relevanceCriteria;

    }

    public GenericRecommenderSystem<Object> getRecommenderSystem() {
        return recommenderSystem;
    }

    public DatasetLoader<? extends Rating> getDatasetLoader() {
        return datasetLoader;
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

    public RelevanceCriteria getRelevanceCriteria() {
        return relevanceCriteria;
    }
}
