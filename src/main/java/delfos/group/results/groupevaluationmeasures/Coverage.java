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
package delfos.group.results.groupevaluationmeasures;

import delfos.common.Global;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * Medida de evaluación para calcular la cobertura del sistema de recomendación
 * evaluado.
 *
 * <p>
 * Es una extensión de la medida de evaluación
 * {@link delfos.Results.EvaluationMeasures.Coverage} para recomendaciones
 * individuales.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 (26-01-2013)
 * @see delfos.Results.EvaluationMeasures.RatingPrediction.MAE_ForGroups
 */
public class Coverage extends GroupEvaluationMeasure {

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            RatingsDataset<? extends Rating> testDataset,
            RelevanceCriteria relevanceCriteria,
            DatasetLoader<? extends Rating> trainingDatasetLoader,
            DatasetLoader<? extends Rating> testDatasetLoader) {

        int predichas = 0;
        int solicitudes = 0;
        for (GroupOfUsers group : groupRecommenderSystemResult.getGroupsOfUsers()) {
            Collection<Recommendation> groupRecommendations = groupRecommenderSystemResult.getGroupOutput(group).getRecommendations();

            {
                //Compruebo que no hay recomendaciones repetidas.
                Set<Integer> itemsRecomendados = new TreeSet<>();
                for (Recommendation recommendation : groupRecommendations) {
                    if (itemsRecomendados.contains(recommendation.getIdItem())) {
                        Global.showWarning("The group " + group + " has received item " + recommendation.getIdItem() + " as recommendation multiple times.");
                    } else {
                        itemsRecomendados.add(recommendation.getIdItem());
                    }
                }
            }

            Collection<Integer> solicitadas = groupRecommenderSystemResult.getGroupInput(group).getItemsRequested();
            if (solicitadas == null) {
                Global.showWarning("the group " + group + " has no requests (null)");
            } else {
                solicitudes += solicitadas.size();
                predichas += groupRecommendations.size();
            }
        }
        double ret = predichas / ((double) solicitudes);
        return new GroupEvaluationMeasureResult(this, ret);
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
