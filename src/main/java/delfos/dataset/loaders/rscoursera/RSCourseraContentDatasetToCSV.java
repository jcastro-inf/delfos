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
package delfos.dataset.loaders.rscoursera;

import com.csvreader.CsvReader;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import delfos.io.csv.dataset.item.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.TreeSet;

/**
 * Clase para leer/escribir un dataset de contenido a fichero csv.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 04-Mar-2013
 */
public class RSCourseraContentDatasetToCSV implements ContentDatasetToCSV {

    private final int itemIdColumn = 0;
    private final int itemNameColumn = 1;

    public RSCourseraContentDatasetToCSV() {
    }

    @Override
    public void writeDataset(ContentDataset contentDataset, String fileName) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public ContentDataset readContentDataset(File contentCSV) throws CannotLoadContentDataset, FileNotFoundException {

        try {

            TreeSet<Item> items = new TreeSet<>();

            CsvReader reader = new CsvReader(
                    new FileInputStream(contentCSV.getAbsolutePath()),
                    Charset.forName("UTF-8"));

            reader.setRecordDelimiter('\n');
            reader.setDelimiter(',');

            int line = 1;
            while (reader.readRecord()) {

                try {
                    int idItem = Integer.parseInt(reader.get(itemIdColumn));
                    String name = reader.get(itemNameColumn);
                    items.add(new Item(idItem, name));
                } catch (NumberFormatException ex) {
                    Global.showWarning("Cannot read item in line " + line + " of file " + contentCSV.getAbsolutePath() + "\n");
                    Global.showWarning(ex);
                }
                line++;

                line++;
            }
            reader.close();
            ContentDataset cd = new ContentDatasetDefault(items);
            return cd;
        } catch (IOException | NumberFormatException ex) {
            throw new CannotLoadContentDataset(ex);
        }
    }
}
