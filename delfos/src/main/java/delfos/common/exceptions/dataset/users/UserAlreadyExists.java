package delfos.common.exceptions.dataset.users;

/**
 * Excepción que indica que se ha intentado añadir un usuario al dataset de
 * usuarios y ya existía.
 *
* @author Jorge Castro Gallardo
 *
 * @version 16-sep-2013
 */
public class UserAlreadyExists extends Exception {

    private static final long serialVersionUID = 1L;
    private final int idUser;

    public UserAlreadyExists(int idUser) {
        this.idUser = idUser;
    }

    public UserAlreadyExists(int idUser, Throwable cause) {
        super(cause);
        this.idUser = idUser;
    }

    public int getIdUser() {
        return idUser;
    }
}
