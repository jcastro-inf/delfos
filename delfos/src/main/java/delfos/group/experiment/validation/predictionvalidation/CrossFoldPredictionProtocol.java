package delfos.group.experiment.validation.predictionvalidation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.loaders.given.DatasetLoaderGiven;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.generated.modifieddatasets.changeratings.RatingsDatasetOverwrite;
import delfos.dataset.util.DatasetUtilities;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Implementa el protocolo de predicción similar a la validación cruzada.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 (10-12-2012)
 */
public class CrossFoldPredictionProtocol extends GroupPredictionProtocol {

    public static final Parameter numFolds = new Parameter("c", new IntegerParameter(2, Integer.MAX_VALUE, 5));

    public CrossFoldPredictionProtocol() {
        super();

        addParameter(numFolds);
    }

    public CrossFoldPredictionProtocol(int c) {
        this();
        setParameterValue(numFolds, c);
    }

    private Collection<Integer> getRatedItems(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers group) throws CannotLoadRatingsDataset, UserNotFound {
        return DatasetUtilities.getMembersRatings_byItem(group, datasetLoader).keySet();
    }

    @Override
    public Collection<GroupRecommendationRequest> getGroupRecommendationRequests(DatasetLoader<? extends Rating> trainDatasetLoader, DatasetLoader<? extends Rating> testDatasetLoader, GroupOfUsers group) throws CannotLoadRatingsDataset, UserNotFound {

        Random random = new Random(getSeedValue());

        ArrayList<Collection<Integer>> crossFoldValidations = new ArrayList<>();
        Collection<Integer> items = getRatedItems(testDatasetLoader, group);

        for (int i = 0; i < getNumPartitions(); i++) {
            crossFoldValidations.add(new ArrayList<>(items.size() / getNumPartitions() + 1));
        }
        int n = 0;
        while (!items.isEmpty()) {
            int idItem = items.toArray(new Integer[0])[random.nextInt(items.size())];
            items.remove(idItem);
            int partition = n % getNumPartitions();
            crossFoldValidations.get(partition).add(idItem);
            n++;
        }

        Collection<Integer> ratedItems = getRatedItems(testDatasetLoader, group);

        ArrayList<GroupRecommendationRequest> groupRecommendationRequests = new ArrayList<>(ratedItems.size());

        for (Collection<Integer> itemsInThisFold : crossFoldValidations) {

            Map<Integer, Map<Integer, Number>> membersRatings_byItem = DatasetUtilities.getMembersRatings_byItem(group, testDatasetLoader);

            itemsInThisFold.stream().forEach((idItem) -> membersRatings_byItem.remove(idItem));

            Map<Integer, Map<Integer, Number>> predictionMembersRatings_byUser = DatasetUtilities.transformIndexedByItemToIndexedByUser_Map(membersRatings_byItem);
            Map<Integer, Map<Integer, Rating>> predictionMembersRatings_byUser_Rating = DatasetUtilities.getMapOfMaps_Rating(predictionMembersRatings_byUser);
            DatasetLoader<Rating> predictionPhaseDatasetLoader
                    = new DatasetLoaderGiven<>(trainDatasetLoader,
                            RatingsDatasetOverwrite.createRatingsDataset(
                                    (RatingsDataset<Rating>) trainDatasetLoader.getRatingsDataset(),
                                    predictionMembersRatings_byUser_Rating));

            groupRecommendationRequests.add(new GroupRecommendationRequest(group, predictionPhaseDatasetLoader, itemsInThisFold));

        }
        return groupRecommendationRequests;
    }

    protected int getNumPartitions() {
        return (Integer) getParameterValue(numFolds);
    }
}
