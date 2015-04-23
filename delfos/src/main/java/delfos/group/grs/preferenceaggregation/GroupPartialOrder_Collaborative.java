package delfos.group.grs.preferenceaggregation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.generated.modifieddatasets.PseudoUserRatingsDataset;
import delfos.dataset.util.DatasetUtilities;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.preferenceaggregation.order.LearningToOrderThings;
import delfos.group.grs.preferenceaggregation.order.Preff;
import delfos.group.grs.preferenceaggregation.order.RatingBasedPref;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.collaborativefiltering.predictiontechniques.WeightedSum;
import delfos.rs.recommendation.Recommendation;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;

/**
 * Implementa el sistema de recomendación a grupos basado en la medida propuesta
 * por Hui Wang para calcular similitudes entre ordenes de preferencia parciales
 *
 * @author Jorge Castro Gallardo
 *
 * @deprecated Se ha modificado la estructura de los sistemas de recomendación y
 * no se ha actualizado este sistema.
 *
 * @version 1.0 Unknown date
 * @version 1.1 9-Mayo-2013
 */
public class GroupPartialOrder_Collaborative extends GroupRecommenderSystemAdapter<GroupPartialOrderModel, GroupPartialOrderGroupModel> {

    private static final long serialVersionUID = 1L;

    public GroupPartialOrder_Collaborative() {
        super();
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public GroupPartialOrderModel build(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public GroupPartialOrderGroupModel buildGroupModel(DatasetLoader<? extends Rating> datasetLoader, GroupPartialOrderModel RecommendationModel, GroupOfUsers groupOfUsers) throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, GroupPartialOrderModel RecommendationModel, GroupPartialOrderGroupModel groupModel, GroupOfUsers groupOfUsers, java.util.Set<Integer> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {

        if (1 == 1) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        final PreferenceOrder<Integer>[] orderMembers = new PreferenceOrder[groupOfUsers.size()];

        Set<Integer> idItemsRated = new TreeSet<>();

        //Calculo el orden de preferencia de cada miembro
        int i = 0;
        for (int idUser : groupOfUsers.getGroupMembers()) {
            //Pruebo inicialmente como no bipolar, por eficiencia de los métodos
            Map<Integer, ? extends Rating> userRatingsRated = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser);
            Preff<Integer> preff = new RatingBasedPref(RecommendationEntity.USER, userRatingsRated.values());

            idItemsRated.addAll(userRatingsRated.keySet());
            List totalOrder = LearningToOrderThings.greedyOrder(userRatingsRated.keySet(), preff);
            orderMembers[i] = new PreferenceOrder<>(totalOrder);

            i++;
        }
        //Agrego los ordenes de preferencia en uno solo
        List greedyOrder = LearningToOrderThings.greedyOrderSimplificado(idItemsRated, new Preff() {
            final PreferenceOrder<Integer>[] _orderMembers = orderMembers;

            @Override
            public float preff(Object e1, Object e2) {

                if (e1 == e2) {
                    //Global.showWarning("Es un warning?");
                    return 0;
                }

                //Secuencia que compruebo en cada uno de los miembros del grupo
                PreferenceOrder alpha = new PreferenceOrder(e1, e2);
                int gAB = 0;
                for (PreferenceOrder x : _orderMembers) {

                    //Esta sentencia suma el valor de ncm(alpha,x), es decir, el número de vecinos en común
                    gAB += CommonSequences.getCommonSequencesEfficient(alpha, x).size();
                }

                int gB = 0;
                alpha = new PreferenceOrder(e2);
                for (PreferenceOrder x : _orderMembers) {

                    //Esta sentencia suma el valor de ncm(alpha,x), es decir, el número de vecinos en común
                    gB += CommonSequences.getCommonSequencesEfficient(alpha, x).size();
                }

                float ret = ((float) gAB) / gB;

                if (ret <= 1 && ret >= 0) {
                    throw new IllegalArgumentException("The value is not between 0 and 1: " + ret);
                }
                if (ret < 0) {
                    throw new IllegalArgumentException("The value is negative: " + ret);
                }

                return ret;
            }
        });

        PreferenceOrder<Integer> groupPreferences = new PreferenceOrder<>(greedyOrder);

        //Aproximo los items a preferencias
        double x1 = groupPreferences.size() - 1;
        double y1 = datasetLoader.getRatingsDataset().getRatingsDomain().max().doubleValue();
        double x2 = 0;
        double y2 = datasetLoader.getRatingsDataset().getRatingsDomain().min().doubleValue();

        double m = (y2 - y1) / (x2 - x1);

        Map<Integer, Number> groupRatings = new TreeMap<>();
        i = 0;
        for (int idItem : groupPreferences.getList()) {
            double rating = m * i + y1;
            groupRatings.put(idItem, rating);
            i++;
        }

        //Compongo el sistema de recomendación colaborativo en que se soporta este sistema
        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        PseudoUserRatingsDataset pseudoUserRatingsDataset = new PseudoUserRatingsDataset<>(
                ratingsDataset,
                DatasetUtilities.getUserMap_Rating(-1, groupRatings),
                groupOfUsers.getGroupMembers());

        int idPseudoUser = pseudoUserRatingsDataset.getIdPseudoUser();

        KnnMemoryBasedCFRS memoryBased = new KnnMemoryBasedCFRS();
        memoryBased.setParameterValue(KnnMemoryBasedCFRS.SIMILARITY_MEASURE, new PearsonCorrelationCoefficient());
        memoryBased.setParameterValue(KnnMemoryBasedCFRS.PREDICTION_TECHNIQUE, new WeightedSum());

        Collection<Recommendation> recommendOnly = memoryBased.recommendOnly(datasetLoader, memoryBased.build(datasetLoader), idPseudoUser, candidateItems);
        return recommendOnly;

    }

    @Override
    public boolean isRatingPredictorRS() {
        return true;
    }
}
