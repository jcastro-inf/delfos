/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delfos.experiment.casestudy;

import java.util.Collection;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.experiment.ExperimentAdapter;
import delfos.experiment.ExperimentListener;
import delfos.experiment.validation.predictionprotocol.PredictionProtocol;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.results.MeasureResult;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.RecommenderSystem;
import delfos.rs.RecommenderSystemBuildingProgressListener;

/**
 *
 * @author Jorge Castro Gallardo
 *
 * @version 27-feb-2014
 */
public abstract class CaseStudy extends ExperimentAdapter implements RecommenderSystemBuildingProgressListener {

    public CaseStudy() {
        addParameter(SEED);
    }

    public abstract void addCaseStudyPropertyListener(CaseStudyParameterChangedListener listener);

    /**
     * Realiza la ejecución del caso de uso.
     *
     * @throws CannotLoadContentDataset Si el dataset de contenido no sepuede
     * recuperar.
     * @throws CannotLoadRatingsDataset Si el dataset de valoraciones no se
     * puede recuperar.
     */
    public abstract void execute() throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset;

    public abstract DatasetLoader<? extends Rating> getDatasetLoader();

    public abstract Collection<EvaluationMeasure> getEvaluationMeasures();

    @Override
    public abstract int getExecutionProgressPercent();

    @Override
    public abstract long getExecutionProgressRemainingTime();

    @Override
    public abstract String getExecutionProgressTask();

    @Override
    public abstract int getExperimentProgressPercent();

    @Override
    public abstract long getExperimentProgressRemainingTime();

    @Override
    public abstract String getExperimentProgressTask();

    public abstract MeasureResult getMeasureResult(EvaluationMeasure em, int execution, int split);

    /**
     * Método que devuelve el resultado agregado de una medida de evaluación.
     *
     * @param em Medida de evaluación.
     * @return Resultado de la medida de evaluación.
     */
    public abstract MeasureResult getMeasureResult(EvaluationMeasure em);

    public abstract int getNumExecutions();

    @Override
    public abstract int getNumVueltas();

    /**
     * Devuelve el número de particiones.
     *
     * @return
     */
    public abstract int getNumberOfSplits();

    /**
     * Devuelve el protocolo de predicción que usa el caso de estudio.
     *
     * @return Protocolo de predicción aplicado.
     */
    public abstract PredictionProtocol getPredictionProtocol();

    public abstract RecommenderSystem<? extends Object> getRecommenderSystem();

    public abstract RelevanceCriteria getRelevanceCriteria();

    public abstract ValidationTechnique getValidationTechnique();

    @Override
    public abstract int getVueltaActual();

    @Override
    public abstract boolean hasErrors();

    @Override
    public abstract boolean isFinished();

    public abstract boolean isRunning();

    public abstract void removeCaseStudyPropertyChangedListener(CaseStudyParameterChangedListener listener);

    @Override
    public abstract void removeExperimentListener(ExperimentListener listener);

    public abstract void setDatasetLoader(DatasetLoader<? extends Rating> loader);

    public abstract void setEvaluationMeasures(Collection<EvaluationMeasure> evaluationMeasures);

    public abstract void setExecutionNumber(int nExec);

    @Override
    public abstract void setSeedValue(long seedValue);

    public abstract void setValidation(ValidationTechnique validacionInterface);

    // METODOS PARA OBTENER TIEMPOS DEL EXPERIMENTO
    /**
     * Devuelve el tiempo de ejecución en milisegundos de una partición concreta
     * de una ejecución dada
     *
     * @param execution Ejecución para la que se quiere conocer el tiempo
     * @param set Indice del subconjunto del dataset
     * @return tiempo de ejecución en milisegundos
     */
    public abstract long getBuildTime(int execution, int set);

    /**
     * Devuelve el tiempo medio de la consttrucción del modelo de recomendación.
     *
     * @return
     */
    public abstract double getAggregateBuildTime();

    /**
     * Devuelve el tiempo de ejecución en milisegundos de una partición concreta
     * de una ejecución dada
     *
     * @param execution Ejecución para la que se quiere conocer el tiempo
     * @param set Indice del subconjunto del dataset
     * @return tiempo de ejecución en milisegundos
     */
    public abstract long getRecommendationTime(int execution, int set);

    public abstract long getAggregateRecommendationTime();

    /**
     * Método que devuelve el resultado agregado de una medida de evaluación.
     *
     * @param em Medida de evaluación
     * @return Resultado de la medida de evaluación
     */
    public abstract MeasureResult getAggregateMeasureResult(EvaluationMeasure em);

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.CASE_STUDY;
    }
}
