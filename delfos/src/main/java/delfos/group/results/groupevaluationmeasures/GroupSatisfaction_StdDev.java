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

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//TODO: revisar si las características e interpretación de la medida son correctas
/**
 * Esta medida de evaluación calcula la diferencia entre la predicción realizada
 * por el sistema de recomendación a grupos y la predicción realizada para cada
 * miembro por un sistema de recomendación a individuos. El sistema de
 * recomendación de base que se utiliza debe ser del mismo tipo que el sistema
 * de recomendacion que se usa.
 *
 * Características que destaca: satisfacción de cada miembro respecto de la
 * recomendación al grupo Interpretación: Si es menor, los individuos están más
 * satisfechos con la recomendación.
 *
 * Esta medida de evaluación se utiliza en el siguiente paper: - García et al. /
 * Information Sciences 189 (2012) p155-175 [Citation: Inma Garcia , Sergio
 * Pajares, Laura Sebastia, Eva Onaindia: Preference elicitation techniques for
 * group recommender systems]
 *
 * @author Jorge Castro Gallardo
 *
 * @version Unknown Date
 * @version 20-Noviembre-2013
 */
public class GroupSatisfaction_StdDev extends GroupEvaluationMeasure {

    public GroupSatisfaction_StdDev() {
        super();
    }

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            RatingsDataset<? extends Rating> testDataset,
            RelevanceCriteria relevanceCriteria,
            DatasetLoader<? extends Rating> trainingDatasetLoader,
            DatasetLoader<? extends Rating> testDatasetLoader) {

        MeanIterative mediaTodasDesviacionesTipicas = new MeanIterative();

        for (GroupOfUsers groupOfUsers : groupRecommenderSystemResult.getGroupsOfUsers()) {
            Collection<Recommendation> groupRecommendations = groupRecommenderSystemResult.getGroupOutput(groupOfUsers).getRecommendations();

            //Recorro todos los grupos
            MeanIterative maeGrupo = new MeanIterative();

            /* Hago esta reordenación de los resultados para ganar eficiencia */
            Map<Integer, Recommendation> recomendacionesAlGrupoReordenadas = new HashMap<>();
            for (Recommendation recommendation : groupRecommendations) {
                recomendacionesAlGrupoReordenadas.put(recommendation.getIdItem(), recommendation);
            }

            //Calculo las recomendaciones individuales de cada miembro del grupo
            List<Double> listaMaes = new LinkedList<>();

            for (int idUser : groupOfUsers.getIdMembers()) {
                try {
                    MeanIterative maeActual = new MeanIterative();
                    Map<Integer, ? extends Rating> userRated = testDataset.getUserRatingsRated(idUser);
                    for (int idItem : userRated.keySet()) {
                        if (recomendacionesAlGrupoReordenadas.containsKey(idItem)) {
                            double prediccionGrupo = recomendacionesAlGrupoReordenadas.get(idItem).getPreference().doubleValue();
                            double prediccionIndividuo = userRated.get(idItem).getRatingValue().doubleValue();
                            maeActual.addValue(Math.abs(prediccionGrupo - prediccionIndividuo));
                        }
                    }

                    listaMaes.add(maeActual.getMean());
                    maeGrupo.addValue(maeActual.getMean());
                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                }

            }

            //Desviación típica del mae de los miembros del grupo:
            MeanIterative mediaDesviacionGrupoActual = new MeanIterative();
            for (double value : listaMaes) {
                mediaDesviacionGrupoActual.addValue(Math.pow(value - maeGrupo.getMean(), 2));
            }
            mediaTodasDesviacionesTipicas.addValue(mediaDesviacionGrupoActual.getMean());
        }
        return new GroupEvaluationMeasureResult(this, (float) mediaTodasDesviacionesTipicas.getMean());
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
