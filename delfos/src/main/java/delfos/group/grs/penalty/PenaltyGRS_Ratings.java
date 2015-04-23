package delfos.group.grs.penalty;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import delfos.common.Global;
import delfos.common.aggregationoperators.penalty.functions.PenaltyFunction;
import delfos.common.aggregationoperators.penalty.functions.PenaltyWholeMatrix;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.BooleanParameter;
import delfos.common.parameters.restriction.FloatParameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.common.parameters.restriction.RecommenderSystemParameterRestriction;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.loaders.given.DatasetLoaderGiven;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.generated.modifieddatasets.PseudoUserRatingsDataset;
import delfos.dataset.util.DatasetPrinterDeprecated;
import delfos.dataset.util.DatasetUtilities;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.SingleRecommendationModel;
import delfos.group.grs.aggregation.GroupModelPseudoUser;
import delfos.group.grs.itemweighted.AggregationOfIndividualRatings_itemWeighted;
import delfos.group.grs.penalty.grouper.Grouper;
import delfos.group.grs.penalty.grouper.GrouperByIdItem;
import delfos.rs.RecommenderSystem;
import delfos.rs.RecommenderSystemBuildingProgressListener;
import delfos.rs.bufferedrecommenders.RecommenderSystem_fixedFilePersistence;
import delfos.rs.collaborativefiltering.svd.SVDFoldingIn;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVD;
import delfos.rs.recommendation.Recommendation;

/**
 * Agregación de valoraciones de los usuarios usando múltiples agregaciones y
 * una función penalty para elegir la agregación que se aplica para cada item.
 *
* @author Jorge Castro Gallardo
 *
 * @version 2-julio-2014
 */
public class PenaltyGRS_Ratings extends GroupRecommenderSystemAdapter<SingleRecommendationModel, GroupModelPseudoUser> {

    private static final long serialVersionUID = 1L;
    /**
     * "Especifica el sistema de recomendación single user que se extiende para
     * ser usado en recomendación a grupos.
     */
    public static final Parameter SINGLE_USER_RECOMMENDER;
    public static final Parameter PENALTY = new Parameter("PENALTY", new ParameterOwnerRestriction(PenaltyFunction.class, new PenaltyWholeMatrix()));
    public static final Parameter ITEM_GROUPER = new Parameter("ITEM_GROUPER", new ParameterOwnerRestriction(Grouper.class, new GrouperByIdItem(3)));
    public static final Parameter COMPLETE_PREFERENCES = new Parameter("COMPLETE_PREFERENCES", new BooleanParameter(Boolean.FALSE));
    public static final Parameter COMPLETE_PREFERENCES_THRESHOLD = new Parameter("Default_rating_value", new FloatParameter(-1000, 1000, 4));

    private final RecommenderSystem_fixedFilePersistence knnUser = new RecommenderSystem_fixedFilePersistence(new TryThisAtHomeSVD(10, 10));

    static {
        SVDFoldingIn svdFoldingIn = new SVDFoldingIn();
        svdFoldingIn.setSeedValue(987654321);

        SINGLE_USER_RECOMMENDER = new Parameter(
                "SINGLE_USER_RECOMMENDER",
                new RecommenderSystemParameterRestriction(svdFoldingIn, RecommenderSystem.class),
                "Especifica el sistema de recomendación single user que se extiende "
                + "para ser usado en recomendación a grupos.");
    }

    private PenaltyFunction oldPenalty = new PenaltyWholeMatrix();
    private Grouper oldGrouper = new GrouperByIdItem(3);
    private boolean oldCompletePreferences = false;
    private RelevanceCriteria oldCompletePreferencesThreshold = new RelevanceCriteria(4);

    public PenaltyGRS_Ratings() {
        super();
        addParameter(SINGLE_USER_RECOMMENDER);
        addParameter(PENALTY);
        addParameter(ITEM_GROUPER);
        addParameter(COMPLETE_PREFERENCES);
        addParameter(COMPLETE_PREFERENCES_THRESHOLD);

        addParammeterListener(() -> {
            PenaltyFunction newPenalty = (PenaltyFunction) getParameterValue(PENALTY);
            Grouper newGrouper = (Grouper) getParameterValue(ITEM_GROUPER);
            boolean newCompletePreferences = (Boolean) getParameterValue(COMPLETE_PREFERENCES);
            RelevanceCriteria newCompletePreferencesThreshold = getCompletePreferencesThreshold();

            String newAlias = getAlias();

            String oldAliasOldParameters
                    = this.getClass().getSimpleName()
                    + "_" + oldPenalty.getAlias()
                    + "_" + oldGrouper.getAlias();

            if (oldCompletePreferences) {
                oldAliasOldParameters += "_complete";
                oldAliasOldParameters += "_u=" + oldCompletePreferencesThreshold.getThreshold().doubleValue();
            }

            String newAliasNewParameters
                    = this.getClass().getSimpleName()
                    + "_" + newPenalty.getAlias()
                    + "_" + newGrouper.getAlias();

            if (newCompletePreferences) {
                newAliasNewParameters += "_complete";
                newAliasNewParameters += "_u=" + newCompletePreferencesThreshold.getThreshold().doubleValue();
            }

            if (!oldAliasOldParameters.equals(newAliasNewParameters)) {
                oldPenalty = newPenalty;
                oldGrouper = newGrouper;
                oldCompletePreferences = newCompletePreferences;
                oldCompletePreferencesThreshold = newCompletePreferencesThreshold;
                setAlias(newAliasNewParameters);
            }
        });
    }

