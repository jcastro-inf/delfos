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
package delfos.dataset.loaders.movilens.ml1m;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureGenerator;
import delfos.dataset.basic.features.FeatureType;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import delfos.io.csv.dataset.item.ContentDatasetToCSV;
import delfos.io.csv.dataset.rating.CSVReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Clase para leer/escribir un dataset de contenido a fichero csv.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 04-Mar-2013
 */
public class MovieLens1MillionContentDatasetToCSV implements ContentDatasetToCSV {

    /**
     * Caracter que se utiliza para delimitar cadenas de texto. Por defecto es
     * comilla doble.
     */
    private final String stringDelimiter;
    /**
     * Carácter que se usa para separar columnas. Por defecto es la coma.
     */
    private final String columnDelimiter;
    /**
     * Carácter que se utiliza para separar registros. Por defecto es salto de
     * línea (\n).
     */
    private final String registerDelimiter;

    public MovieLens1MillionContentDatasetToCSV() {
        this(",", "\n", "\"");
    }

    public MovieLens1MillionContentDatasetToCSV(String columnSeparator, String rowSeparator, String stringDelimiter) {
        this.stringDelimiter = stringDelimiter;
        this.columnDelimiter = columnSeparator;
        this.registerDelimiter = rowSeparator;
    }

    @Override
    public void writeDataset(ContentDataset contentDataset, String fileName) throws IOException {

        String fileNameWithExtension = fileName;
        if (!fileNameWithExtension.endsWith(".csv")) {
            fileNameWithExtension = fileNameWithExtension + "." + CSV_EXTENSION;
        }

        StringBuilder stringBuilder = new StringBuilder();

        Feature[] features = contentDataset.getFeatures();

        stringBuilder.append(ID_ITEM_COLUMN_NAME).append(columnDelimiter);
        stringBuilder.append(ITEM_NAME_COLUMN_NAME);

        for (Feature itemFeature : features) {
            String extendedName = itemFeature.getExtendedName();
            stringBuilder.append(columnDelimiter).append(stringDelimiter).append(extendedName).append(stringDelimiter);
        }

        stringBuilder.append(registerDelimiter);

        for (Item item : contentDataset) {

            int idItem = item.getId();
            StringBuilder linea = new StringBuilder();

            //Escribir el id y el nombre
            linea.append(idItem).append(columnDelimiter).append(stringDelimiter).append(item.getName()).append(stringDelimiter);
            for (Feature itemFeature : features) {
                Object featureValue = item.getFeatureValue(itemFeature);
                String featureValueString = itemFeature.getType().featureValueToString(featureValue);
                linea.append(columnDelimiter).append(featureValueString);
            }

            linea.append(registerDelimiter);
            stringBuilder.append(linea.toString());
        }

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(fileNameWithExtension)))) {
            bufferedWriter.write(stringBuilder.toString());
            bufferedWriter.flush();
        }

    }

    @Override
    public ContentDataset readContentDataset(File contentCSV) throws CannotLoadContentDataset, FileNotFoundException {

        try {
            FeatureGenerator featureGenerator = new FeatureGenerator();

            TreeSet<Item> items = new TreeSet<>();
            CSVReader reader = new CSVReader(contentCSV, "\"", "::");

            final int idItemColumn = 0;
            final int titleColumn = 1;
            final int genresColumn = 2;

            Feature genresFeature = featureGenerator.createFeature("Genres", FeatureType.MultiNominal);

            while (reader.readRecord()) {

                int idItem = Integer.parseInt(reader.get(idItemColumn));
                String name = reader.get(titleColumn);
                String rawGenres = reader.get(genresColumn);
                String[] split = rawGenres.split("\\|");

                List<String> genresValues = Arrays.asList(split);

                Map<Feature, Object> featureValues = new TreeMap<Feature, Object>();
                featureValues.put(genresFeature, genresValues);

                Item item = new Item(idItem, name, featureValues);
                items.add(item);
            }
            reader.close();
            ContentDataset cd = new ContentDatasetDefault(items);
            return cd;
        } catch (IOException ex) {
            throw new CannotLoadContentDataset(ex);
        } catch (NumberFormatException ex) {
            throw new CannotLoadContentDataset(ex);
        }
    }
}
