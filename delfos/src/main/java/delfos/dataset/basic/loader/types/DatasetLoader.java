package delfos.dataset.basic.loader.types;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.parameters.ParameterOwner;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.UsersDataset;

/**
 * Interfaz que define los métodos de un objeto encargado de cargar conjuntos de
 * datos. Obligatoriamente, todos los {@link DatasetLoader} deben cargar al
 * menos un dataset de valoraciones, que se hará a través del método
 * {@link RatingsDatasetLoader#getRatingsDataset()}.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 26-Noviembre-2013
 * @param <RatingType> Tipo de los ratings del dataset de valoraciones.
 */
public interface DatasetLoader<RatingType extends Rating>
        extends ParameterOwner,
        RatingsDatasetLoader<RatingType>,
        Comparable<Object>,
        UsersDatasetLoader,
        ContentDatasetLoader {

    @Override
    public RatingsDataset<RatingType> getRatingsDataset() throws CannotLoadRatingsDataset;

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset;

    @Override
    public UsersDataset getUsersDataset() throws CannotLoadUsersDataset;

}
