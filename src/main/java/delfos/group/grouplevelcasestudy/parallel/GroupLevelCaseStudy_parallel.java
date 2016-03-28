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

import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.MultiThreadExecutionManager;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.groupsofusers.measuresovergroups.GroupMeasure;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Realiza un caso de estudio a nivel de grupo, es decir, aplica las medidas de
 * evaluación sólo por cada grupo.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 03-Jun-2013
 */
public class GroupLevelCaseStudy_parallel {

    public GroupLevelCaseStudy_parallel() {
    }

    public void execute(
            long seed,
            DatasetLoader<? extends Rating> datasetLoader,
            GroupFormationTechnique groupFormation,
            final GroupRecommenderSystem[] groupRecommenderSystems,
            ValidationTechnique validationTechnique,
            GroupPredictionProtocol predictionProtocol,
            final GroupMeasure[] grouMeasures,
            final Collection<GroupEvaluationMeasure> evaluationMeasures)
            throws CannotLoadRatingsDataset, CannotLoadContentDataset, UserNotFound, ItemNotFound {

        groupFormation.setSeedValue(seed);
        Collection<GroupOfUsers> groups = groupFormation.generateGroups(datasetLoader);

        final RelevanceCriteria relevanceCriteria = datasetLoader.getDefaultRelevanceCriteria();

        Global.showln("");
        Global.showln("===============================================================");
        Global.showln("===================== RESULTADOS POR GRUPO ====================");
        Global.showln("===============================================================");
        Global.showln("");

        {
            //Línea de cabecera.
            StringBuilder line = new StringBuilder();
            line.append("group\tsplit");
            for (GroupMeasure groupMeasure : grouMeasures) {
                line.append("\t").append(groupMeasure.getAlias());
            }

            for (GroupEvaluationMeasure groupEvaluationMeasure : evaluationMeasures) {
                for (GroupRecommenderSystem groupRecommenderSystem : groupRecommenderSystems) {
                    line.append("\t").append(groupRecommenderSystem.getAlias()).append("-->").append(groupEvaluationMeasure.getAlias());
                }
            }
            Global.showln(line.toString());
        }

        List<SingleGroupTask> tasks = new ArrayList<>();

        for (GroupOfUsers group : groups) {
            tasks.add(new SingleGroupTask(seed, validationTechnique, datasetLoader, groups, group, groupRecommenderSystems, predictionProtocol, grouMeasures, evaluationMeasures));
        }

        MultiThreadExecutionManager<SingleGroupTask> multiThreadExecutionManager = new MultiThreadExecutionManager<>(
                "Group calculation",
                tasks,
                SingleGroupTaskExecute.class);

        multiThreadExecutionManager.run();
    }
}