    public PenaltyGRS_Ratings(RecommenderSystem<? extends Object> singleUserRecommender, PenaltyFunction penaltyFunction, Grouper grouper, Number completePreferencesThreshold) {
        this();
        setParameterValue(SINGLE_USER_RECOMMENDER, singleUserRecommender);
        setParameterValue(PENALTY, penaltyFunction);
        setParameterValue(ITEM_GROUPER, grouper);
        if (completePreferencesThreshold == null) {
            setParameterValue(COMPLETE_PREFERENCES, false);
        } else {
            setParameterValue(COMPLETE_PREFERENCES, true);
            setParameterValue(COMPLETE_PREFERENCES_THRESHOLD, completePreferencesThreshold);
        }
    }

    @Override
    public SingleRecommendationModel build(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {

        RecommenderSystemBuildingProgressListener buildListener = (String actualJob, int percent, long remainingTime) -> {
            fireBuildingProgressChangedEvent(actualJob, percent, remainingTime);
        };

        if (isCompleteUngivenPreferences()) {
            knnUser.build(datasetLoader);
        }

        getSingleUserRecommender().addBuildingProgressListener(buildListener);
        Object build = getSingleUserRecommender().build(datasetLoader);
        getSingleUserRecommender().removeBuildingProgressListener(buildListener);
        return new SingleRecommendationModel(build);
    }

    @Override
    public GroupModelPseudoUser buildGroupModel(DatasetLoader<? extends Rating> datasetLoader, SingleRecommendationModel RecommendationModel, GroupOfUsers groupOfUsers) throws UserNotFound, CannotLoadRatingsDataset {

        PenaltyFunction penaltyFunction = (PenaltyFunction) getParameterValue(PENALTY);
        Grouper itemGrouper = (Grouper) getParameterValue(ITEM_GROUPER);
        RelevanceCriteria completePreferencesThreshold = getCompletePreferencesThreshold();

        DatasetLoader<? extends Rating> newDatasetLoader;

        if (isCompleteUngivenPreferences()) {
            RatingsDataset<Rating> completeRatingsDataset = AggregationOfIndividualRatings_itemWeighted.completeRatings(this, knnUser, groupOfUsers, datasetLoader, completePreferencesThreshold);
            DatasetLoaderGiven<Rating> datasetLoaderGiven = new DatasetLoaderGiven<>(datasetLoader, completeRatingsDataset);
            newDatasetLoader = datasetLoaderGiven;
        } else {
            newDatasetLoader = datasetLoader;
        }

        Map<Integer, Map<Integer, Number>> memberRatings = DatasetUtilities.getMembersRatings_byUser(groupOfUsers, newDatasetLoader);

        Map<Integer, Map<Integer, Number>> memberRatingsByItem = DatasetUtilities.transformIndexedByUsersToIndexedByItems_Map(memberRatings);
        Map<Integer, Number> groupAggregatedProfile = PenaltyMethods.aggregateWithPenalty_combinatory(
                memberRatingsByItem,
                groupOfUsers,
                penaltyFunction,
                itemGrouper);

        return new GroupModelPseudoUser(groupOfUsers, groupAggregatedProfile);
    }

    @Override
    public Collection<Recommendation> recommendOnly(
            DatasetLoader<? extends Rating> datasetLoader, SingleRecommendationModel RecommendationModel, GroupModelPseudoUser groupModel, GroupOfUsers groupOfUsers, java.util.Set<Integer> candidateItems)
            throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {

        RecommenderSystem recommenderSystem = getSingleUserRecommender();
        Map<Integer, Number> groupRatings_Number = groupModel.getRatings();
        Map<Integer, Rating> groupRatings = DatasetUtilities.getUserMap_Rating(-1, groupRatings_Number);

        PseudoUserRatingsDataset<Rating> ratingsDataset_withPseudoUser = new PseudoUserRatingsDataset<>(
                datasetLoader.getRatingsDataset(),
                groupRatings);
        final int idGroup = ratingsDataset_withPseudoUser.getIdPseudoUser();

        if (Global.isVerboseAnnoying()) {
            DatasetPrinterDeprecated.printCompactRatingTable(ratingsDataset_withPseudoUser, Arrays.asList(idGroup), ratingsDataset_withPseudoUser.getUserRated(idGroup));
        }

        Collection<Recommendation> groupRecomendations;

        groupRecomendations = recommenderSystem.recommendOnly(
                new DatasetLoaderGiven(datasetLoader, ratingsDataset_withPseudoUser),
                RecommendationModel.getRecommendationModel(),
                idGroup,
                candidateItems);

        return groupRecomendations;
    }

    @Override
    public boolean isRatingPredictorRS() {
        return true;
    }

    public RecommenderSystem getSingleUserRecommender() {
        return (RecommenderSystem) getParameterValue(SINGLE_USER_RECOMMENDER);
    }

    public boolean isCompleteUngivenPreferences() {
        return (Boolean) getParameterValue(COMPLETE_PREFERENCES);
    }

    private RelevanceCriteria getCompletePreferencesThreshold() {
        Number threshold = (Number) getParameterValue(COMPLETE_PREFERENCES_THRESHOLD);
        return new RelevanceCriteria(threshold);
    }
}
