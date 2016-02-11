/* 
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
