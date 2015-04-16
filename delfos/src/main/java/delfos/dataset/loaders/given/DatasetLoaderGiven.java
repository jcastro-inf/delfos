package delfos.dataset.loaders.given;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.basic.loader.types.CompleteDatasetLoaderAbstract_withTrust;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;

/**
 * Dataset loader a partir de los datasets de contenido y de valoraciones.
 *
* @author Jorge Castro Gallardo
 *
 * @version Unknown date
 * @version 26-Noviembre-2013
 * @param <RatingType>
 */
public class DatasetLoaderGiven<RatingType extends Rating> extends CompleteDatasetLoaderAbstract_withTrust<RatingType> implements ContentDatasetLoader {

    private static final long serialVersionUID = 1L;
    private final DatasetLoader<? extends Rating> datasetLoader;
    private final RatingsDataset<RatingType> ratingsDataset;

    public DatasetLoaderGiven(DatasetLoader<? extends Rating> datasetLoader, RatingsDataset<RatingType> ratingsDataset) {
        this.datasetLoader = datasetLoader;
        this.ratingsDataset = ratingsDataset;

        setAlias(datasetLoader.getAlias());
    }

    @Override
    public RelevanceCriteria getDefaultRelevanceCriteria() {
        return datasetLoader.getDefaultRelevanceCriteria();
    }

    @Override
    public RatingsDataset<RatingType> getRatingsDataset() throws CannotLoadRatingsDataset {
        return ratingsDataset;
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {
        final ContentDataset contentDataset;
        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            contentDataset = contentDatasetLoader.getContentDataset();
        } else {
            throw new CannotLoadContentDataset("The dataset loader is not a ContentDatasetLoader, cannot apply a content-based ");
        }

        return contentDataset;
    }

    @Override
    public UsersDataset getUsersDataset() throws CannotLoadUsersDataset {
        final UsersDataset usersDataset;
        if (datasetLoader instanceof UsersDatasetLoader) {
            UsersDatasetLoader usersDatasetLoader = (UsersDatasetLoader) datasetLoader;
            usersDataset = usersDatasetLoader.getUsersDataset();
        } else {
            throw new CannotLoadUsersDataset("The dataset loader is not a UsersDatasetLoader, cannot return the users dataset");
        }

        return usersDataset;
    }
}
