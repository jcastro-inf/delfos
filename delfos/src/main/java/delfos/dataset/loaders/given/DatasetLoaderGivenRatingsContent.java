package delfos.dataset.loaders.given;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.UsersDataset;

/**
 *
 * @author Jorge Castro Gallardo
 *
 * @version 26-nov-2013
 * @param <RatingType>
 */
public class DatasetLoaderGivenRatingsContent<RatingType extends Rating> extends DatasetLoaderAbstract<RatingType> {

    private final ContentDataset contentDataset;
    private final RatingsDataset<RatingType> ratingsDataset;
    private final UsersDataset usersDataset;

    private static final long serialVersionUID = 1L;

    public DatasetLoaderGivenRatingsContent(RatingsDataset<RatingType> ratingsDataset, ContentDataset contentDataset, UsersDataset usersDataset) {
        this.ratingsDataset = ratingsDataset;
        this.contentDataset = contentDataset;
        this.usersDataset = usersDataset;
    }

    @Override
    public RatingsDataset<RatingType> getRatingsDataset() throws CannotLoadRatingsDataset {
        return ratingsDataset;
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {
        return contentDataset;
    }

    @Override
    public UsersDataset getUsersDataset() throws CannotLoadUsersDataset {
        return usersDataset;
    }

    @Override
    public RelevanceCriteria getDefaultRelevanceCriteria() {
        return new RelevanceCriteria(4);
    }
}
