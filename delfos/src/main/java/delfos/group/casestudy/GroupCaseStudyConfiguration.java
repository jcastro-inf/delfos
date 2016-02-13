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
package delfos.group.casestudy;

import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.experiment.validation.validationtechniques.GroupValidationTechnique;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import java.util.Map;

/**
 * Almacena los valores de un caso de estudio de sistemas de recomendación para
 * grupos de usuarios.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 9-Enero-2014
 */
public class GroupCaseStudyConfiguration {

    private final GroupRecommenderSystem<? extends Object, ? extends Object> groupRecommenderSystem;
    private final DatasetLoader<? extends Rating> datasetLoader;
    private final GroupFormationTechnique groupFormationTechnique;
    private final GroupValidationTechnique groupValidationTechnique;
    private final GroupPredictionProtocol groupPredictionProtocol;
    private final RelevanceCriteria relevanceCriteria;
    private final Map<GroupEvaluationMeasure, GroupEvaluationMeasureResult> groupEvaluationMeasuresResults;

    private final String caseStudyAlias;
    private final int numExecutions;
    private final long seed;

    public GroupCaseStudyConfiguration(
            GroupRecommenderSystem<Object, Object> groupRecommenderSystem,
            DatasetLoader<? extends Rating> datasetLoader,
            GroupFormationTechnique groupFormationTechnique,
            GroupValidationTechnique groupValidationTechnique,
            GroupPredictionProtocol groupPredictionProtocol,
            RelevanceCriteria relevanceCriteria,
            String caseStudyAlias,
            int numExecutions,
            long seed,
            Map<GroupEvaluationMeasure, GroupEvaluationMeasureResult> groupEvaluationMeasuresResults) {

        this.groupRecommenderSystem = groupRecommenderSystem;
        this.groupFormationTechnique = groupFormationTechnique;
        this.groupValidationTechnique = groupValidationTechnique;
        this.groupPredictionProtocol = groupPredictionProtocol;
        this.datasetLoader = datasetLoader;
        this.relevanceCriteria = relevanceCriteria;

        this.caseStudyAlias = caseStudyAlias;
        this.numExecutions = numExecutions;
        this.seed = seed;
        this.groupEvaluationMeasuresResults = groupEvaluationMeasuresResults;
    }

    public GroupRecommenderSystem<? extends Object, ? extends Object> getGroupRecommenderSystem() {
        return groupRecommenderSystem;
    }

    public DatasetLoader<? extends Rating> getDatasetLoader() {
        return datasetLoader;
    }

    public GroupValidationTechnique getGroupValidationTechnique() {
        return groupValidationTechnique;
    }

    public GroupFormationTechnique getGroupFormationTechnique() {
        return groupFormationTechnique;
    }

    public GroupPredictionProtocol getGroupPredictionProtocol() {
        return groupPredictionProtocol;
    }

    public RelevanceCriteria getRelevanceCriteria() {
        return relevanceCriteria;
    }

    public String getCaseStudyAlias() {
        return caseStudyAlias;
    }

    public GroupCaseStudy createGroupCaseStudy() {

        GroupCaseStudy caseStudyGroupRecommendation = new GroupCaseStudy(
                datasetLoader,
                groupRecommenderSystem,
                groupFormationTechnique,
                groupValidationTechnique,
                groupPredictionProtocol,
                groupEvaluationMeasuresResults.keySet(),
                relevanceCriteria,
                numExecutions,
                seed);

        caseStudyGroupRecommendation.setAggregateResults(groupEvaluationMeasuresResults);

        caseStudyGroupRecommendation.setAlias(caseStudyAlias);

        return caseStudyGroupRecommendation;
    }
}
