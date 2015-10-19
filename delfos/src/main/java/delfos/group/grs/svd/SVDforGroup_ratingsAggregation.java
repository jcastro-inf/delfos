package delfos.group.grs.svd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import delfos.common.aggregationoperators.AggregationOperator;
import delfos.common.aggregationoperators.Mean;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.loaders.given.DatasetLoaderGivenRatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.generated.modifieddatasets.PseudoUserRatingsDataset;
import delfos.dataset.util.DatasetUtilities;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.rs.collaborativefiltering.svd.SVDFoldingIn;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVD;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVDModel;
import delfos.rs.recommendation.Recommendation;

/**
 * Sistema de recomendación a grupos con agregación de valoraciones de los
 * miembros del grupo. Utiliza el SR a inviduos basado en SVD con actualización
 * FoldIn del modelo. De esta manera se incluyen las valoraciones del
 * pseudo-usuario que representa al grupo.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 08-Julio-2013
 */
public class SVDforGroup_ratingsAggregation extends GroupRecommenderSystemAdapter<TryThisAtHomeSVDModel, GroupSVDModel> {

    private static final long serialVersionUID = 1L;
    /**
     * Especifica la técnica de agregación para agregar los ratings de los
     * usuarios y formar el perfil del grupo.
     */
    public static final Parameter AGGREGATION_OPPERATOR = new Parameter(
            "AGGREGATION_METHOD",
            new ParameterOwnerRestriction(AggregationOperator.class, new Mean()),
            "Especifica la técnica de agregación para agregar los ratings de "
            + "los usuarios y formar el perfil del grupo.");
    private final SVDFoldingIn singleUserSR = new SVDFoldingIn();

    public SVDforGroup_ratingsAggregation() {
        super();

        addParameter(AGGREGATION_OPPERATOR);
        addParameter(TryThisAtHomeSVD.LEARNING_RATE);
        addParameter(TryThisAtHomeSVD.NORMALIZE_WITH_USER_MEAN);
        addParameter(TryThisAtHomeSVD.NUM_FEATURES);
        addParameter(TryThisAtHomeSVD.NUM_ITER_PER_FEATURE);
        addParameter(TryThisAtHomeSVD.PREDICT_IN_RATING_RANGE);
        addParameter(TryThisAtHomeSVD.SMART_INITIALISATION);
        addParameter(TryThisAtHomeSVD.SEED);
        addParameter(TryThisAtHomeSVD.K);

        addParammeterListener(() -> {
            singleUserSR.setParameterValue(TryThisAtHomeSVD.LEARNING_RATE, getParameterValue(TryThisAtHomeSVD.LEARNING_RATE));
            singleUserSR.setParameterValue(TryThisAtHomeSVD.NORMALIZE_WITH_USER_MEAN, getParameterValue(TryThisAtHomeSVD.NORMALIZE_WITH_USER_MEAN));
            singleUserSR.setParameterValue(TryThisAtHomeSVD.NUM_FEATURES, getParameterValue(TryThisAtHomeSVD.NUM_FEATURES));
            singleUserSR.setParameterValue(TryThisAtHomeSVD.NUM_ITER_PER_FEATURE, getParameterValue(TryThisAtHomeSVD.NUM_ITER_PER_FEATURE));
            singleUserSR.setParameterValue(TryThisAtHomeSVD.PREDICT_IN_RATING_RANGE, getParameterValue(TryThisAtHomeSVD.PREDICT_IN_RATING_RANGE));
            singleUserSR.setParameterValue(TryThisAtHomeSVD.SMART_INITIALISATION, getParameterValue(TryThisAtHomeSVD.SMART_INITIALISATION));
            singleUserSR.setParameterValue(TryThisAtHomeSVD.SEED, getParameterValue(TryThisAtHomeSVD.SEED));
            singleUserSR.setParameterValue(TryThisAtHomeSVD.K, getParameterValue(TryThisAtHomeSVD.K));
        });

        singleUserSR.addRecommendationModelBuildingProgressListener(this::fireBuildingProgressChangedEvent);
    }

    @Override
    public boolean isRatingPredictorRS() {
        return true;
    }

    @Override
    public TryThisAtHomeSVDModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        return singleUserSR.buildRecommendationModel(datasetLoader);
    }

    @Override
    public GroupSVDModel buildGroupModel(
            DatasetLoader<? extends Rating> datasetLoader,
            TryThisAtHomeSVDModel RecommendationModel,
            GroupOfUsers groupOfUsers)
            throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {

        AggregationOperator aggregationOperator = getAggregationOperator();
        Map<Integer, Number> groupAggregatedProfile = AggregationOfIndividualRatings.getGroupProfile(datasetLoader, aggregationOperator, groupOfUsers);

        Map<Integer, Map<Integer, Number>> groupAggregatedProfile_matrix = new TreeMap<>();
        groupAggregatedProfile_matrix.put(-1, groupAggregatedProfile);

        PseudoUserRatingsDataset<Rating> rd = new PseudoUserRatingsDataset<>(datasetLoader.getRatingsDataset(), DatasetUtilities.getUserMap_Rating(-1, groupAggregatedProfile));

        final int idPseudoUser = rd.getIdPseudoUser();

        TryThisAtHomeSVDModel foldInModel = singleUserSR.incrementModelWithUserRatings(RecommendationModel, new DatasetLoaderGivenRatingsDataset(datasetLoader, rd), idPseudoUser);

        int idPseudoUserIndex = foldInModel.getUsersIndex().get(idPseudoUser);

        ArrayList<Double> groupFeatures = foldInModel.getAllUserFeatures().get(idPseudoUserIndex);

        return new GroupSVDModel(groupOfUsers, groupFeatures);
    }

    public AggregationOperator getAggregationOperator() {
        return (AggregationOperator) getParameterValue(AGGREGATION_OPPERATOR);
    }

    @Override
    public Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, TryThisAtHomeSVDModel RecommendationModel, GroupSVDModel groupModel, GroupOfUsers groupOfUsers, java.util.Set<Integer> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {

        int idUser = -1;
        if (datasetLoader.getRatingsDataset().allUsers().contains(idUser)) {
            idUser--;
        }

        TryThisAtHomeSVDModel extendedModel = TryThisAtHomeSVDModel.addUser(RecommendationModel, idUser, groupModel.getGroupFeatures());
        return singleUserSR.recommendToUser(datasetLoader, extendedModel, idUser, candidateItems);
    }
}
