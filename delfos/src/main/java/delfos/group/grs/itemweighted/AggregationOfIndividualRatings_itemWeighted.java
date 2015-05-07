package delfos.group.grs.itemweighted;

import delfos.Constants;
import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.aggregationoperators.AggregationOperator;
import delfos.common.aggregationoperators.Mean;
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
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.generated.modifieddatasets.PseudoUserRatingsDataset;
import delfos.dataset.loaders.given.DatasetLoaderGiven;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.dataset.util.DatasetOperations;
import delfos.dataset.util.DatasetPrinterDeprecated;
import delfos.dataset.util.DatasetUtilities;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.SingleRecommendationModel;
import delfos.group.grs.itemweighted.knn.memory.KnnMemoryBasedNWR_itemWeighted;
import delfos.group.grs.itemweighted.measures.GroupItemWeight;
import delfos.group.grs.itemweighted.measures.StandardDeviationWeights;
import delfos.rs.RecommendationModelBuildingProgressListener;
import delfos.rs.RecommenderSystem;
import delfos.rs.bufferedrecommenders.RecommenderSystem_fixedFilePersistence;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryModel;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVD;
import delfos.rs.explanation.GroupModelWithExplanation;
import delfos.rs.nonpersonalised.meanrating.arithmeticmean.MeanRatingRS;
import delfos.rs.persistence.FilePersistence;
import delfos.rs.recommendation.Recommendation;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public final class AggregationOfIndividualRatings_itemWeighted
        extends GroupRecommenderSystemAdapter<SingleRecommendationModel, GroupModelWithExplanation<GroupModelPseudoUser_itemWeighted, ? extends Object>> {

    private static final long serialVersionUID = 1L;

    /**
     * "Especifica el sistema de recomendación single user que se extiende para
     * ser usado en recomendación a grupos.
     */
    public static final Parameter SINGLE_USER_RECOMMENDER = new Parameter(
            "KnnMemoryBasedNWR_itemWeighted",
            new RecommenderSystemParameterRestriction(new KnnMemoryBasedNWR_itemWeighted(), KnnMemoryBasedNWR_itemWeighted.class),
            "Especifica el sistema de recomendación single user que se extiende "
            + "para ser usaso en recomendación a grupos.");
    /**
     * Especifica la técnica de agregación para agregar los ratings de los
     * usuarios y formar el perfil del grupo.
     */
    public static final Parameter AGGREGATION_OPPERATOR = new Parameter(
            "AGGREGATION_OPPERATOR",
            new ParameterOwnerRestriction(AggregationOperator.class, new Mean()),
            "Especifica la técnica de agregación para agregar los ratings de "
            + "los usuarios y formar el perfil del grupo.");

    public static final Parameter GROUP_ITEM_WEIGHT = new Parameter("GROUP_ITEM_WEIGHT", new ParameterOwnerRestriction(GroupItemWeight.class, new StandardDeviationWeights()));

    public static final Parameter COMPLETE_PREFERENCES = new Parameter("COMPLETE_UNGIVEN_PREFERENCES", new BooleanParameter(Boolean.FALSE));

    public static final Parameter COMPLETE_PREFERENCES_THRESHOLD = new Parameter("Default_rating_value", new FloatParameter(-1000, 1000, 4));

    public static final Parameter COMPLETE_PREFERENCES_RECOMMENDER_SYSTEM = new Parameter(
            "COMPLETE_PREFERENCES_RECOMMENDER_SYSTEM",
            new RecommenderSystemParameterRestriction(
                    new RecommenderSystem_fixedFilePersistence<>(
                            new TryThisAtHomeSVD(10, 10),
                            new FilePersistence(
                                    "complete-preferences-rs-model", "data",
                                    new File(Constants.getTempDirectory().getAbsolutePath() + File.separator + "buffered-recommendation-models" + File.separator))),
                    RecommenderSystem_fixedFilePersistence.class));

    private AggregationOperator oldAggregationOperator = new Mean();
    private GroupItemWeight oldGroupItemWeight = new StandardDeviationWeights();
    private boolean oldCompleteUngivenPreferences = false;
    private RelevanceCriteria oldCompleteUngivenPreferencesThreshold = new RelevanceCriteria(4.0);
    private RecommenderSystem oldCompletePreferencesRS = new MeanRatingRS();

    public AggregationOfIndividualRatings_itemWeighted() {
        super();
        addParameter(SINGLE_USER_RECOMMENDER);
        addParameter(AGGREGATION_OPPERATOR);

        addParameter(GROUP_ITEM_WEIGHT);
        addParameter(COMPLETE_PREFERENCES);
        addParameter(COMPLETE_PREFERENCES_THRESHOLD);
        addParameter(COMPLETE_PREFERENCES_RECOMMENDER_SYSTEM);

        addParammeterListener(() -> {
            AggregationOperator newAggregationOperator = (AggregationOperator) getParameterValue(AGGREGATION_OPPERATOR);
            GroupItemWeight newGroupItemWeight = (GroupItemWeight) getParameterValue(GROUP_ITEM_WEIGHT);
            boolean newCompleteUngivenPreferences = isCompletePreferences();
            RelevanceCriteria newCompleteUngivenPreferencesThreshold = getCompletePreferencesThreshold();
            RecommenderSystem newCompletePreferencesRS = getCompletePreferencesRS();

            String newAlias = getAlias();

            String oldAliasOldParameters
                    = "AOI_Ratings"
                    + "_" + oldAggregationOperator.getAlias()
                    + "_" + oldGroupItemWeight.getAlias();

            if (oldCompleteUngivenPreferences) {
                oldAliasOldParameters += "_completePreferences";
                oldAliasOldParameters += "_rs=" + oldCompletePreferencesRS.getAlias();
                oldAliasOldParameters += "_" + oldCompleteUngivenPreferencesThreshold.getThreshold().doubleValue();
            }

            String newAliasNewParameters
                    = "AOI_Ratings"
                    + "_" + newAggregationOperator.getAlias()
                    + "_" + newGroupItemWeight.getAlias();

            if (newCompleteUngivenPreferences) {
                newAliasNewParameters += "_completePreferences";

                newAliasNewParameters += "_rs=" + newCompletePreferencesRS.getAlias();
                newAliasNewParameters += "_" + newCompleteUngivenPreferencesThreshold.getThreshold().doubleValue();
            }

            if (!oldAliasOldParameters.equals(newAliasNewParameters)) {
                oldAggregationOperator = newAggregationOperator;
                oldGroupItemWeight = newGroupItemWeight;
                oldCompleteUngivenPreferences = newCompleteUngivenPreferences;
                oldCompletePreferencesRS = newCompletePreferencesRS;
                oldCompleteUngivenPreferencesThreshold = newCompleteUngivenPreferencesThreshold;
                setAlias(newAliasNewParameters);
            }
        });
    }

    public AggregationOfIndividualRatings_itemWeighted(KnnMemoryBasedNWR_itemWeighted singleUserRecommender) {

        this();
        setParameterValue(SINGLE_USER_RECOMMENDER, singleUserRecommender);
    }

    public AggregationOfIndividualRatings_itemWeighted(
            KnnMemoryBasedNWR_itemWeighted singleUserRecommender,
            AggregationOperator aggregationOperator) {

        this();
        setParameterValue(SINGLE_USER_RECOMMENDER, singleUserRecommender);
        setParameterValue(AGGREGATION_OPPERATOR, aggregationOperator);
    }

    public AggregationOfIndividualRatings_itemWeighted(
            KnnMemoryBasedNWR_itemWeighted singleUserRecommender,
            AggregationOperator aggregationOperator,
            GroupItemWeight groupItemWeight,
            Number completePreferencesThreshold) {

        this();
        setParameterValue(SINGLE_USER_RECOMMENDER, singleUserRecommender);
        setParameterValue(AGGREGATION_OPPERATOR, aggregationOperator);
        setParameterValue(GROUP_ITEM_WEIGHT, groupItemWeight);
        if (completePreferencesThreshold == null) {
            setParameterValue(COMPLETE_PREFERENCES, false);
        } else {
            setParameterValue(COMPLETE_PREFERENCES, true);
            setParameterValue(COMPLETE_PREFERENCES_THRESHOLD, completePreferencesThreshold);
        }
    }

    public AggregationOfIndividualRatings_itemWeighted(
            KnnMemoryBasedNWR_itemWeighted singleUserRecommender,
            AggregationOperator aggregationOperator,
            GroupItemWeight groupItemWeight,
            Number completePreferencesThreshold,
            RecommenderSystem_fixedFilePersistence completePreferencesRS) {
        this(singleUserRecommender, aggregationOperator, groupItemWeight, completePreferencesThreshold);
        setParameterValue(COMPLETE_PREFERENCES_RECOMMENDER_SYSTEM, completePreferencesRS);

    }

    @Override
    public SingleRecommendationModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {

        RecommendationModelBuildingProgressListener buildListener = this::fireBuildingProgressChangedEvent;
        getSingleUserRecommender().addRecommendationModelBuildingProgressListener(buildListener);
        Object singleUserRecommendationModel = getSingleUserRecommender().buildRecommendationModel(datasetLoader);
        getSingleUserRecommender().removeRecommendationModelBuildingProgressListener(buildListener);

        if (isCompletePreferences()) {
            RecommenderSystem completePreferencesRS = getCompletePreferencesRS();
            completePreferencesRS.addRecommendationModelBuildingProgressListener(buildListener);
            completePreferencesRS.buildRecommendationModel(datasetLoader);
            completePreferencesRS.removeRecommendationModelBuildingProgressListener(buildListener);
        }
        return new SingleRecommendationModel(singleUserRecommendationModel);
    }

    @Override
    public GroupModelWithExplanation<GroupModelPseudoUser_itemWeighted, ? extends Object> buildGroupModel(DatasetLoader<? extends Rating> datasetLoader, SingleRecommendationModel RecommendationModel, GroupOfUsers groupOfUsers) throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {

        GroupModelWithExplanation<GroupModelPseudoUser_itemWeighted, ? extends Object> groupModelWithExplanation;
        AggregationOperator aggregationOperator = getAggregationOperator();
        GroupItemWeight groupItemWeight = getGroupItemWeight();
        RelevanceCriteria completeUngivenPreferencesThreshold = getCompletePreferencesThreshold();

        DatasetLoader<? extends Rating> newDatasetLoader;

        if (isCompletePreferences()) {
            RecommenderSystem_fixedFilePersistence completePreferencesRS = getCompletePreferencesRS();
            RatingsDataset<Rating> completeRatingsDataset = completeRatings(this, completePreferencesRS, groupOfUsers, datasetLoader, completeUngivenPreferencesThreshold);
            DatasetLoaderGiven<Rating> datasetLoaderGiven = new DatasetLoaderGiven<>(datasetLoader, completeRatingsDataset);
            newDatasetLoader = datasetLoaderGiven;
        } else {
            newDatasetLoader = datasetLoader;
        }

        Map< Integer, Number> groupRatings = getGroupProfile(newDatasetLoader, aggregationOperator, groupOfUsers);
        Map<Integer, ? extends Number> itemWeights = groupItemWeight.getItemWeights(newDatasetLoader, groupOfUsers);

        Map<Integer, Double> itemWeightsDouble = new TreeMap<>();
        itemWeights.entrySet().stream().forEach((entry) -> {
            itemWeightsDouble.put(entry.getKey(), entry.getValue().doubleValue());
        });

        GroupModelPseudoUser_itemWeighted groupModel = new GroupModelPseudoUser_itemWeighted(groupRatings, itemWeightsDouble, groupOfUsers);
        groupModelWithExplanation = new GroupModelWithExplanation<>(groupModel, "No explanantion");

        return groupModelWithExplanation;
    }

    public static RatingsDataset<Rating> completeRatings(GroupRecommenderSystem grs, RecommenderSystem_fixedFilePersistence rs, GroupOfUsers groupOfUsers, DatasetLoader<? extends Rating> datasetLoader, RelevanceCriteria relevanceCriteria) throws CannotLoadRatingsDataset, UserNotFound {
        Object recommendationModel = rs.buildRecommendationModel(datasetLoader);

        Map<Integer, Map<Integer, Number>> membersRatings = DatasetUtilities.getMembersRatings_byUser(groupOfUsers, datasetLoader);
        Set<Integer> itemUnion = DatasetUtilities.transformIndexedByUsersToIndexedByItems_Map(membersRatings).keySet();

        Map<Integer, Map<Integer, Number>> completedGroupRatings = new TreeMap<>();

        for (int idUser : membersRatings.keySet()) {
            Set<Integer> toPredict = new TreeSet<>(itemUnion);
            toPredict.removeAll(membersRatings.get(idUser).keySet());

            Collection<Recommendation> recommendOnly;
            try {
                recommendOnly = rs.recommendToUser(datasetLoader, recommendationModel, idUser, toPredict);
            } catch (ItemNotFound | CannotLoadContentDataset | NotEnoughtUserInformation ex) {
                ERROR_CODES.UNDEFINED_ERROR.exit(ex);
                throw new IllegalStateException("arg");
            }

            Map<Integer, Number> thisMemberPredictions = new TreeMap<>();
            recommendOnly
                    .stream()
                    .filter(recommendation -> relevanceCriteria.isRelevant(recommendation.getPreference()))
                    .forEach((recommendation) -> {
                        thisMemberPredictions.put(recommendation.getIdItem(), recommendation.getPreference());
                    });

            completedGroupRatings.put(idUser, new TreeMap<>());
            completedGroupRatings.get(idUser).putAll(thisMemberPredictions);
            completedGroupRatings.get(idUser).putAll(membersRatings.get(idUser));
        }
        RatingsDataset<Rating> completedRatingsDataset = getCompletedRatingsDataset(datasetLoader, completedGroupRatings);

        return completedRatingsDataset;
    }

    public static RatingsDataset<Rating> getCompletedRatingsDataset(DatasetLoader<? extends Rating> datasetLoader, Map<Integer, Map<Integer, Number>> completedGroupRatings) throws CannotLoadRatingsDataset, UserNotFound {
        TreeSet<Integer> usersNotInGroup = new TreeSet<>(datasetLoader.getRatingsDataset().allUsers());
        usersNotInGroup.removeAll(completedGroupRatings.keySet());

        Map<Integer, Map<Integer, Rating>> selectRatings = DatasetOperations.selectRatings((RatingsDataset<Rating>) datasetLoader.getRatingsDataset(), usersNotInGroup);

        BothIndexRatingsDataset rd1 = new BothIndexRatingsDataset<>(selectRatings);

        RatingsDataset< Rating> rd = new BothIndexRatingsDataset<>(
                rd1,
                DatasetOperations.convertNumberToRatings(completedGroupRatings));
        return rd;
    }

    @Override
    public Collection<Recommendation> recommendOnly(
            DatasetLoader<? extends Rating> datasetLoader, SingleRecommendationModel RecommendationModel, GroupModelWithExplanation<GroupModelPseudoUser_itemWeighted, ? extends Object> groupModel, GroupOfUsers groupOfUsers, java.util.Set<Integer> candidateItems)
            throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {

        //Recojo los parámetros en variables
        RecommenderSystem recommenderSystem = getSingleUserRecommender();

        if (recommenderSystem instanceof KnnMemoryBasedNWR_itemWeighted) {
            Map<Integer, Number> groupRatings_Number = groupModel.getGroupModel().getRatings();
            Map<Integer, Double> itemWeights = groupModel.getGroupModel().getItemWeights();
            Collection<Recommendation> groupRecom = recommendWithRatingsAndWeights(datasetLoader, recommenderSystem, RecommendationModel, groupRatings_Number, itemWeights, candidateItems);
            return groupRecom;
        } else {
            throw new IllegalStateException("Cannot use this GRS with a " + recommenderSystem.getAlias() + ", must be a " + KnnMemoryBasedNWR_itemWeighted.class);
        }

    }

    @Override
    public boolean isRatingPredictorRS() {
        return true;
    }

    public AggregationOperator getAggregationOperator() {
        return (AggregationOperator) getParameterValue(AGGREGATION_OPPERATOR);
    }

    public GroupItemWeight getGroupItemWeight() {
        return (GroupItemWeight) getParameterValue(GROUP_ITEM_WEIGHT);
    }

    public RecommenderSystem getSingleUserRecommender() {
        return (RecommenderSystem) getParameterValue(SINGLE_USER_RECOMMENDER);
    }

    public static Map<Integer, Number> getGroupProfile(
            DatasetLoader<? extends Rating> datasetLoader,
            AggregationOperator aggregationOperator,
            GroupOfUsers groupOfUsers) throws UserNotFound, CannotLoadRatingsDataset {

        //Generate groupProfile:
        Map<Integer, List<Number>> groupRatingsList = new TreeMap<>();

        for (int idUser : groupOfUsers.getGroupMembers()) {
            Map<Integer, ? extends Rating> userRatingsRated = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser);
            userRatingsRated.keySet().stream().map((idItem) -> {
                if (!groupRatingsList.containsKey(idItem)) {
                    groupRatingsList.put(idItem, new LinkedList<>());
                }
                return idItem;
            }).forEach((idItem) -> {
                groupRatingsList.get(idItem).add(userRatingsRated.get(idItem).ratingValue);
            });
        }

        //Aggregate profiles
        Map<Integer, Number> groupRatings = new TreeMap<>();
        groupRatingsList.keySet().stream().forEach((idItem) -> {
            List<Number> lista = groupRatingsList.get(idItem);
            float aggregateValue = aggregationOperator.aggregateValues(lista);
            groupRatings.put(idItem, aggregateValue);
        });

        return groupRatings;
    }

    public static Collection<Recommendation> recommendWithRatingsAndWeights(
            DatasetLoader<? extends Rating> datasetLoader,
            RecommenderSystem recommenderSystem,
            SingleRecommendationModel RecommendationModel,
            Map<Integer, Number> groupRatings,
            Map<Integer, Double> itemWeights,
            Collection<Integer> candidateItems) throws ItemNotFound, NotEnoughtUserInformation, UserNotFound, CannotLoadContentDataset, CannotLoadRatingsDataset {

        Map<Integer, Rating> groupRatings_Ratings = DatasetUtilities.getUserMap_Rating(-1, groupRatings);
        PseudoUserRatingsDataset<Rating> ratingsDataset_withPseudoUser = new PseudoUserRatingsDataset<>(
                datasetLoader.getRatingsDataset(),
                groupRatings_Ratings);
        final int idGroup = ratingsDataset_withPseudoUser.getIdPseudoUser();
        if (Global.isVerboseAnnoying()) {
            DatasetPrinterDeprecated.printCompactRatingTable(ratingsDataset_withPseudoUser, Arrays.asList(idGroup), ratingsDataset_withPseudoUser.getUserRated(idGroup));
        }
        Collection<Recommendation> groupRecom;

        if (recommenderSystem instanceof KnnMemoryBasedNWR_itemWeighted) {
            KnnMemoryBasedNWR_itemWeighted knnMemoryBasedNWR_NaturalNoise = (KnnMemoryBasedNWR_itemWeighted) recommenderSystem;

            groupRecom = knnMemoryBasedNWR_NaturalNoise.recommendOnlyWithItemWeighting(
                    new DatasetLoaderGiven(datasetLoader, ratingsDataset_withPseudoUser),
                    (KnnMemoryModel) RecommendationModel.getRecommendationModel(),
                    idGroup,
                    itemWeights,
                    candidateItems);
        } else {
            throw new IllegalStateException("Cannot use this GRS with a " + recommenderSystem.getAlias() + ", must be a " + KnnMemoryBasedNWR_itemWeighted.class);
        }
        return groupRecom;
    }

    private boolean isCompletePreferences() {
        return (Boolean) getParameterValue(COMPLETE_PREFERENCES);
    }

    public RelevanceCriteria getCompletePreferencesThreshold() {
        Number threshold = (Number) getParameterValue(COMPLETE_PREFERENCES_THRESHOLD);
        return new RelevanceCriteria(threshold);
    }

    public RecommenderSystem_fixedFilePersistence getCompletePreferencesRS() {
        return (RecommenderSystem_fixedFilePersistence) getParameterValue(COMPLETE_PREFERENCES_RECOMMENDER_SYSTEM);
    }
}
