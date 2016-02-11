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
package delfos.io.csv.dataset.item;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import delfos.dataset.basic.item.ContentDataset;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 13-mar-2014
 */
public interface ContentDatasetToCSV {

    /**
     * Extensión de un archivo csv (sin el punto).
     */
    public static final String CSV_EXTENSION = "csv";
    /**
     * Nombre de la columna que almacena el id de producto.
     */
    public static final String ID_ITEM_COLUMN_NAME = "idItem";
    /*
     * Nombre de la columna que almacena el nombre de producto.
     */
    public static final String ITEM_NAME_COLUMN_NAME = "name";

    public ContentDataset readContentDataset(File contentCSV) throws CannotLoadContentDataset, FileNotFoundException;

    public void writeDataset(ContentDataset contentDataset, String fileName) throws IOException;

}
