package delfos.group.grs.hesitant;

import delfos.common.aggregationoperators.Mean;
import delfos.common.decimalnumbers.NumberCompare;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parallelwork.MultiThreadExecutionManager;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.BooleanParameter;
import delfos.common.parameters.restriction.ObjectParameter;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.dataset.generated.modifieddatasets.PseudoUserRatingsDataset;
import delfos.dataset.loaders.given.DatasetLoaderGivenRatingsDataset;
import delfos.dataset.util.DatasetUtilities;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.knn.memorybased.nwr.KnnMemoryBasedNWR;
import delfos.rs.collaborativefiltering.predictiontechniques.PredictionTechnique;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationWithNeighbors;
import es.jcastro.hesitant.HesitantValuation;
import es.jcastro.hesitant.similarity.HesitantPearson;
import es.jcastro.hesitant.similarity.HesitantSimilarity;
import es.jcastro.hesitant.similarity.factory.HesitantSimilarityFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author jcastro
 */
public class HesitantKnnGroupUser
        extends GroupRecommenderSystemAdapter<Object, HesitantValuation> {

    /**
     * Parámetro para indicar la medida de similitud que el sistema de
     * recomendación utiliza para el cálculo de los vecinos más cercanos. Si no
     * se modifica, su valor por defecto es la suma ponderada
     * ({@link CosineCoefficient})
     */
    public static final Parameter HESITANT_SIMILARITY_MEASURE = new Parameter(
            "SIMILARITY_MEASURE",
            new ObjectParameter(
                    HesitantSimilarityFactory.getAll(),
                    HesitantSimilarityFactory.getHesitantSimilarity(HesitantPearson.class.getSimpleName())
            )
    );

    public static final Parameter NEIGHBORHOOD_SIZE = delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender.NEIGHBORHOOD_SIZE;
    public static final Parameter PREDICTION_TECHNIQUE = delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender.PREDICTION_TECHNIQUE;

    public static final Parameter DELETE_REPEATED = new Parameter("Delete_repeated", new BooleanParameter(Boolean.FALSE));

    public HesitantKnnGroupUser() {
        super();

        addParameter(NEIGHBORHOOD_SIZE);
        addParameter(HESITANT_SIMILARITY_MEASURE);
        addParameter(PREDICTION_TECHNIQUE);
        addParameter(DELETE_REPEATED);
    }

    @Override
    public boolean isRatingPredictorRS() {
        return true;
    }

    @Override
    public Object buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset {
        return 1;
    }

    @Override
    public HesitantValuation buildGroupModel(DatasetLoader<? extends Rating> datasetLoader, Object RecommendationModel, GroupOfUsers groupOfUsers) throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {
        HesitantValuation<Item, Double> hesitantProfile = getHesitantProfile(datasetLoader, groupOfUsers.getMembers());

        if (isDeleteRepeatedOn()) {
            Comparator<Double> comparator = (Double o1, Double o2) -> NumberCompare.compare(o1, o2);
            hesitantProfile = hesitantProfile.deleteRepeated(comparator);
        }
        return hesitantProfile;
    }

    @Override
    public Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, Object RecommendationModel, HesitantValuation groupModel, GroupOfUsers groupOfUsers, Set<Integer> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {

        try {
            List<Neighbor> neighbors;
            RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
            neighbors = getNeighbors(datasetLoader, groupModel, groupOfUsers);

            int neighborhoodSize = (int) getParameterValue(NEIGHBORHOOD_SIZE);

            PredictionTechnique predictionTechnique = (PredictionTechnique) getParameterValue(PREDICTION_TECHNIQUE);
            int idPseudoUser = -1;
            Map<Integer, Rating> groupRatings = DatasetUtilities.getUserMap_Rating(idPseudoUser,
                    AggregationOfIndividualRatings.getGroupProfile(datasetLoader, new Mean(), groupOfUsers));

            RatingsDataset<? extends Rating> pseudoUserRatingsDatasetForPrediction = new PseudoUserRatingsDataset<>(ratingsDataset, groupRatings);
            DatasetLoader<? extends Rating> datasetLoaderNew = new DatasetLoaderGivenRatingsDataset<>(datasetLoader, pseudoUserRatingsDatasetForPrediction);
            Collection<Recommendation> ret = KnnMemoryBasedNWR.recommendWithNeighbors(
                    datasetLoaderNew,
                    idPseudoUser,
                    neighbors,
                    neighborhoodSize, candidateItems,
                    predictionTechnique);

            Collection<Recommendation> retWithNeighbors = ret.stream()
                    .map(recommendation -> new RecommendationWithNeighbors(recommendation.getItem(), recommendation.getPreference(), neighbors))
                    .collect(Collectors.toList());

            return retWithNeighbors;
        } catch (CannotLoadRatingsDataset ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private List<Neighbor> getNeighbors(
            DatasetLoader<? extends Rating> datasetLoader,
            HesitantValuation groupModel,
            GroupOfUsers groupOfUsers) {

        HesitantSimilarity similarity = (HesitantSimilarity) getParameterValue(HESITANT_SIMILARITY_MEASURE);

        Stream<HesitantKnnNeighborSimilarityTask> stream = datasetLoader.getUsersDataset()
                .stream()
                .map(idNeighbor -> new HesitantKnnNeighborSimilarityTask(
                                datasetLoader, groupOfUsers, groupModel, idNeighbor, similarity));

        List<HesitantKnnNeighborSimilarityTask> tasks = stream.collect(Collectors.toList());

        MultiThreadExecutionManager<HesitantKnnNeighborSimilarityTask> executionManager = new MultiThreadExecutionManager<>(
                "Find neighbors of group " + groupOfUsers,
                tasks,
                HesitantKnnNeighborSimilarityTaskExecutor.class);

        executionManager.run();

        List<Neighbor> neighbors = executionManager.getAllFinishedTasks().parallelStream()
                .map((tarea) -> {
                    if (tarea.getNeighbor() == null) {
                        return new Neighbor(RecommendationEntity.USER, tarea.neighborUser, 0);
                    } else {
                        return tarea.getNeighbor();
                    }
                })
                .collect(Collectors.toList());

        Collections.sort(neighbors);

        return neighbors;
    }

    public static HesitantValuation<Item, Double> getHesitantProfile(DatasetLoader<? extends Rating> datasetLoader, Collection<User> users) {
        Collection<HesitantValuation.HesitantSingleValuation<Item, Double>> valuations = new ArrayList<>();

        final RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

        datasetLoader.getContentDataset();
        for (User user : users) {
            Collection<? extends Rating> ratings = ratingsDataset.getUserRatingsRated(user.getId()).values();
            for (Rating rating : ratings) {
                Item item = rating.getItem();
                double ratingValue = rating.getRatingValue().doubleValue();
                HesitantValuation.HesitantSingleValuation<Item, Double> valuation
                        = new HesitantValuation.HesitantSingleValuation<>(item, ratingValue);
                valuations.add(valuation);
            }
        }

        HesitantValuation<Item, Double> groupProfileHesitant = new HesitantValuation<>(valuations);
        return groupProfileHesitant;
    }

    private boolean isDeleteRepeatedOn() {
        return (Boolean) getParameterValue(DELETE_REPEATED);
    }
}
