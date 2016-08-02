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
package delfos.dataset.loaders.bookcrossing;

import com.csvreader.CsvReader;
import static com.csvreader.CsvReader.ESCAPE_MODE_BACKSLASH;
import delfos.common.Chronometer;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.DirectoryParameter;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureGenerator;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.basic.user.UsersDatasetAdapter;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 10-dic-2013
 */
public class BookCrossingDatasetLoader extends DatasetLoaderAbstract<Rating> {

    public static final Parameter BOOKCROSSING_DATASET_DIRECTORY;

    private RatingsDataset<Rating> ratingsDataset = null;
    private ContentDataset contentDataset = null;
    private UsersDataset usersDataset = null;

    static {
        File bookcrossingDatasetDirectory = new File("." + File.separator + "datasets" + File.separator + "bookcrossing" + File.separator);
        BOOKCROSSING_DATASET_DIRECTORY = new Parameter("BOOKCROSSING_DATASET_DIRECTORY", new DirectoryParameter(bookcrossingDatasetDirectory));
    }

    public BookCrossingDatasetLoader() {
        addParameter(BOOKCROSSING_DATASET_DIRECTORY);

        addParammeterListener(() -> {
            usersDataset = null;
            ratingsDataset = null;
            contentDataset = null;
        });
    }

