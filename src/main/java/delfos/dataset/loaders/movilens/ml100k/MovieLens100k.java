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
package delfos.dataset.loaders.movilens.ml100k;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.DirectoryParameter;
import delfos.common.parameters.restriction.IntegerParameter;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dataset loader para cargar los datasets de MovieLens llamados ml-100k.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 24-Julio-2013
 */
public class MovieLens100k extends CompleteDatasetLoaderAbstract<Rating> {

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
    private RatingsDataset<Rating> ratingsDataset = null;
    private ContentDataset contentDataset = null;
    private UsersDataset usersDataset = null;

    public MovieLens100k() {
        addParameter(DirectoryOfDataset);
        addParameter(Index_init_genres);

        addParammeterListener(() -> {
            ratingsDataset = null;
            contentDataset = null;
            usersDataset = null;
        });
    }

    public MovieLens100k(File directory) {
        this();
        setParameterValue(DirectoryOfDataset, directory);
    }

    @Override
    public synchronized RatingsDataset<Rating> getRatingsDataset() throws CannotLoadRatingsDataset {
        if (ratingsDataset == null) {
            getUsersDataset();
            getContentDataset();

            try {
                List<Rating> ratings = new LinkedList<>();

                File ratingsFile = getRatingsDatasetFile();
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
                        User user;
                        try {
                            user = usersDataset.getUser(idUser);
                        } catch (UserNotFound ex) {
                            user = usersDataset.getUser(idUser);
                            ERROR_CODES.USER_NOT_FOUND.exit(ex);
                            throw new IllegalStateException("User not found");
                        }
                        int idItem = Integer.parseInt(campos[1]);

                        Item item;
                        try {
                            item = contentDataset.getItem(idItem);
                        } catch (ItemNotFound ex) {
                            item = contentDataset.getItem(idItem);
                            ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
                            throw new IllegalStateException("Item not found");
                        }
                        byte ratingValue = Byte.parseByte(campos[2]);
                        long timestamp = Long.parseLong(campos[3]);

                        Rating rating = new RatingWithTimestamp(user, item, ratingValue, timestamp);
                        ratings.add(rating);
                    }
                }

                ratingsDataset = new BothIndexRatingsDataset(ratings);
            } catch (IOException | NumberFormatException ex) {
                throw new CannotLoadRatingsDataset(ex);
            }
        }
        return ratingsDataset;
    }

    @Override
    public synchronized ContentDataset getContentDataset() throws CannotLoadContentDataset {
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
                        String emptyField = fields[3];
                        String imdbUrl = fields[4];

                        if (!"".equals(emptyField)) {
                            throw new IllegalStateException("Field #4 in movieLens 100k is usually empty, but in item '" + idItem + "' is '" + emptyField + "'.");
                        }

                        Map<Feature, Object> features = new TreeMap<>();

                        addGenres(indexInicialGeneros, fields, generos_byIndex, featureGenerator, features);
                        addYear(featureGenerator, date, idItem, itemName, features);
                        addIMDBLink(featureGenerator, imdbUrl, idItem, itemName, features);

                        Item item = new Item(
                                idItem,
                                itemName,
                                features.keySet().toArray(new Feature[0]),
                                features.values().toArray());

                        boolean added = items.add(item);
                        if (!added) {
                            throw new IllegalStateException("The item '" + item + "' was already added!.");
                        }

                    }
                }

                contentDataset = new ContentDatasetDefault(items);
            } catch (IOException | NumberFormatException ex) {
                throw new CannotLoadContentDataset(ex);
            }
        }
        return contentDataset;
    }

    public void addGenres(final int indexInicialGeneros, String[] fields, List<String> generos_byIndex, FeatureGenerator featureGenerator, Map<Feature, Object> features) {
        for (int indexGenero = indexInicialGeneros; indexGenero < fields.length; indexGenero++) {
            String featureName = generos_byIndex.get(indexGenero - indexInicialGeneros);
            String featureValue = fields[indexGenero];

            if (!featureGenerator.containsFeature(featureName)) {
                featureGenerator.createFeature(featureName, FeatureType.Nominal);
            }
            Feature thisGenreFeature = featureGenerator.searchFeature(featureName);
            features.put(thisGenreFeature, featureValue);
        }
    }

    @Override
    public synchronized UsersDataset getUsersDataset() throws CannotLoadUsersDataset {

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
                    Logger.getLogger(MovieLens100k.class.getName()).log(Level.SEVERE, null, ex);
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
                    Logger.getLogger(MovieLens100k.class.getName()).log(Level.SEVERE, null, ex);
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

    private File getRatingsDatasetFile() {
        String filename = ((File) getParameterValue(DirectoryOfDataset)).getAbsoluteFile() + File.separator + "u.data";
        return new File(filename);
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

    public void addYear(FeatureGenerator featureGenerator, String rawDate, int idItem, String itemName, Map<Feature, Object> features) throws IllegalStateException {

        if ("".equals(rawDate)) {
            return;
        }

        Feature yearFeature;
        if (!featureGenerator.containsFeature("year")) {
            yearFeature = featureGenerator.createFeature("year", FeatureType.Numerical);
        } else {
            yearFeature = featureGenerator.searchFeature("year");
        }

        Feature completeDateFeature;
        if (!featureGenerator.containsFeature("date")) {
            completeDateFeature = featureGenerator.createFeature("date", FeatureType.Nominal);
        } else {
            completeDateFeature = featureGenerator.searchFeature("date");
        }

        DateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);

        String completeDate;
        Integer year;
        try {
            Date dateParsed = df.parse(rawDate);

            year = Integer.parseInt(new SimpleDateFormat("yyyy", Locale.ENGLISH).format(dateParsed));

            completeDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(dateParsed);

        } catch (ParseException ex) {
            throw new IllegalStateException("Cannot parse date of item " + idItem + " " + itemName + ": '" + rawDate + "'");
        }

        features.put(yearFeature, year);
        features.put(completeDateFeature, completeDate);
    }

    private void addIMDBLink(FeatureGenerator featureGenerator, String imdbUrl, int idItem, String itemName, Map<Feature, Object> features) {
        if ("".equals(imdbUrl)) {
            return;
        }

        Feature imdbURLFeature;

        final String imdbURLFeatureName = "imdbURL";
        if (!featureGenerator.containsFeature(imdbURLFeatureName)) {
            imdbURLFeature = featureGenerator.createFeature(imdbURLFeatureName, FeatureType.Nominal);
        } else {
            imdbURLFeature = featureGenerator.searchFeature(imdbURLFeatureName);
        }

        features.put(imdbURLFeature, imdbUrl);
    }
}
