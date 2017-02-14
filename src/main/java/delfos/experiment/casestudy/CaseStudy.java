/*
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package delfos.experiment.casestudy;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.ExperimentAdapter;
import delfos.experiment.ExperimentListener;
import delfos.experiment.validation.predictionprotocol.NoPredictionProtocol;
import delfos.experiment.validation.predictionprotocol.PredictionProtocol;
import delfos.experiment.validation.validationtechnique.CrossFoldValidation_Ratings;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.results.MeasureResult;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.RecommendationModelBuildingProgressListener;
import delfos.rs.RecommenderSystem;
import delfos.rs.nonpersonalised.randomrecommender.RandomRecommender;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 27-feb-2014
 */
public abstract class CaseStudy extends ExperimentAdapter implements RecommendationModelBuildingProgressListener {

    /**
     * Parameter to store the dataset used in this case study
     */
    public static final Parameter DATASET_LOADER = new Parameter(
            DatasetLoader.class.getSimpleName(),
            new ParameterOwnerRestriction(DatasetLoader.class, new ConfiguredDatasetLoader("ml-100k")));

    public static final Parameter VALIDATION_TECHNIQUE = new Parameter(
            ValidationTechnique.class.getSimpleName(),
            new ParameterOwnerRestriction(ValidationTechnique.class, new CrossFoldValidation_Ratings()));

    public static final Parameter PREDICTION_PROTOCOL = new Parameter(
            PredictionProtocol.class.getSimpleName(),
            new ParameterOwnerRestriction(PredictionProtocol.class, new NoPredictionProtocol()));

    /*------------------------- TECHNIQUE PARAMETERS -------------------------*/
    public static final Parameter RECOMMENDER_SYSTEM = new Parameter(
            RecommenderSystem.class.getSimpleName(),
            new ParameterOwnerRestriction(RecommenderSystem.class, new RandomRecommender()));

    public static final Parameter NUM_EXECUTIONS = new Parameter(
            "numExecutions",
            new IntegerParameter(1, 100000000, 1));

    public CaseStudy() {
        addParameter(SEED);
        addParameter(NUM_EXECUTIONS);
        addParameter(DATASET_LOADER);
        addParameter(VALIDATION_TECHNIQUE);
        addParameter(PREDICTION_PROTOCOL);
        addParameter(RECOMMENDER_SYSTEM);
    }

    public abstract void addCaseStudyPropertyListener(CaseStudyParameterChangedListener listener);

    /**
     * Realiza la ejecución del caso de uso.
     *
     * @throws CannotLoadContentDataset Si el dataset de contenido no sepuede recuperar.
     * @throws CannotLoadRatingsDataset Si el dataset de valoraciones no se puede recuperar.
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

    public abstract void setEvaluationMeasures(Collection<EvaluationMeasure> evaluationMeasures);

    public abstract void setExecutionNumber(int nExec);

    @Override
    public abstract void setSeedValue(long seedValue);

    public abstract void setValidation(ValidationTechnique validacionInterface);

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

    public int hashDataValidation() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.getSeedValue());
        hash = 97 * hash + Objects.hashCode(this.getDatasetLoader());
        hash = 97 * hash + Objects.hashCode(this.getPredictionProtocol());
        hash = 97 * hash + Objects.hashCode(this.getRelevanceCriteria());
        hash = 97 * hash + Objects.hashCode(this.getValidationTechnique());
        return hash;
    }

    public int hashTechnique() {
        return this.getRecommenderSystem().hashCode();
    }

    public abstract void setAggregateResults(Map<EvaluationMeasure, MeasureResult> aggregateResults);

}
