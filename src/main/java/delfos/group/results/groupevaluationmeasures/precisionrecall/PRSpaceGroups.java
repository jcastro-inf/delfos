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
package delfos.group.results.groupevaluationmeasures.precisionrecall;

import delfos.common.Global;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.util.DatasetUtilities;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatricesCurve;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Medida de evaluación para sistemas de recomendación a grupos que calcula la precisión y recall a lo largo de todos
 * los tamaños de recomendación al grupo. Usa como test la media de valoraciones de test de los usuarios sobre el
 * producto que se predice.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 15-01-2013
 */
public class PRSpaceGroups extends GroupEvaluationMeasure {

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult, DatasetLoader<? extends Rating> originalDatasetLoader, RelevanceCriteria relevanceCriteria, DatasetLoader<? extends Rating> trainingDatasetLoader, DatasetLoader<? extends Rating> testDatasetLoader) {

        ConfusionMatricesCurve agregada = getDetailedResult(
                groupRecommenderSystemResult,
                originalDatasetLoader,
                relevanceCriteria,
                trainingDatasetLoader,
                testDatasetLoader);

        double value;
        if (agregada.size() >= 2) {
            value = agregada.getPrecisionAt(1);
        } else {
            value = Double.NaN;
        }

        Map<String, Double> detailedResult = new TreeMap<>();
        for (int i = 0; i < agregada.size(); i++) {
            double precisionAt = agregada.getPrecisionAt(i);
            detailedResult.put("Precision@" + i, precisionAt);
        }

        return new GroupEvaluationMeasureResult(this, value);
    }

    public ConfusionMatricesCurve getDetailedResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            RelevanceCriteria relevanceCriteria,
            DatasetLoader<? extends Rating> trainingDatasetLoader,
            DatasetLoader<? extends Rating> testDatasetLoader) {

        Map<GroupOfUsers, ConfusionMatricesCurve> prCurves = Collections.synchronizedMap(new TreeMap<>());

        AtomicInteger gruposSinMatriz = new AtomicInteger(0);
        groupRecommenderSystemResult.getGroupsOfUsers().parallelStream().forEach((group) -> {
            Collection<Recommendation> groupRecommendations = groupRecommenderSystemResult
                    .getGroupOutput(group)
                    .getRecommendations()
                    .getRecommendations();

            Map<Integer, Map<Integer, Number>> membersRatings_byUser = DatasetUtilities.getMembersRatings_byUser(group, testDatasetLoader);

            List<Boolean> recommendacionesGrupo = new ArrayList<>(groupRecommendations.size());
            for (Recommendation r : groupRecommendations) {
                int idItem = r.getItem().getId();

                MeanIterative mean = new MeanIterative();
                for (int idUser : group.getIdMembers()) {

                    if (!membersRatings_byUser.containsKey(idUser)) {
                        Global.showError(new IllegalStateException("User '" + idUser + "' has no ratings"));

                    } else if (membersRatings_byUser.get(idUser).containsKey(idItem)) {

                        Number rating = membersRatings_byUser.get(idUser).get(idItem);
                        mean.addValue(rating.doubleValue());
                    }
                }
                recommendacionesGrupo.add(relevanceCriteria.isRelevant(mean.getMean()));
            }

            try {
                prCurves.put(group, new ConfusionMatricesCurve(recommendacionesGrupo));
            } catch (IllegalArgumentException iae) {
                gruposSinMatriz.incrementAndGet();
            }
        });

        ConfusionMatricesCurve agregada = ConfusionMatricesCurve.mergeCurves(prCurves.values());

        return agregada;
    }
}
