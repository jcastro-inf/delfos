package delfos.similaritymeasures.useruser;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.basic.loader.types.CompleteDatasetLoaderAbstract;
import delfos.dataset.loaders.movilens.ml100k.MovieLens100k;

/**
 *
 * @version 08-may-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class MockDatasetLoader_UserUserSimilarity extends CompleteDatasetLoaderAbstract<Rating> {

    private final RatingsDataset ratingsDataset;
    private final ContentDataset contentDataset;
    private final UsersDataset usersDataset;

    public MockDatasetLoader_UserUserSimilarity() {
        MovieLens100k ml100k = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k", MovieLens100k.class);
        ratingsDataset = ml100k.getRatingsDataset();
        contentDataset = ml100k.getContentDataset();
        usersDataset = ml100k.getUsersDataset();
    }

    @Override
    public RatingsDataset<Rating> getRatingsDataset() throws CannotLoadRatingsDataset {
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

}
