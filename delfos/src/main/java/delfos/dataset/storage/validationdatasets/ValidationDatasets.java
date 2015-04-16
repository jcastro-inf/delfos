package delfos.dataset.storage.validationdatasets;

import java.util.Map;
import java.util.Set;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;

/**
 *
 * @version 14-may-2014
* @author Jorge Castro Gallardo
 */
public class ValidationDatasets {

    private static final ValidationDatasets instance = new ValidationDatasets();

    private ValidationDatasets() {

    }

    public static ValidationDatasets getInstance() {
        return instance;
    }

    public <RatingType extends Rating> TrainingRatingsDataset<RatingType> createTrainingDataset(RatingsDataset<RatingType> ratingsDataset, Map<Integer, Set<Integer>> testSet) throws UserNotFound, ItemNotFound {
        return new TrainingRatingsDataset_CPU<>(ratingsDataset, testSet);
    }

    public <RatingType extends Rating> TestRatingsDataset<RatingType> createTestDataset(RatingsDataset<RatingType> ratingsDataset, Map<Integer, Set<Integer>> testSet) throws UserNotFound, ItemNotFound {
        return new TestRatingsDataset_CPU<>(ratingsDataset, testSet);
    }

}
