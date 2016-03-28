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
package delfos.group.grouplevelcasestudy.parallel;

import delfos.common.parallelwork.Task;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.grouplevelcasestudy.GroupLevelResults;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.groupsofusers.measuresovergroups.GroupMeasure;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import java.util.Collection;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 04-Jun-2013
 * @version 1.1 26-Noviembre-2013
 */
public class SingleGroupTask extends Task {

    private final long seed;
    private final ValidationTechnique validationTechnique;
    private final DatasetLoader<? extends Rating> datasetLoader;
    private final Collection<GroupOfUsers> groups;
    private final GroupOfUsers group;
    private final GroupRecommenderSystem[] groupRecommenderSystems;
    private final GroupPredictionProtocol predictionProtocol;
    private final GroupMeasure[] grouMeasures;
    private final Collection<GroupEvaluationMeasure> evaluationMeasures;
    private GroupLevelResults[] groupLevelResults = null;

    public SingleGroupTask(
            long seed,
            ValidationTechnique validationTechnique,
            DatasetLoader<? extends Rating> datasetLoader,
            Collection<GroupOfUsers> groups,
            GroupOfUsers group,
            GroupRecommenderSystem[] groupRecommenderSystems,
            GroupPredictionProtocol predictionProtocol,
            GroupMeasure[] groupMeasures,
            Collection<GroupEvaluationMeasure> evaluationMeasures) {

        this.seed = seed;
        this.validationTechnique = validationTechnique;
        this.datasetLoader = datasetLoader;
        this.groups = groups;
        this.group = group;
        this.groupRecommenderSystems = groupRecommenderSystems;
        this.predictionProtocol = predictionProtocol;
        this.grouMeasures = groupMeasures;
        this.evaluationMeasures = evaluationMeasures;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + this.hashCode();
    }

    public DatasetLoader<? extends Rating> getDatasetLoader() {
        return datasetLoader;
    }

    public Collection<GroupEvaluationMeasure> getEvaluationMeasures() {
        return evaluationMeasures;
    }

    public GroupMeasure[] getGrouMeasures() {
        return grouMeasures;
    }

    public GroupOfUsers getGroup() {
        return group;
    }

    public GroupRecommenderSystem[] getGroupRecommenderSystems() {
        return groupRecommenderSystems;
    }

    public Collection<GroupOfUsers> getGroups() {
        return groups;
    }

    public GroupPredictionProtocol getPredictionProtocol() {
        return predictionProtocol;
    }

    public ValidationTechnique getValidationTechnique() {
        return validationTechnique;
    }

    public void setGroupLevelResults(GroupLevelResults[] groupLevelResults) {
        this.groupLevelResults = groupLevelResults;
    }

    public GroupLevelResults[] getGroupLevelResults() {
        return groupLevelResults;
    }

    public long getSeed() {
        return seed;
    }
}
