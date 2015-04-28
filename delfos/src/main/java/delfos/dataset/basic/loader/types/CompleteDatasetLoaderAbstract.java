package delfos.dataset.basic.loader.types;

import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.dataset.basic.rating.Rating;

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
public abstract class CompleteDatasetLoaderAbstract<RatingType extends Rating> extends DatasetLoaderAbstract<RatingType> implements Comparable<Object>, DatasetLoader<RatingType>, ContentDatasetLoader, UsersDatasetLoader {

    @Override
    public int compareTo(Object o) {
        if (o instanceof DatasetLoader) {
            DatasetLoader parameterOwner = (DatasetLoader) o;
            return ParameterOwnerAdapter.compare(this, parameterOwner);
        }

        throw new IllegalArgumentException("The type is not valid, must be a " + DatasetLoader.class);
    }
}
