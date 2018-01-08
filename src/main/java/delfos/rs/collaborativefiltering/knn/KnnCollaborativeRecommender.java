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
package delfos.rs.collaborativefiltering.knn;

import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.BooleanParameter;
import delfos.common.parameters.restriction.DoubleParameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.rs.collaborativefiltering.CollaborativeRecommender;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.collaborativefiltering.predictiontechniques.PredictionTechnique;
import delfos.rs.collaborativefiltering.predictiontechniques.WeightedSum;
import delfos.similaritymeasures.CollaborativeSimilarityMeasure;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;

public abstract class KnnCollaborativeRecommender<RecommendationModel>
        extends CollaborativeRecommender<RecommendationModel> {

    /**
     * Parámetro para almacenar el número de vecinos que se tienen en cuenta para la predicción de la valoración. Si no
     * se modifica, su valor por defecto es 20.
     */
    public static final Parameter NEIGHBORHOOD_SIZE = new Parameter("Neighborhood_size", new IntegerParameter(1, 999999, 20));

    /**
     * Parámetro para almacenar el número de vecinos que se almacenan en el perfil de cada producto. Si no se modifica,
     * su valor por defecto es 999999.
     */
    public static final Parameter NEIGHBORHOOD_SIZE_STORE = new Parameter("Neighborhood_size_store", new IntegerParameter(1, 999999, 999999));
    /**
     * Parámetro para indicar la medida de similitud que el sistema de recomendación utiliza para el cálculo de los
     * vecinos más cercanos. Si no se modifica, su valor por defecto es la suma ponderada.
     */
    public static final Parameter SIMILARITY_MEASURE = new Parameter(
            "SIMILARITY_MEASURE",
            new ParameterOwnerRestriction(
                    CollaborativeSimilarityMeasure.class,
                    new PearsonCorrelationCoefficient()
            )
    );
    /**
     * Parámetro para indicar la técnica de predicción que el sistema de recomendación utiliza para la predicción de las
     * valoraciones. Si no se modifica, su valor por defecto es la suma ponderada ({@link WeightedSum})
     */
    public static final Parameter PREDICTION_TECHNIQUE = new Parameter(
            "Prediction_technique",
            new ParameterOwnerRestriction(PredictionTechnique.class, new WeightedSum()));
    /**
     * Parámetro que indica si se usa o no la mejora de frecuencia inversa. Por defecto no se aplica la mejora de
     * frecuencia inversa
     */
    public static final Parameter INVERSE_FREQUENCY = new Parameter("Inverse_frequency", new BooleanParameter(false));
    /**
     * Parámetro que almacena el valor de la mejora de ampliación de casos. Si el valor de este parámetro es 1, no
     * aplica cambios, ya que esta mejora eleva la similitud calculada al valor de ampliación de casos
     */
    public static final Parameter CASE_AMPLIFICATION = new Parameter("Case_amplification", new DoubleParameter(1.0f, 10.0f, 1.0f));
    /**
     * Indica si se imputa un valor por defecto a las valoraciones no conocidas de los usuarios. Esta mejora sirve para
     * poder calcular la similitud incluso cuando las valoraciones de los usuarios no se solapan, es decir, no han
     * valorado ningún producto en común. Por defecto no se aplica esta mejora.
     */
    public static final Parameter DEFAULT_RATING = new Parameter("Default_rating", new BooleanParameter(false));
    /**
     * Indica el valor que se imputa a las valoraciones no conocidas en caso de que se haya activado la mejora de
     * valoración por defecto.
     */
    public static final Parameter DEFAULT_RATING_VALUE = new Parameter("Default_rating_value", new IntegerParameter(1, 5, 3));
    /**
     * Parámetro que indica si se utiliza la mejora de factor de relevancia. Esta mejora modifica las similitudes de
     * manera que penaliza las similitudes calculadas con un número bajo de valoraciones en común. Por defecto no se
     * utiliza esta mejora.
     */
    public static final Parameter RELEVANCE_FACTOR = new Parameter("Relevance_factor", new BooleanParameter(true));
    /**
     * Almacena el valor del factor de relevancia aplicado si el parámetro relevanceFactor
     * indica que se debe usar factor de relevancia (true). El valor por defecto del factor de relevancia es 50.
     */
    public static final Parameter RELEVANCE_FACTOR_VALUE = new Parameter("Relevance_factor_value", new IntegerParameter(1, 9999, 20));

    public final CollaborativeSimilarityMeasure getSimilarityMeasure() {
        return (CollaborativeSimilarityMeasure) getParameterValue(SIMILARITY_MEASURE);
    }

    public KnnCollaborativeRecommender setNeighborhoodSize(int neighborhoodSize) {
        setParameterValue(NEIGHBORHOOD_SIZE, neighborhoodSize);
        return this;
    }

    public KnnCollaborativeRecommender setSIMILARITY_MEASURE(CollaborativeSimilarityMeasure similarityMeasure) {
        setParameterValue(SIMILARITY_MEASURE, similarityMeasure);
        return this;
    }

    public KnnCollaborativeRecommender setPREDICTION_TECHNIQUE(PredictionTechnique predictionTechnique) {
        setParameterValue(PREDICTION_TECHNIQUE, predictionTechnique);
        return this;
    }

    public KnnCollaborativeRecommender setINVERSE_FREQUENCY(boolean inverseFrequency) {
        setParameterValue(INVERSE_FREQUENCY, inverseFrequency);
        return this;
    }

    public KnnCollaborativeRecommender setCASE_AMPLIFICATION(double caseAmplification) {
        setParameterValue(CASE_AMPLIFICATION, caseAmplification);
        return this;
    }

    public KnnCollaborativeRecommender setDEFAULT_RATING_VALUE(Double defaultRatingValue) {
        setParameterValue(DEFAULT_RATING, defaultRatingValue != null);
        if (defaultRatingValue != null) {
            setParameterValue(DEFAULT_RATING_VALUE, defaultRatingValue);
        }
        return this;
    }

    public KnnCollaborativeRecommender setRELEVANCE_FACTOR_VALUE(Integer relevanceFactorValue) {
        setParameterValue(RELEVANCE_FACTOR, relevanceFactorValue != null);
        if (relevanceFactorValue != null) {
            setParameterValue(RELEVANCE_FACTOR_VALUE, relevanceFactorValue);
        }
        return this;
    }

    public boolean isRelevanceFactorApplied() {
        return (Boolean) getParameterValue(RELEVANCE_FACTOR);
    }

    public Integer getRelevanceFactorValue() {
        return (Integer) getParameterValue(RELEVANCE_FACTOR_VALUE);
    }

    public int getNeighborhoodSize() {
        return (Integer) getParameterValue(NEIGHBORHOOD_SIZE);
    }

    public final int getNeighborhoodSizeStore() {
        return (Integer) getParameterValue(NEIGHBORHOOD_SIZE_STORE);
    }

    public void setNeighborhoodSizeStore(int neighbourhoodSizeStore) {
        setParameterValue(NEIGHBORHOOD_SIZE_STORE, neighbourhoodSizeStore);
    }
}
