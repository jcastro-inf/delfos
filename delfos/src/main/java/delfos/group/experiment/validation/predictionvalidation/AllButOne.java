package delfos.group.experiment.validation.predictionvalidation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.loaders.given.DatasetLoaderGiven;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.generated.modifieddatasets.changeratings.RatingsDatasetOverwrite;
import delfos.dataset.util.DatasetUtilities;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Implementa el protocolo de predicción todos menos uno, predice cada
 * valoración teniendo en cuenta el resto de valoraciones. Se realizan n
 * predicciones por grupo, donde n es el número de productos valorados por el
 * grupo.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 (10-12-2012)
 */
public class AllButOne extends GroupPredictionProtocol {

    public AllButOne() {
        super();
    }

    private Collection<Integer> getRatedItems(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers group) throws CannotLoadRatingsDataset, UserNotFound {
        return DatasetUtilities.getMembersRatings_byItem(group, datasetLoader).keySet();
    }

    @Override
    public Collection<GroupRecommendationRequest> getGroupRecommendationRequests(
            DatasetLoader<? extends Rating> trainDatasetLoader, DatasetLoader<? extends Rating> testDatasetLoader, GroupOfUsers group) throws CannotLoadRatingsDataset, UserNotFound {

        Collection<Integer> ratedItems = getRatedItems(testDatasetLoader, group);

        ArrayList<GroupRecommendationRequest> groupRecommendationRequests = new ArrayList<>(ratedItems.size());

        for (int idItem : ratedItems) {

            //Añado todos los rating en test del grupo, excepto los ratings de este item, ya que será el item que se pida predecir.
            Map<Integer, Map<Integer, Number>> membersRatings_byItem = DatasetUtilities.getMembersRatings_byItem(group, testDatasetLoader);
            membersRatings_byItem.remove(idItem);

            Map<Integer, Map<Integer, Number>> predictionMembersRatings_byUser = DatasetUtilities.transformIndexedByItemToIndexedByUser_Map(membersRatings_byItem);
            Map<Integer, Map<Integer, Rating>> predictionMembersRatings_byUser_Rating = DatasetUtilities.getMapOfMaps_Rating(predictionMembersRatings_byUser);

            DatasetLoader<Rating> predictionPhaseDatasetLoader
                    = new DatasetLoaderGiven<>(trainDatasetLoader,
                            RatingsDatasetOverwrite.createRatingsDataset((RatingsDataset<Rating>) trainDatasetLoader.getRatingsDataset(), predictionMembersRatings_byUser_Rating));

            Set<Integer> itemsToPredict = new TreeSet<>(Arrays.asList(idItem));
            groupRecommendationRequests.add(new GroupRecommendationRequest(group, predictionPhaseDatasetLoader, itemsToPredict));

        }
        return groupRecommendationRequests;
    }

}
