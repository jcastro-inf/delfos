package delfos.dataset.basic.loader.types;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.basic.user.UsersDatasetAdapter;
import java.util.stream.Collectors;

/**
 * Establece las operaciones que un <code>DatasetLoader</code> debe implementar.
 * Un <code>DatasetLoader</code> se encarga de cargar un dataset de
 * recomendación para su posterior uso con un sistema de recomendación.
 *
 * @author Jorge Castro Gallardo
 * @version 1.0 Unknown date.
 * @version 1.0.1 26-Mayo-2013
 * @version 1.0.2 15-Noviembre-2013
 * @param <RatingType>
 */
public abstract class DatasetLoaderAbstract<RatingType extends Rating> extends ParameterOwnerAdapter implements Comparable<Object>, DatasetLoader<RatingType> {

    public static final void loadFullDataset(DatasetLoader<? extends Rating> datasetLoader) {
        datasetLoader.getRatingsDataset();

        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            contentDatasetLoader.getContentDataset();

        }

        if (datasetLoader instanceof UsersDatasetLoader) {
            UsersDatasetLoader usersDatasetLoader = (UsersDatasetLoader) datasetLoader;
            usersDatasetLoader.getUsersDataset();
        }

        if (datasetLoader instanceof TrustDatasetLoader) {
            TrustDatasetLoader trustDatasetLoader = (TrustDatasetLoader) datasetLoader;
            trustDatasetLoader.getTrustDataset();
        }

    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof DatasetLoader) {
            DatasetLoader parameterOwner = (DatasetLoader) o;
            return ParameterOwnerAdapter.compare(this, parameterOwner);
        }

        throw new IllegalArgumentException("The type is not valid, must be a " + DatasetLoader.class);
    }

    @Override
    public final ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.DATASET_LOADER;
    }

    @Override
    public RelevanceCriteria getDefaultRelevanceCriteria() {
        return new RelevanceCriteria(4);
    }

    @Override
    public UsersDataset getUsersDataset() throws CannotLoadUsersDataset {

        RatingsDataset<RatingType> ratingsDataset = getRatingsDataset();

        return new UsersDatasetAdapter(ratingsDataset
                .allUsers().stream()
                .map(idUser -> new User(idUser))
                .collect(Collectors.toSet()));

    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {

        RatingsDataset<RatingType> ratingsDataset = getRatingsDataset();

        return new ContentDatasetDefault(ratingsDataset
                .allRatedItems().stream()
                .map(idItem -> new Item(idItem))
                .collect(Collectors.toSet()));

    }
}
