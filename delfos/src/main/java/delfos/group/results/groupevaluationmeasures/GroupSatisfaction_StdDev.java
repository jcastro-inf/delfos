package delfos.group.results.groupevaluationmeasures;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommendationResult;
import delfos.rs.recommendation.Recommendation;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
    public GroupMeasureResult getMeasureResult(GroupRecommendationResult recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {
        MeanIterative mediaTodasDesviacionesTipicas = new MeanIterative();
        for (Entry<GroupOfUsers, List<Recommendation>> next : recommendationResults) {
            //Recorro todos los grupos
            MeanIterative maeGrupo = new MeanIterative();

            /* Hago esta reordenación de los resultados para ganar eficiencia */
            Map<Integer, Recommendation> recomendacionesAlGrupoReordenadas = new HashMap<Integer, Recommendation>();
            for (Recommendation r : next.getValue()) {
                recomendacionesAlGrupoReordenadas.put(r.getIdItem(), r);
            }

            //Calculo las recomendaciones individuales de cada miembro del grupo
            List<Double> listaMaes = new LinkedList<Double>();

            for (int idUser : next.getKey().getIdMembers()) {
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
        return new GroupMeasureResult(this, (float) mediaTodasDesviacionesTipicas.getMean());
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
