package delfos.dataset.basic.user;

import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.features.CollectionOfEntitiesWithFeatures;

/**
 * Interfaz que define los métodos genéricos de un dataset que almacena los
 * datos de los usuarios de un sistema de recomendación.
 *
* @author Jorge Castro Gallardo
 *
 * @version 16-sep-2013
 */
public interface UsersDataset extends CollectionOfEntitiesWithFeatures<User> {

    /**
     * Obtiene los datos del usuario especificado.
     *
     * @param idUser
     * @return
     * @throws delfos.common.exceptions.dataset.users.UserNotFound
     */
    public User getUser(int idUser) throws UserNotFound;
}
