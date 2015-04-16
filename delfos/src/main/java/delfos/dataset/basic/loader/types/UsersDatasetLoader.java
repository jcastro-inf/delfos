package delfos.dataset.basic.loader.types;

import delfos.dataset.basic.user.UsersDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 26-Noviembre-2013
 */
public interface UsersDatasetLoader {

    /**
     * Obtiene el dataset de información sobre los usuarios usuarios que se
     * usará en la recomendación.
     *
     * @return dataset de contenido que se usará en la recomendación
     */
    public UsersDataset getUsersDataset() throws CannotLoadUsersDataset;
}
