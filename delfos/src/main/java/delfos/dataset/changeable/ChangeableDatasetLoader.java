package delfos.dataset.changeable;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;

/**
 * Interfaz de un dataset que permite cambios.
 *
* @author Jorge Castro Gallardo
 * @version 28-Noviembre-2013
 */
public interface ChangeableDatasetLoader extends DatasetLoader<Rating>, ContentDatasetLoader, UsersDatasetLoader {

    /**
     * Obtiene el dataset de contenido que se usará en la recomendación
     *
     * @return dataset de contenido que se usará en la recomendación
     */
    ChangeableContentDataset getChangeableContentDataset() throws CannotLoadContentDataset;

    /**
     * Obtiene el dataset de ratings en memoria que se usará en la
     * recomendación.
     *
     * @return Dataset de ratings.
     */
    ChangeableRatingsDataset getChangeableRatingsDataset() throws CannotLoadRatingsDataset;

    ChangeableUsersDataset getChangeableUsersDataset() throws CannotLoadUsersDataset;

    /**
     * Crea las estructuras necesarias para almacenar en persistencia el
     * dataset. Si ya existía, se debe lanzar un error.
     *
     * @throws IllegalStateException Cuando la base de datos ya estaba
     * inicializada.
     */
    public void initStructures();

    public void commitChangesInPersistence();
}
