package delfos.group.grouplevelcasestudy.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.MultiThreadExecutionManager;
import delfos.common.parallelwork.PartialWorkListener;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.grouplevelcasestudy.GroupLevelResults;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.groupsofusers.measuresovergroups.GroupMeasure;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupMeasureResult;
import delfos.group.experiment.validation.validationtechniques.GroupValidationTechnique;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;

/**
 * Realiza un caso de estudio a nivel de grupo, es decir, aplica las medidas de
 * evaluación sólo por cada grupo.
 *
* @author Jorge Castro Gallardo
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
            GroupValidationTechnique validationTechnique,
            GroupPredictionProtocol predictionProtocol,
            final GroupMeasure[] grouMeasures,
            final Collection<GroupEvaluationMeasure> evaluationMeasures)
            throws CannotLoadRatingsDataset, CannotLoadContentDataset, UserNotFound, ItemNotFound {

        groupFormation.setSeedValue(seed);
        Collection<GroupOfUsers> groups = groupFormation.shuffle(datasetLoader);

        final RelevanceCriteria relevanceCriteria = datasetLoader.getDefaultRelevanceCriteria();

        System.out.println("");
        System.out.println("===============================================================");
        System.out.println("===================== RESULTADOS POR GRUPO ====================");
        System.out.println("===============================================================");
        System.out.println("");

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
            System.out.println(line);
        }

        List<SingleGroupTask> tasks = new ArrayList<SingleGroupTask>();

        for (GroupOfUsers group : groups) {
            tasks.add(new SingleGroupTask(seed, validationTechnique, datasetLoader, groups, group, groupRecommenderSystems, predictionProtocol, grouMeasures, evaluationMeasures));
        }

        MultiThreadExecutionManager<SingleGroupTask> multiThreadExecutionManager = new MultiThreadExecutionManager<SingleGroupTask>(
                "Group calculation",
                tasks,
                SingleGroupTaskExecute.class);

        multiThreadExecutionManager.addPartialWorkListener(new PartialWorkListener<SingleGroupTask>() {
            private final Object exMut = 0;

            @Override
            public void finishedTask() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void finishedTask(SingleGroupTask t) {
                GroupOfUsers group = t.getGroup();

                for (GroupLevelResults groupLevelResults : t.getGroupLevelResults()) {
                    StringBuilder line = new StringBuilder();
                    line.append(group.toString());
                    synchronized (exMut) {
                        //Línea del grupo

                        for (GroupMeasure groupMeasure : grouMeasures) {
                            line.append("\t").append(groupLevelResults.getGroupMeasureValue(groupMeasure));
                        }

                        for (GroupEvaluationMeasure groupEvaluationMeasure : evaluationMeasures) {
                            for (GroupRecommenderSystem groupRecommenderSystem : groupRecommenderSystems) {
                                GroupMeasureResult groupMeasureResult = groupLevelResults.getEvaluationMeasureValue(groupRecommenderSystem, groupEvaluationMeasure);
                                line.append("\t").append(groupMeasureResult.getValue());
                            }
                        }
                    }
                    System.out.println(line);
                }
            }
        });

        multiThreadExecutionManager.run();
    }
}
