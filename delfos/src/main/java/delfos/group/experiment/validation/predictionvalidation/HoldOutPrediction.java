package delfos.group.experiment.validation.predictionvalidation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.FloatParameter;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.loaders.given.DatasetLoaderGiven;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.generated.modifieddatasets.changeratings.RatingsDatasetOverwrite;
import delfos.dataset.util.DatasetUtilities;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Implementa el protocolo de predicción HoldOut, que divide el conjunto de
 * productos valorados por todos los usuarios en dos conjuntos disjuntos:
 * entrenamiento y evaluación. Lo hace de manera que un cierto porcentaje de
 * productos estén en el conjunto de entrenamiento, y el resto en el de
 * evaluación
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 (10-12-2012)
 */
public class HoldOutPrediction extends GroupPredictionProtocol {

    /**
     * Parametro para establecer el porcentaje de productos que se utilizan en
     * el conjunto de entrenamiento.
     */
    public static final Parameter trainingPercent = new Parameter("trainingPercent", new FloatParameter(0f, 1f, 0.80f));

    public HoldOutPrediction() {
        super();
        addParameter(trainingPercent);
    }

    private Collection<Integer> getItemsToPredict(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers group, long seed) throws CannotLoadRatingsDataset {

        Random random = new Random(getSeedValue());
        Set<Integer> ratedProducts = new TreeSet<>();
        for (int idUser : group.getGroupMembers()) {
            try {
                ratedProducts.addAll(datasetLoader.getRatingsDataset().getUserRated(idUser));
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }
        Set<Integer> trainSet = new TreeSet<>();
        final float trainingPercentValue = (Float) getParameterValue(trainingPercent);

        while ((trainSet.size() / (float) ratedProducts.size()) < trainingPercentValue) {

            int idItem = (Integer) ratedProducts.toArray()[random.nextInt(ratedProducts.size())];
            ratedProducts.remove(idItem);
            trainSet.add(idItem);
        }
        return ratedProducts;
    }

    @Override
    public Collection<GroupRecommendationRequest> getGroupRecommendationRequests(DatasetLoader<? extends Rating> trainDatasetLoader, DatasetLoader<? extends Rating> testDatasetLoader, GroupOfUsers group) throws CannotLoadRatingsDataset, UserNotFound {

        Collection<Integer> itemsToPredict = getItemsToPredict(testDatasetLoader, group, getSeedValue());

        Map<Integer, Map<Integer, Number>> membersRatings_byItem = DatasetUtilities.getMembersRatings_byItem(group, testDatasetLoader);
        itemsToPredict.stream().forEach((idItem) -> membersRatings_byItem.remove(idItem));

        Map<Integer, Map<Integer, Number>> predictionMembersRatings_byUser = DatasetUtilities.transformIndexedByItemToIndexedByUser_Map(membersRatings_byItem);
        Map<Integer, Map<Integer, Rating>> predictionMembersRatings_byUser_Rating = DatasetUtilities.getMapOfMaps_Rating(predictionMembersRatings_byUser);

        DatasetLoader<Rating> predictionPhaseDatasetLoader
                = new DatasetLoaderGiven<>(trainDatasetLoader,
                        RatingsDatasetOverwrite.createRatingsDataset((RatingsDataset<Rating>) trainDatasetLoader.getRatingsDataset(), predictionMembersRatings_byUser_Rating));

        return Arrays.asList(new GroupRecommendationRequest(group, predictionPhaseDatasetLoader, itemsToPredict));
    }
}
