package delfos.io.csv.dataset.user;

import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.dataset.basic.user.UsersDataset;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author Jorge Castro Gallardo
 *
 * @version 13-mar-2014
 */
public interface UsersDatasetToCSV {

    /**
     * Extensión de un archivo csv (sin el punto).
     */
    public static final String CSV_EXTENSION = "csv";
    /**
     * Nombre de la columna que almacena el id de producto.
     */
    public static final String ID_USER_COLUMN_NAME = "idUser";
    /*
     * Nombre de la columna que almacena el nombre de producto.
     */
    public static final String USER_NAME_COLUMN_NAME = "name";

    public UsersDataset readUsersDataset(File usersFile)
            throws CannotLoadUsersDataset, FileNotFoundException;

    public void writeDataset(UsersDataset usersDataset, String fileName) throws IOException;

}
