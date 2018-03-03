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
package delfos.common.parameters;

import delfos.factories.*;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.factories.GroupFormationTechniquesFactory;
import delfos.group.factories.GroupPredictionProtocolsFactory;
import delfos.group.factories.GroupRatingsFilterFactory;
import delfos.group.factories.GroupRecommendationsSelectorFactory;
import delfos.group.factories.GroupRecommenderSystemsFactory;
import delfos.group.grs.cww.centrality.CentralityConceptDefinitionFactory;
import delfos.rs.trustbased.belieffunctions.BeliefFunctionsFactory;

/**
 * Tipos de parameter owner que existen.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 15-Noviembre-2013
 */
public enum ParameterOwnerType {

    RECOMMENDER_SYSTEM,
    DATASET_LOADER,
    VALIDATION_TECHNIQUE,
    PREDICTION_PROTOCOL_TECHNIQUE,
    EVALUATION_MESAURE,
    AGGREGATION_OPERATOR,
    DISTANCE_OF_USER_RATINGS,
    DISTANCE, MULTIDISTANCE,
    RECOMMENDATIONS_OUTPUT_METHOD,
    PERSISTENCE_METHOD,
    CONTENT_DATASET,
    RATINGS_DATASET,
    DATASET_SAVER,
    WEIGHTED_GRAPH_CALCULATION,
    GROUP_CASE_STUDY,
    GROUP_RATINGS_FILTER,
    GROUP_RECOMMENDER_SYSTEM,
    GROUP_MEASURE,
    PATH,
    GROUP_ITEM_WEIGHT,
    GROUP_PREDICTION_PROTOCOL,
    GROUP_FORMATION_TECHNIQUE,
    GROUP_EVALUATION_MEASURE,
    PREDICTION_TECHNIQUE,
    SIMILARITY_MEASURE,
    CENTRALITY_CONCEPT_DEFINITION,
    BELIEF_FUNCTION,
    RECOMMENDATION_CANDIDATES_SELECTOR,
    CASE_STUDY,
    GROUPER,
    GROUP_RECOMMENDATION_SELECTION_MODE, NON_PERSONALISED_RECOMMENDER_SYSTEM;

    private ParameterOwnerType() {
    }

    public ParameterOwner createObjectFromClassName(String className) {
        switch (this) {
            case AGGREGATION_OPERATOR:
                return AggregationOperatorFactory.getInstance().getClassByName(className);
            case BELIEF_FUNCTION:
                return BeliefFunctionsFactory.getInstance().getClassByName(className);
            case CENTRALITY_CONCEPT_DEFINITION:
                return CentralityConceptDefinitionFactory.getInstance().getClassByName(className);
            case DATASET_LOADER:
                return DatasetLoadersFactory.getInstance().getClassByName(className);
            case EVALUATION_MESAURE:
                return EvaluationMeasuresFactory.getInstance().getClassByName(className);
            case GROUP_EVALUATION_MEASURE:
                return GroupEvaluationMeasuresFactory.getInstance().getClassByName(className);
            case GROUP_FORMATION_TECHNIQUE:
                return GroupFormationTechniquesFactory.getInstance().getClassByName(className);
            case GROUP_PREDICTION_PROTOCOL:
                return GroupPredictionProtocolsFactory.getInstance().getClassByName(className);
            case GROUP_RATINGS_FILTER:
                return GroupRatingsFilterFactory.getInstance().getClassByName(className);
            case GROUP_RECOMMENDER_SYSTEM:
                return GroupRecommenderSystemsFactory.getInstance().getClassByName(className);
            case GROUP_RECOMMENDATION_SELECTION_MODE:
                return GroupRecommendationsSelectorFactory.getInstance().getClassByName(className);
            case NON_PERSONALISED_RECOMMENDER_SYSTEM:
                return RecommenderSystemsFactory.getInstance().getClassByName(className);
            case PREDICTION_PROTOCOL_TECHNIQUE:
                return PredictionProtocolFactory.getInstance().getClassByName(className);
            case PREDICTION_TECHNIQUE:
                return PredictionTechniquesFactory.getInstance().getClassByName(className);
            case RECOMMENDATION_CANDIDATES_SELECTOR:
                return RecommendationCandidatesSelectorFactory.getInstance().getClassByName(className);
            case RECOMMENDATIONS_OUTPUT_METHOD:
                return RecommendationsOutputMethodFactory.getInstance().getClassByName(className);
            case RECOMMENDER_SYSTEM:
                return RecommenderSystemsFactory.getInstance().getClassByName(className);
            case SIMILARITY_MEASURE:
                return SimilarityMeasuresFactory.getInstance().getClassByName(className);
            case VALIDATION_TECHNIQUE:
                return ValidationTechniquesFactory.getInstance().getClassByName(className);
            case WEIGHTED_GRAPH_CALCULATION:
                return WeightedGraphCalculatorFactory.getInstance().getClassByName(className);
            case CASE_STUDY:
                return ExperimentFactory.getInstance().getClassByName(className);
            default:
                throw new IllegalArgumentException("This parameter owner type '" + this + "' does not have an associated factory.");
        }
    }

    public void loadFactory() {
        //Parche para que se llame siempre a la factoría de los parámetros que se usan.
        try {
            createObjectFromClassName("");
        } catch (RuntimeException ex) {
            if (ex instanceof IllegalArgumentException) {
            } else {
                throw ex;
            }
        }
    }
}
