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

import com.csvreader.CsvReader;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureGenerator;
import delfos.dataset.basic.features.FeatureType;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

/**
 * Clase para leer/escribir un dataset de contenido a fichero csv.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 04-Mar-2013
 */
public class DefaultContentDatasetToCSV implements ContentDatasetToCSV {

    /**
     * Caracter que se utiliza para delimitar cadenas de texto. Por defecto es
     * comilla doble.
     */
    private final char stringDelimiter;
    /**
     * Carácter que se usa para separar columnas. Por defecto es la coma.
     */
    private final char columnDelimiter;
    /**
     * Carácter que se utiliza para separar registros. Por defecto es salto de
     * línea (\n).
     */
    private final char registerDelimiter;

    public DefaultContentDatasetToCSV() {
        this('"', ',', '\n');
    }

    public DefaultContentDatasetToCSV(char stringDelimiter, char columnSeparator, char rowSeparator) {
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
            int defaultIndexItems = 1;
            TreeSet<Item> items = new TreeSet<>();

            CsvReader reader = new CsvReader(
                    new FileInputStream(contentCSV.getAbsolutePath()),
                    Charset.forName("UTF-8"));

            reader.setRecordDelimiter('\n');
            reader.setDelimiter(',');

            reader.readHeaders();
            String[] headers = reader.getHeaders();
            if (headers.length < 2) {
                throw new CannotLoadContentDataset("Wrong headers in CSV file '" + contentCSV + "'.");
            }

            String itemIDcolumnName = null;
            String nameColumn = null;
            String latitudeColumn = null;
            String longitudeColumn = null;

            LinkedList<Feature> features = new LinkedList<>();
            LinkedList<Integer> indexFeatures = new LinkedList<>();

            for (int indexHeader = 0; indexHeader < headers.length; indexHeader++) {
                String header = headers[indexHeader];
                String head = header.trim();
                if (head.equals("idItem") || head.equals("itemID")) {
                    itemIDcolumnName = header;
                    continue;
                }
                if (head.equals("name")) {
                    nameColumn = header;
                    continue;
                }
                if (head.equals("latitude")) {
                    latitudeColumn = header;
                    continue;
                }
                if (head.equals("longitude")) {
                    longitudeColumn = header;
                    continue;
                }
                try {
                    FeatureType featureType = FeatureType.inferTypeByNameWithSuffix(head);
                    String realName = head.substring(0, head.lastIndexOf(featureType.getSufix()));
                    if (!featureGenerator.containsFeature(realName)) {
                        featureGenerator.createFeature(realName, featureType);
                    }
                    features.add(featureGenerator.searchFeature(realName));
                    indexFeatures.add(indexHeader);
                } catch (IllegalArgumentException ex) {
                    throw new CannotLoadContentDataset("The header: " + head + " is not valid, unrecognised sufix.", ex);
                }
            }

            Feature[] featuresArray = new Feature[features.size()];
            int index = 0;
            for (Feature f : features) {
                featuresArray[index] = f;
                index++;
            }

            Integer[] indexFeaturesArray = new Integer[indexFeatures.size()];
            index = 0;
            for (int i : indexFeatures) {
                indexFeaturesArray[index] = i;
                index++;
            }

            int j = 40499;
            while (reader.readRecord()) {
                Float latitude;
                Float longitude;

                int idItem;

                String name;
                if (nameColumn == null) {
                    name = "unknow";
                } else {
                    name = reader.get(nameColumn);
                }

                if (latitudeColumn != null) {
                    latitude = Float.parseFloat(reader.get(latitudeColumn));
                } else {
                    latitude = null;
                }

                if (longitudeColumn != null) {
                    longitude = Float.parseFloat(reader.get(longitudeColumn));
                } else {
                    longitude = null;
                }

                if (itemIDcolumnName != null) {
                    try {
                        idItem = Integer.parseInt(reader.get(itemIDcolumnName));
                    } catch (Throwable e) {
                        Global.showWarning("Error: no se pudo obtener el id del item de la cadena: \"" + reader.get("idItem") + "\"");
                        Global.showError(e);
                        idItem = j--;
                    }
                } else {
                    idItem = defaultIndexItems++;
                }

                int i;
                try {

                    List<Feature> itemFeatures = new ArrayList<>(featuresArray.length);
                    List<Object> itemFeatureValues = new ArrayList<>(featuresArray.length);
                    for (i = 0; i < featuresArray.length; i++) {
                        String recordHeader = headers[indexFeaturesArray[i]];
                        String recordValue = reader.get(recordHeader);
                        Feature actualFeature = featuresArray[i];

                        itemFeatures.add(actualFeature);
                        if (recordValue.equals(recordHeader) || Feature.isNullValue(recordValue)) {
                            if (Global.isVerboseAnnoying()) {
                                Global.showWarning("Item " + idItem + " has a null value for feature " + actualFeature + ".");
                            }
                            itemFeatureValues.add(null);
                        } else {
                            itemFeatureValues.add(actualFeature.getType().parseFeatureValue(recordValue));
                        }
                    }
                    Item item = null;
                    if (latitude != null && longitude != null) {
                        DecimalFormat threeDec = new DecimalFormat("0.00000000000000000000", new DecimalFormatSymbols(Locale.ENGLISH));
                        Global.showInfoMessage("lat:" + threeDec.format(latitude) + "\tlng: " + threeDec.format(longitude) + "\t\t" + idItem + ": " + name + "\n");
                        item = new Item(idItem, name, itemFeatures.toArray(new Feature[0]), itemFeatureValues.toArray(), latitude, longitude);
                    } else {
                        if (latitude == null && longitude == null) {
                            item = new Item(idItem, name, itemFeatures.toArray(new Feature[0]), itemFeatureValues.toArray());
                        } else {
                            throw new UnsupportedOperationException("Debe estar definido latitud y longitud o ninguno de los dos");
                        }
                    }
                    items.add(item);
                } catch (Throwable ex) {
                    //Global.showError(headers[i - 1] + " value \"" + reader.get(headers[i - 1]) + "\"");
                    Global.showWarning("Ignored item " + idItem + ". Cause: " + ex.getMessage());
                    Global.showError(ex);
                }
            }
            reader.close();
            ContentDataset cd = new ContentDatasetDefault(items);
            return cd;
        } catch (IOException | NumberFormatException ex) {
            throw new CannotLoadContentDataset(ex);
        }
    }
}
