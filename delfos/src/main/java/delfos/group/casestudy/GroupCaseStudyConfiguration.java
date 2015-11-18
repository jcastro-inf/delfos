package delfos.group.casestudy;

import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.casestudy.defaultcase.DefaultGroupCaseStudy;
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
 * @author Jorge Castro Gallardo
 *
 * @version 9-Enero-2014
 */
public class GroupCaseStudyConfiguration {

    private final GroupRecommenderSystem<Object, Object> groupRecommenderSystem;
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

    public GroupRecommenderSystem<Object, Object> getGroupRecommenderSystem() {
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

        GroupCaseStudy caseStudyGroupRecommendation = new DefaultGroupCaseStudy(
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
