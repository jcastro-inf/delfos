package delfos.dataset.basic.loader.types;

import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.TrustDatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;

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
public abstract class CompleteDatasetLoaderAbstract<RatingType extends Rating> extends ParameterOwnerAdapter implements Comparable<Object>, DatasetLoader<RatingType>, ContentDatasetLoader, UsersDatasetLoader {

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
}
