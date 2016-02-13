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
package delfos.dataset.loaders.movilens.ml100k.validation;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.DirectoryParameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.parameters.restriction.ObjectParameter;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureGenerator;
import delfos.dataset.basic.features.FeatureType;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.CompleteDatasetLoaderAbstract;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingWithTimestamp;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.basic.user.UsersDatasetAdapter;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class MovieLens100kValidation extends CompleteDatasetLoaderAbstract<RatingWithTimestamp> {

    private static final long serialVersionUID = 1L;
    public final static Parameter DirectoryOfDataset = new Parameter("Directory", new DirectoryParameter(new File(
            ".." + File.separator
            + ".." + File.separator
            + ".." + File.separator
            + "Datasets" + File.separator
            + "MovieLens" + File.separator
            + "MovieLens-100k ratings" + File.separator
            + "ml-100k" + File.separator)));
    public final static Parameter Index_init_genres = new Parameter("Index_init_genres", new IntegerParameter(0, 1000, 5));
    public final static Parameter Partition = new Parameter("Partition", new ObjectParameter(Arrays.asList("1", "2", "3", "4", "5", "a", "b"), "1"));
    private RatingsDataset<RatingWithTimestamp> ratingsDataset = null;
    private RatingsDataset<RatingWithTimestamp> ratingsDataset_test = null;
    private ContentDataset contentDataset = null;
    private UsersDataset usersDataset = null;

    public MovieLens100kValidation() {
        addParameter(DirectoryOfDataset);
        addParameter(Index_init_genres);

        addParameter(Partition);

        addParammeterListener(() -> {
            ratingsDataset = null;
            contentDataset = null;
            usersDataset = null;
            ratingsDataset_test = null;
        });
    }

    public MovieLens100kValidation(File directory, String partition) {
        this();
        setParameterValue(DirectoryOfDataset, directory);
        setParameterValue(Partition, partition);
    }

    @Override
    public RatingsDataset<RatingWithTimestamp> getRatingsDataset() throws CannotLoadRatingsDataset {
        if (ratingsDataset == null) {
            ratingsDataset = getRatingsDatasetFromFile(getRatingsDatasetFile());
        }
        return ratingsDataset;
    }

    public RatingsDataset<RatingWithTimestamp> getRatingsDataset_test() throws CannotLoadRatingsDataset {
        if (ratingsDataset_test == null) {
            ratingsDataset_test = getRatingsDatasetFromFile(getRatingsDatasetTestFile());
        }
        return ratingsDataset_test;
    }

    private BothIndexRatingsDataset<RatingWithTimestamp> getRatingsDatasetFromFile(File ratingsFile) throws CannotLoadRatingsDataset {
        try {
            List<Rating> ratings = new LinkedList<>();

            // Abrimos el archivo
            FileInputStream fstream = new FileInputStream(ratingsFile);
            // Creamos el Buffer de Lectura
            try ( // Creamos el objeto de entrada
                    DataInputStream entrada = new DataInputStream(fstream)) {
                // Creamos el Buffer de Lectura
                BufferedReader buffer = new BufferedReader(new InputStreamReader(entrada));
                String linea;
                // Leer el archivo linea por linea
                while ((linea = buffer.readLine()) != null) {
                    String[] campos = linea.split("\t");

                    int idUser = Integer.parseInt(campos[0]);
                    int idItem = Integer.parseInt(campos[1]);
                    byte ratingValue = Byte.parseByte(campos[2]);
                    long timestamp = Long.parseLong(campos[3]);

                    Rating rating = new RatingWithTimestamp(idUser, idItem, ratingValue, timestamp);
                    ratings.add(rating);
                }
            }

            return new BothIndexRatingsDataset(ratings);
        } catch (IOException | NumberFormatException ex) {
            throw new CannotLoadRatingsDataset(ex);
        }
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {
        if (contentDataset == null) {
            Map<String, Integer> generos_byName = new TreeMap<>();
            List<String> generos_byIndex = new ArrayList<>();
            final int indexInicialGeneros = getIndexInicialGeneros();

            //Cargo el archivo de géneros.
            try {
                // Abrimos el archivo
                FileInputStream fstream = new FileInputStream(getGenreFile());
                // Creamos el Buffer de Lectura
                try ( // Creamos el objeto de entrada
                        DataInputStream entrada = new DataInputStream(fstream)) {
                    // Creamos el Buffer de Lectura
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(entrada));
                    String linea;
                    // Leer el archivo linea por linea
                    while ((linea = buffer.readLine()) != null) {
                        if (linea.isEmpty() || linea.trim().isEmpty()) {
                            continue;
                        }
                        String[] campos = linea.split("\\|");
                        String genreName = campos[0];
                        int genreIndex = Integer.parseInt(campos[1]);

                        while (genreIndex >= generos_byIndex.size()) {
                            generos_byIndex.add(null);
                        }

                        generos_byName.put(genreName, genreIndex);
                        generos_byIndex.set(genreIndex, genreName);

                    }
                }
            } catch (IOException | NumberFormatException ex) {
                throw new CannotLoadContentDataset(ex);
            }

            FeatureGenerator featureGenerator = new FeatureGenerator();
            try {

                TreeSet<Item> items = new TreeSet<>();

                // Abrimos el archivo
                FileInputStream fstream = new FileInputStream(getContentDatasetFile());
                // Creamos el Buffer de Lectura
                try ( // Creamos el objeto de entrada
                        DataInputStream entrada = new DataInputStream(fstream)) {
                    // Creamos el Buffer de Lectura
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(entrada));
                    String line;
                    // Leer el archivo linea por linea
                    while ((line = buffer.readLine()) != null) {
                        // Imprimimos la línea por pantalla

                        String[] fields = line.split("\\|");

                        int idItem = Integer.parseInt(fields[0]);
                        String itemName = fields[1];
                        String date = fields[2];
                        String unknownField = fields[3];
                        String imdbUrl = fields[4];

                        Map<Feature, String> nominalFeatures = new TreeMap<>();

                        for (int indexGenero = indexInicialGeneros; indexGenero < fields.length; indexGenero++) {
                            String featureName = generos_byIndex.get(indexGenero - indexInicialGeneros);
                            String featureValue = fields[indexGenero];

                            if (!featureGenerator.containsFeature(featureName)) {
                                featureGenerator.createFeature(featureName, FeatureType.Nominal);
                            }
                            Feature thisGenreFeature = featureGenerator.searchFeature(featureName);
                            nominalFeatures.put(thisGenreFeature, featureValue);
                        }

                        Item item = new Item(
                                idItem,
                                itemName,
                                nominalFeatures.keySet().toArray(new Feature[0]),
                                nominalFeatures.values().toArray());
                        items.add(item);

                    }
                }

                contentDataset = new ContentDatasetDefault(items);
            } catch (IOException | NumberFormatException ex) {
                throw new CannotLoadContentDataset(ex);
            }
        }
        return contentDataset;
    }

    @Override
    public UsersDataset getUsersDataset() throws CannotLoadUsersDataset {

        if (usersDataset == null) {

            File occupationFile = getOccupationFile();

            TreeSet<String> occupations = new TreeSet<>();
            TreeSet<User> users = new TreeSet<>();
            try {
                try (BufferedReader br = new BufferedReader(new FileReader(occupationFile))) {
                    String line = br.readLine();
                    while (line != null) {
                        if (line.isEmpty()) {
                            continue;
                        }
                        occupations.add(line);
                        line = br.readLine();
                    }
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(MovieLens100kValidation.class.getName()).log(Level.SEVERE, null, ex);
                }

            } catch (IOException ex) {
                throw new CannotLoadUsersDataset(ex);
            }

            FeatureGenerator featureGenerator = new FeatureGenerator();

            Feature ageFeature = featureGenerator.createFeature("age", FeatureType.Numerical);
            Feature genderFeature = featureGenerator.createFeature("gender", FeatureType.Nominal);
            Feature occupationFeature = featureGenerator.createFeature("occupation", FeatureType.Nominal);
            Feature zipCodeFeature = featureGenerator.createFeature("zipcode", FeatureType.Nominal);

            File usersFile = getUsersDatasetFile();
            try {
                try (BufferedReader br = new BufferedReader(new FileReader(usersFile))) {
                    String line = br.readLine();
                    while (line != null) {
                        if (line.isEmpty()) {
                            continue;
                        }

                        Map<Feature, Object> featureValues = new TreeMap<>();

                        String[] fields = line.split("\\|");

                        int idUser = Integer.parseInt(fields[0]);
                        int age = Integer.parseInt(fields[1]);
                        String gender = fields[2];
                        String occupation = fields[3];
                        String zipCode = fields[4];

                        featureValues.put(ageFeature, age);
                        featureValues.put(genderFeature, gender);
                        featureValues.put(occupationFeature, occupation);
                        featureValues.put(zipCodeFeature, zipCode);

                        User user = new User(idUser, "ml100k-user_" + idUser, featureValues);

                        users.add(user);
                        line = br.readLine();
                    }
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(MovieLens100kValidation.class.getName()).log(Level.SEVERE, null, ex);
                }

            } catch (IOException ex) {
                throw new CannotLoadUsersDataset(ex);
            }
            usersDataset = new UsersDatasetAdapter(users);

        }

        return usersDataset;
    }

    @Override
    public RelevanceCriteria getDefaultRelevanceCriteria() {
        return new RelevanceCriteria(4);
    }

    private File getContentDatasetFile() {
        String filename = ((File) getParameterValue(DirectoryOfDataset)).getAbsoluteFile() + File.separator + "u.item";
        return new File(filename);
    }

    private File getUsersDatasetFile() {
        String filename = ((File) getParameterValue(DirectoryOfDataset)).getAbsoluteFile() + File.separator + "u.user";
        return new File(filename);
    }

    private File getOccupationFile() {
        String filename = ((File) getParameterValue(DirectoryOfDataset)).getAbsoluteFile() + File.separator + "u.user";
        return new File(filename);
    }

    private File getGenreFile() {
        String filename = ((File) getParameterValue(DirectoryOfDataset)).getAbsoluteFile() + File.separator + "u.genre";
        return new File(filename);
    }

    private int getIndexInicialGeneros() {
        return (Integer) getParameterValue(Index_init_genres);
    }

    private File getRatingsDatasetFile() {
        String partition = getParameterValue(Partition).toString();
        String filename = ((File) getParameterValue(DirectoryOfDataset)).getAbsoluteFile()
                + File.separator + "u" + partition + ".base";
        return new File(filename);
    }

    private File getRatingsDatasetTestFile() {
        String partition = getParameterValue(Partition).toString();
        String filename = ((File) getParameterValue(DirectoryOfDataset)).getAbsoluteFile()
                + File.separator + "u" + partition + ".test";
        return new File(filename);
    }
}
