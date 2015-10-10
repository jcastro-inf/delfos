package delfos.dataset.basic.loader.types;

import delfos.dataset.basic.rating.Rating;

/**
 *
 * @author Jorge Castro Gallardo
 *
 * @version 26-nov-2013
 * @param <RatingType>
 */
public interface CompleteDatasetLoader<RatingType extends Rating> extends DatasetLoader<RatingType>, ContentDatasetLoader, UsersDatasetLoader, TaggingDatasetLoader, TrustDatasetLoader {
}
