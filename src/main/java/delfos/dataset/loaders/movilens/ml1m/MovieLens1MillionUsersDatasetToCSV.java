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
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureGenerator;
import delfos.dataset.basic.features.FeatureType;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.basic.user.UsersDatasetAdapter;
import static delfos.io.csv.dataset.item.ContentDatasetToCSV.ID_ITEM_COLUMN_NAME;
import static delfos.io.csv.dataset.item.ContentDatasetToCSV.ITEM_NAME_COLUMN_NAME;
import delfos.io.csv.dataset.rating.CSVReader;
import delfos.io.csv.dataset.user.UsersDatasetToCSV;
import static delfos.io.csv.dataset.user.UsersDatasetToCSV.CSV_EXTENSION;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Clase para leer/escribir un dataset de contenido a fichero csv.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 04-Mar-2013
 */
public class MovieLens1MillionUsersDatasetToCSV implements UsersDatasetToCSV {

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

    public MovieLens1MillionUsersDatasetToCSV() {
        this(",", "\n", "\"");
    }

    public MovieLens1MillionUsersDatasetToCSV(String columnSeparator, String rowSeparator, String stringDelimiter) {
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

        stringBuilder.append(ID_ITEM_COLUMN_NAME).append(columnDelimiter);
        stringBuilder.append(ITEM_NAME_COLUMN_NAME);

        for (Feature userFeature : features) {
            String extendedName = userFeature.getExtendedName();
            stringBuilder.append(columnDelimiter).append(stringDelimiter).append(extendedName).append(stringDelimiter);
        }

        stringBuilder.append(registerDelimiter);

        for (User user : usersDataset) {

            long idUser = user.getId();
            StringBuilder linea = new StringBuilder();

            //Escribir el id y el nombre
            linea.append(idUser).append(columnDelimiter).append(stringDelimiter).append(user.getName()).append(stringDelimiter);
            for (Feature userFeature : features) {
                Object featureValue = user.getFeatureValue(userFeature);
                String featureValueString = userFeature.getType().featureValueToString(featureValue);
                linea.append(columnDelimiter).append(featureValueString);
            }

            linea.append(registerDelimiter);
            stringBuilder.append(linea.toString());
        }

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(fileNameWithExtension)));
        bufferedWriter.write(stringBuilder.toString());
        bufferedWriter.flush();
        bufferedWriter.close();

    }

    @Override
    public UsersDataset readUsersDataset(File usersCSV) throws CannotLoadUsersDataset, FileNotFoundException {

        try {
            FeatureGenerator featureGenerator = new FeatureGenerator();

            TreeSet<User> users = new TreeSet<>();
            CSVReader reader = new CSVReader(usersCSV, "\"", "::");

            final int idUserColumn = 0;
            final int genderColumn = 1;
            final int ageColumn = 2;
            final int occupationColumn = 3;
            final int zipCodeColumn = 4;

            Feature genderFeature = featureGenerator.createFeature("Gender", FeatureType.Nominal);
            Feature ageFeature = featureGenerator.createFeature("Age", FeatureType.Numerical);
            Feature occupationFeature = featureGenerator.createFeature("Occupation", FeatureType.Nominal);
            Feature zipCodeFeature = featureGenerator.createFeature("Zip - Code", FeatureType.Nominal);

            int numLine = 0;
            while (reader.readRecord()) {
                numLine++;
                int idUser = Integer.parseInt(reader.get(idUserColumn));
                String gender = reader.get(genderColumn);
                int age = Integer.parseInt(reader.get(ageColumn));

                int occupationID = Integer.parseInt(reader.get(occupationColumn));

                String occupation;
                switch (occupationID) {
                    case 0:
                        occupation = "other";
                        break;
                    case 1:
                        occupation = "academic/educator";
                        break;
                    case 2:
                        occupation = "artist";
                        break;
                    case 3:
                        occupation = "clerical/admin";
                        break;
                    case 4:
                        occupation = "college/grad student";
                        break;
                    case 5:
                        occupation = "customer service";
                        break;
                    case 6:
                        occupation = "doctor/health care";
                        break;
                    case 7:
                        occupation = "executive/managerial";
                        break;
                    case 8:
                        occupation = "farmer";
                        break;
                    case 9:
                        occupation = "homemaker";
                        break;
                    case 10:
                        occupation = "K-12 student";
                        break;
                    case 11:
                        occupation = "lawyer";
                        break;
                    case 12:
                        occupation = "programmer";
                        break;
                    case 13:
                        occupation = "retired";
                        break;
                    case 14:
                        occupation = "sales/marketing";
                        break;
                    case 15:
                        occupation = "scientist";
                        break;
                    case 16:
                        occupation = "self-employed";
                        break;
                    case 17:
                        occupation = "technician/engineer";
                        break;
                    case 18:
                        occupation = "tradesman/craftsman";
                        break;
                    case 19:
                        occupation = "unemployed";
                        break;
                    case 20:
                        occupation = "writer";
                        break;
                    default:
                        throw new IndexOutOfBoundsException("Line " + numLine + ": The occupation id '" + occupationID + "' is not defined.");

                }

                String zipCode = reader.get(zipCodeColumn);

                Map<Feature, Object> featureValues = new TreeMap<>();
                featureValues.put(genderFeature, gender);
                featureValues.put(ageFeature, age);
                featureValues.put(occupationFeature, occupation);
                featureValues.put(zipCodeFeature, zipCode);

                User user = new User(idUser, "User_" + idUser, featureValues);
                users.add(user);
            }
            reader.close();
            UsersDataset cd = new UsersDatasetAdapter(users);
            return cd;
        } catch (IOException ex) {
            throw new CannotLoadContentDataset(ex);
        } catch (NumberFormatException ex) {
            throw new CannotLoadContentDataset(ex);
        }
    }
}
