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
