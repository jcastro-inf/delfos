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
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
public class GroupSatisfaction_Average extends GroupEvaluationMeasure {

    public GroupSatisfaction_Average() {
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

        MeanIterative maeTotal = new MeanIterative();

        for (GroupOfUsers groupOfUsers : groupRecommenderSystemResult.getGroupsOfUsers()) {
            Collection<Recommendation> groupRecommendations = groupRecommenderSystemResult.getGroupOutput(groupOfUsers).getRecommendations();

            //Recorro todos los grupos
            MeanIterative maeGupos = new MeanIterative();

            /* Hago esta reordenación de los resultados para ganar eficiencia */
            Map<Integer, Recommendation> recomendacionesAlGrupoReordenadas = new HashMap<>();
            for (Recommendation r : groupRecommendations) {
                recomendacionesAlGrupoReordenadas.put(r.getIdItem(), r);
            }

            //Calculo las recomendaciones individuales de cada miembro del grupo
            for (int idUser : groupOfUsers.getIdMembers()) {
                try {
                    MeanIterative maeActual = new MeanIterative();

                    Map<Integer, ? extends Rating> userRated = testDataset.getUserRatingsRated(idUser);

                    Set<Integer> commonItems = new TreeSet<>(recomendacionesAlGrupoReordenadas.keySet());
                    commonItems.retainAll(userRated.keySet());

                    for (int idItem : commonItems) {
                        double prediccionGrupo = recomendacionesAlGrupoReordenadas.get(idItem).getPreference().doubleValue();
                        double prediccionIndividuo = userRated.get(idItem).getRatingValue().doubleValue();
                        maeActual.addValue(Math.abs(prediccionGrupo - prediccionIndividuo));
                    }
                    maeGupos.addValue(maeActual.getMean());
                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                }
            }
            maeTotal.addValue(maeGupos.getMean());
        }
        return new GroupEvaluationMeasureResult(this, (float) maeTotal.getMean());
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