    @Override
    public RatingsDataset<Rating> getRatingsDataset() throws CannotLoadRatingsDataset {
        lazyLoad();

        return ratingsDataset;
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {
        lazyLoad();

        return contentDataset;
    }

    @Override
    public UsersDataset getUsersDataset() throws CannotLoadUsersDataset {
        lazyLoad();

        return usersDataset;
    }

    public BookCrossingDatasetLoader setDatasetDirectory(File datasetDirectory) {
        setParameterValue(BOOKCROSSING_DATASET_DIRECTORY, datasetDirectory);
        return this;
    }

    private File getDatasetDirectory() {
        return (File) getParameterValue(BOOKCROSSING_DATASET_DIRECTORY);
    }

    private synchronized void lazyLoad() {

        if (usersDataset != null && contentDataset != null && ratingsDataset != null) {
            return;
        }

        File datasetDirectory = getDatasetDirectory();

        File usersFile = new File(datasetDirectory.getPath() + File.separator + "BX-Users.csv");
        File itemsFile = new File(datasetDirectory.getPath() + File.separator + "BX-Books.csv");
        File ratingsFile = new File(datasetDirectory.getPath() + File.separator + "BX-Book-Ratings.csv");

        try {
            usersDataset = loadUsers(usersFile);
        } catch (IOException ex) {
            throw new CannotLoadUsersDataset(ex);
        }
        try {
            contentDataset = loadItems(itemsFile);
        } catch (IOException ex) {
            throw new CannotLoadContentDataset(
                    ex.getMessage() + "[File '" + ratingsFile.getAbsolutePath() + "']",
                    ex
            );
        }
        try {
            ratingsDataset = loadRatings(ratingsFile);
        } catch (IOException ex) {
            throw new CannotLoadRatingsDataset(
                    ex.getMessage() + "[File '" + ratingsFile.getAbsolutePath() + "']",
                    ex
            );
        }

    }

    private UsersDataset loadUsers(File usersFile) throws FileNotFoundException, IOException {

        Set<User> users = new HashSet<>();

        CsvReader reader = new CsvReader(
                new FileInputStream(usersFile),
                Charset.forName("UTF-8"));
        Global.showInfoMessage("Loading CSV file " + usersFile.getPath() + "\n");
        reader.setDelimiter(';');
        reader.setEscapeMode(ESCAPE_MODE_BACKSLASH);
        reader.setTextQualifier('"');

        reader.readHeaders();
        checkUsersHeaders(reader.getHeaders());
        int i = 0;

        FeatureGenerator featureGenerator = new FeatureGenerator();
        Feature ageFeature = featureGenerator.createFeatureByExtendedName("age_numerical");
        Feature locationFeature = featureGenerator.createFeatureByExtendedName("location_nominal");

        Feature[] features = {locationFeature, ageFeature};

        while (reader.readRecord()) {
            int userId = new Integer(reader.get("User-ID"));

            String location = reader.get("Location").equals("NULL") ? null : reader.get("Location");
            Integer age = reader.get("Age").equals("NULL") ? null : new Integer(reader.get("Age"));

            Object[] values = {location, age};

            User user = new User(userId, "User_" + userId, features, values);
            users.add(user);

            i++;
        }
        reader.close();

        return new UsersDatasetAdapter(users);

    }

    private ContentDataset loadItems(File itemsFile) throws FileNotFoundException, IOException {
        Set<Item> items = new HashSet<>();

        CsvReader reader = new CsvReader(
                new FileInputStream(itemsFile),
                Charset.forName("UTF-8"));
        Global.showInfoMessage("Loading CSV file" + itemsFile.getPath() + "\n");
        reader.setDelimiter(';');
        reader.setEscapeMode(ESCAPE_MODE_BACKSLASH);
        reader.setTextQualifier('"');

        reader.readHeaders();
        checkItemsHeaders(reader.getHeaders());

        Chronometer c = new Chronometer();
        c.reset();
        int i = 0;

        FeatureGenerator featureGenerator = new FeatureGenerator();

        Feature isbnFeature = featureGenerator.createFeatureByExtendedName("isbn_nominal");
        Feature authorFeature = featureGenerator.createFeatureByExtendedName("author_nominal");
        Feature yearFeature = featureGenerator.createFeatureByExtendedName("year_nominal");
        Feature publisherFeature = featureGenerator.createFeatureByExtendedName("publisher_nominal");

        Feature[] features = {isbnFeature, authorFeature, yearFeature, publisherFeature};

        while (reader.readRecord()) {

            try {
                int idItem = i + 1;

                String isbn = reader.get("ISBN");
                String bookTitle = reader.get("Book-Title");

                String bookAuthor = reader.get("Book-Author");
                int yearOfPublication = new Integer(reader.get("Year-Of-Publication"));
                String publisher = reader.get("Publisher");

                String imageSmall = reader.get("Image-URL-S");
                String imageMedium = reader.get("Image-URL-M");
                String imageLarge = reader.get("Image-URL-L");

                Object[] values = {isbn, bookAuthor, yearOfPublication, publisher};

                Item item = new Item(idItem, bookTitle + "(" + isbn + ")", features, values);
                items.add(item);

            } catch (IOException | NumberFormatException ex) {
                Global.showWarning("Error in line " + (i + 2));

                throw ex;
            }

            i++;
        }
        reader.close();

        return new ContentDatasetDefault(items);
    }

    private RatingsDataset<Rating> loadRatings(File ratingsFile) throws FileNotFoundException, IOException {
        List<Rating> ratings = new ArrayList<>();

        CsvReader reader = new CsvReader(
                new FileInputStream(ratingsFile),
                Charset.forName("UTF-8"));
        Global.showInfoMessage("Loading CSV file" + ratingsFile.getPath() + "\n");
        reader.setDelimiter(';');
        reader.setEscapeMode(ESCAPE_MODE_BACKSLASH);
        reader.setTextQualifier('"');

        reader.readHeaders();
        checkRatingsHeaders(reader.getHeaders());

        Chronometer c = new Chronometer();
        c.reset();
        int i = 0;

        Map<Integer, User> usersIndex = usersDataset.stream().collect(Collectors.toMap(
                user -> user.getId(),
                user -> user));

        Feature isbnFeature = Arrays.stream(contentDataset.getFeatures())
                .filter(feature -> feature.getName().equals("isbn"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("ISBN feature not found"));

        Map<String, Item> itemsIndex = contentDataset.stream().collect(Collectors.toMap(
                item -> item.getFeatureValue(isbnFeature).toString(),
                user -> user));

        Map<String, Item> additionalItems = new HashMap<>();
        AtomicInteger nextIdItem = new AtomicInteger(contentDataset.allIDs().stream().mapToInt(idItem -> idItem).max().getAsInt() + 1);
        Feature[] isbnFeatures = {isbnFeature};

        while (reader.readRecord()) {

            String isbn = reader.get("ISBN");
            int idUser = new Integer(reader.get("User-ID"));

            int ratingValue = new Integer(reader.get("Book-Rating"));

            User user = usersIndex.get(idUser);
            Item item = itemsIndex.get(isbn);

            if (user == null) {
                throw new UserNotFound(idUser, "User " + idUser + "' not found, line " + (i + 2));
            }

            if (item == null) {
                item = additionalItems.get(isbn);
            }

            if (item == null) {
                Object[] featureValues = {isbn};
                item = new Item(nextIdItem.incrementAndGet(), isbn, isbnFeatures, featureValues);

                additionalItems.put(isbn, item);
            }

            Rating rating = new Rating(user, item, ratingValue);

            ratings.add(rating);

            i++;
        }
        reader.close();

        if (!additionalItems.isEmpty()) {
            contentDataset = addNotFoundItemsToContentDataset(isbnFeature, additionalItems.values());

            Global.showWarning("Ratings recovered through creation of empty items: " + ratings.parallelStream()
                    .filter(rating -> additionalItems.containsKey(rating.getItem().getFeatureValue(isbnFeature).toString()))
                    .count());
        }

        return new BothIndexRatingsDataset<>(ratings);
    }

    public ContentDataset addNotFoundItemsToContentDataset(Feature isbnFeature, Collection<Item> additionalItems) {

        Set<Item> allItems = contentDataset.stream().collect(Collectors.toCollection(HashSet::new));

        allItems.addAll(additionalItems);

        ContentDatasetDefault contentDatasetDefault = new ContentDatasetDefault(allItems);

        Global.showWarning("Items defined in the content file:     " + contentDataset.stream().count());
        Global.showWarning("Items NOT defined in the content file: " + additionalItems.stream().count());
        Global.showWarning("Final number of items:                 " + contentDatasetDefault.stream().count());

        return contentDatasetDefault;
    }

    private void checkUsersHeaders(String[] headers) {
        checkHeaderPresent(headers, "User-ID");
        checkHeaderPresent(headers, "Location");
        checkHeaderPresent(headers, "Age");

    }

    private void checkItemsHeaders(String[] headers) {

        checkHeaderPresent(headers, "ISBN");
        checkHeaderPresent(headers, "Book-Title");
        checkHeaderPresent(headers, "Book-Author");
        checkHeaderPresent(headers, "Year-Of-Publication");
        checkHeaderPresent(headers, "Publisher");
        checkHeaderPresent(headers, "Image-URL-S");
        checkHeaderPresent(headers, "Image-URL-M");
        checkHeaderPresent(headers, "Image-URL-L");
    }

    private void checkRatingsHeaders(String[] headers) {
        checkHeaderPresent(headers, "User-ID");
        checkHeaderPresent(headers, "ISBN");
        checkHeaderPresent(headers, "Book-Rating");
    }

    private void checkHeaderPresent(String[] headers, String headerThatMustBePresent) {
        boolean isHeaderPresent = false;
        for (String header : headers) {
            if (header.equals(headerThatMustBePresent)) {
                isHeaderPresent = true;
            }
        }

        if (!isHeaderPresent) {
            throw new CannotLoadRatingsDataset("Header '" + headerThatMustBePresent + "' is not present");
        }
    }

}
