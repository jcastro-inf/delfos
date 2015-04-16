package delfos.dataset.changeable;

import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.common.exceptions.dataset.CannotSaveUsersDataset;
import delfos.common.exceptions.dataset.users.UserAlreadyExists;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 16-sep-2013
 */
public interface ChangeableUsersDataset extends UsersDataset {

    /**
     * Añade el usuario especificado. Si el usuario ya existía, lanza una
     * excepción.
     *
     * @param user Información del usuario a agregar.
     */
    public void addUser(User user) throws UserAlreadyExists;

    /**
     * Ordena que los datos sean guardados en el método persistente
     * correspondiente del dataset.
     */
    public void commitChangesInPersistence() throws CannotSaveUsersDataset;
}
