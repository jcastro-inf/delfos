package delfos.dataset.storage.validationdatasets;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;

/**
 *
 * @version 14-may-2014
* @author Jorge Castro Gallardo
 * @param <RatingType>
 */
public interface TestRatingsDataset<RatingType extends Rating> extends RatingsDataset<RatingType> {

    public RatingsDataset<RatingType> getOriginalDataset();

}
