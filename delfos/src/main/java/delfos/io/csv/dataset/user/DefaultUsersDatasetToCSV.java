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

import com.csvreader.CsvReader;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureGenerator;
import delfos.dataset.basic.features.FeatureType;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.basic.user.UsersDatasetAdapter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/**
 * Clase para leer/escribir un dataset de contenido a fichero csv.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 04-Mar-2013
 */
public class DefaultUsersDatasetToCSV implements UsersDatasetToCSV {

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

    public DefaultUsersDatasetToCSV() {
        this('"', ',', '\n');
    }

    public DefaultUsersDatasetToCSV(char stringDelimiter, char columnSeparator, char rowSeparator) {
        this.stringDelimiter = stringDelimiter;
        this.columnDelimiter = columnSeparator;
        this.registerDelimiter = rowSeparator;
    }

    @Override
    public void writeDataset(UsersDataset usersDataset, String fileName) throws IOException {

        String fileNameWithExtension = fileName;
        if (!fileNameWithExtension.endsWith(".csv")) {
            fileNameWithExtension = fileNameWithExtension + "." + CSV_EXTENSION;
        }

        StringBuilder stringBuilder = new StringBuilder();
        Feature[] features = usersDataset.getFeatures();

        stringBuilder.append(ID_USER_COLUMN_NAME).append(columnDelimiter);
        stringBuilder.append(USER_NAME_COLUMN_NAME);

        for (Feature userFeature : features) {
            stringBuilder.append(columnDelimiter).append(stringDelimiter).append(userFeature.getExtendedName()).append(stringDelimiter);
        }
        stringBuilder.append(registerDelimiter);

        for (User user : usersDataset) {

            int idUser = user.getId();

            StringBuilder linea = new StringBuilder();

            //Escribir el id y el nombre
            linea.append(idUser).append(columnDelimiter).append(stringDelimiter).append(user.getName()).append(stringDelimiter);

            for (Feature userFeature : features) {
                Object featureValue = user.getFeatureValue(userFeature);
                String featureValueString;

                if (featureValue != null) {
                    switch (userFeature.getType()) {
                        case Numerical:
                            featureValueString = featureValue.toString();
                            break;
                        default:
                            featureValueString = stringDelimiter + featureValue.toString() + stringDelimiter;
                            break;
                    }
                } else {
                    featureValueString = Feature.NULL_VALUE;
                }
                linea.append(columnDelimiter).append(featureValueString);
            }
            linea.append(registerDelimiter);
            stringBuilder.append(linea.toString());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileNameWithExtension)))) {
            bw.write(stringBuilder.toString());
        }
    }

    @Override
    public UsersDataset readUsersDataset(File usersFile) throws CannotLoadUsersDataset, FileNotFoundException {

        try {
            FeatureGenerator featureGenerator = new FeatureGenerator();
            int defaultIndexUsers = 1;
            TreeSet<User> users = new TreeSet<>();
            CsvReader reader = new CsvReader(
                    new FileInputStream(usersFile.getAbsolutePath()),
                    Charset.forName("UTF-8"));
            reader.setRecordDelimiter('\n');
            reader.setDelimiter(',');
            reader.readHeaders();
            String[] headers = reader.getHeaders();
            if (headers.length < 2) {
                throw new CannotLoadUsersDataset("Wrong headers in CSV file '" + usersFile + "'.");
            }

            String userIDcolumnName = null;
            String nameColumn = null;
            String latitudeColumn = null;
            String longitudeColumn = null;

            LinkedList<Feature> features = new LinkedList<>();
            LinkedList<Integer> indexFeatures = new LinkedList<>();

            for (int i = 0; i < headers.length; i++) {
                String head = headers[i].trim();
                if (head.equals("idUser") || head.equals("userID")) {
                    userIDcolumnName = headers[i];
                    continue;
                }
                if (head.equals("name")) {
                    nameColumn = headers[i];
                    continue;
                }
                if (head.equals("latitude")) {
                    latitudeColumn = headers[i];
                    continue;
                }
                if (head.equals("longitude")) {
                    longitudeColumn = headers[i];
                    continue;
                }
                FeatureType featureType = FeatureType.inferTypeByNameWithSuffix(head);
                String fetureRealName = featureType.getFeatureRealName(head);

                if (!featureGenerator.containsFeature(fetureRealName)) {
                    featureGenerator.createFeature(fetureRealName, featureType);
                }

                Feature feature = featureGenerator.searchFeature(fetureRealName);

                features.add(feature);
                indexFeatures.add(i);
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

                int idUser;

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

                if (userIDcolumnName != null) {
                    try {
                        idUser = Integer.parseInt(reader.get(userIDcolumnName));
                    } catch (Throwable e) {
                        Global.showWarning("Error: no se pudo obtener el id del user de la cadena: \"" + reader.get("idUser") + "\"");
                        Global.showError(e);
                        idUser = j--;
                    }
                } else {
                    idUser = defaultIndexUsers++;
                }

                int i = 0;
                try {
                    List<Object> userFeatureValues = new ArrayList<>(featuresArray.length);
                    List<Feature> userFeatures = new ArrayList<>(featuresArray.length);

                    for (i = 0; i < featuresArray.length; i++) {
                        String recordHeader = headers[indexFeaturesArray[i]];
                        String recordValue = reader.get(recordHeader);
                        Feature actualFeature = featuresArray[i];

                        if (recordValue.equals(recordHeader) || recordValue.toLowerCase().equals(Feature.NULL_VALUE)) {
                            if (Global.isVerboseAnnoying()) {
                                Global.showWarning("User " + idUser + " has a null value for feature " + actualFeature + ".");
                            }
                        } else {
                            userFeatureValues.add(actualFeature.getType().parseFeatureValue(recordValue));
                            userFeatures.add(actualFeature);
                        }
                    }
                    User user = null;
                    if (latitude == null && longitude == null) {
                        user = new User(idUser, name, userFeatures.toArray(new Feature[0]), userFeatureValues.toArray());
                    } else {
                        throw new UnsupportedOperationException("Debe estar definido latitud y longitud o ninguno de los dos");
                    }
                    users.add(user);
                } catch (Throwable ex) {
                    Global.showWarning(headers[i - 1] + " value \"" + reader.get(headers[i - 1]) + "\"");
                    Global.showWarning("Ignored user " + idUser + ". Cause: " + ex.getMessage());
                    Global.showError(ex);
                }
            }
            reader.close();
            UsersDataset cd = new UsersDatasetAdapter(users);
            return cd;
        } catch (IOException | NumberFormatException ex) {
            throw new CannotLoadUsersDataset(ex);
        }
    }
}
