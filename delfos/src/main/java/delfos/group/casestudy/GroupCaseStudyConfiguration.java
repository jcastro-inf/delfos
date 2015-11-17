package delfos.group.casestudy;

import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.experiment.validation.validationtechniques.GroupValidationTechnique;
import delfos.group.grs.GroupRecommenderSystem;

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
    private final String caseStudyAlias;

    public GroupCaseStudyConfiguration(
            GroupRecommenderSystem<Object, Object> groupRecommenderSystem,
            DatasetLoader<? extends Rating> datasetLoader,
            GroupFormationTechnique groupFormationTechnique,
            GroupValidationTechnique groupValidationTechnique,
            GroupPredictionProtocol groupPredictionProtocol,
            RelevanceCriteria relevanceCriteria,
            String caseStudyAlias) {

        this.groupRecommenderSystem = groupRecommenderSystem;
        this.groupFormationTechnique = groupFormationTechnique;
        this.groupValidationTechnique = groupValidationTechnique;
        this.groupPredictionProtocol = groupPredictionProtocol;
        this.datasetLoader = datasetLoader;
        this.relevanceCriteria = relevanceCriteria;

        this.caseStudyAlias = caseStudyAlias;
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

}
