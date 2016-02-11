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
package delfos.dataset.loaders.csv;

import com.csvreader.CsvReader;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.filefilters.FileFilterByExtension;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.FileParameter;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.loader.types.RatingsDatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
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
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 *
 * @version 14-oct-2014
 * @author Jorge Castro Gallardo
 */
public class CSVfileDatasetLoader_table extends DatasetLoaderAbstract<Rating> implements RatingsDatasetLoader<Rating>, ContentDatasetLoader, UsersDatasetLoader {

    public final static Parameter RATINGS_FILE = new Parameter("Ratings_file", new FileParameter(new File("ratingsTable"), new FileFilterByExtension(true, "")));

    private RatingsDataset<Rating> ratingsDataset;
    private ContentDataset contentDataset;
    private UsersDataset usersDataset;

    public CSVfileDatasetLoader_table() {

        super();
        addParameter(RATINGS_FILE);

        addParammeterListener(() -> {
            CSVfileDatasetLoader_table.this.ratingsDataset = null;
            CSVfileDatasetLoader_table.this.contentDataset = null;
            CSVfileDatasetLoader_table.this.usersDataset = null;
        });
    }

    public CSVfileDatasetLoader_table(String ratingsFile) {
        this();

        setParameterValue(RATINGS_FILE, new File(ratingsFile));
    }

    @Override
    public RatingsDataset<Rating> getRatingsDataset() throws CannotLoadRatingsDataset {
        if (ratingsDataset != null) {
            return ratingsDataset;
        }

        File ratingsFile = (File) getParameterValue(RATINGS_FILE);
        String ratingsCSV = ratingsFile.getAbsolutePath();
        Collection<Rating> ratings = new ArrayList<>();

        CsvReader reader;
        try {
            reader = new CsvReader(
                    new FileInputStream(ratingsCSV),
                    Charset.forName("UTF-8"));
        } catch (FileNotFoundException ex) {
            throw new CannotLoadRatingsDataset(ex);
        }

        reader.setDelimiter('\t');

        Map<Integer, Item> items = new TreeMap<>();
        Set<User> users = new TreeSet<>();

        try {
            int i = 1;

            reader.readRecord();

            //Leo los items.
            {
                String[] columnsNames = reader.getValues();

                for (int j = 1; j < columnsNames.length; j++) {
                    String itemName = columnsNames[j];

                    int idItem = Integer.parseInt(itemName.substring(itemName.indexOf("_") + 1));

                    items.put(j, new Item(idItem, itemName));
                }
            }

            i++;
            while (reader.readRecord()) {

                try {

                    String[] userData = reader.getValues();

                    String userName = userData[0];

                    int idUser = Integer.parseInt(userName.substring(userName.indexOf("_") + 1));

                    users.add(new User(idUser, userName));

                    for (int j = 1; j < userData.length; j++) {
                        String ratingFromFile = userData[j];

                        if (ratingFromFile.isEmpty()) {
                            continue;
                        }

                        int idItem = items.get(j).getId();
                        int ratingValue = Integer.parseInt(ratingFromFile);

                        ratings.add(new Rating(idUser, idItem, ratingValue));
                    }
                } catch (Throwable ex) {
                    Global.showError(ex);
                    Global.showWarning("Raw record  '" + reader.getRawRecord() + "' line " + i + "\n");
                }
                i++;

            }
        } catch (IOException ex) {
            throw new CannotLoadRatingsDataset(ex);
        }
        reader.close();

        ratingsDataset = new BothIndexRatingsDataset<>(ratings);
        contentDataset = new ContentDatasetDefault(items.values().stream().collect(Collectors.toSet()));
        usersDataset = new UsersDatasetAdapter(users);

        return ratingsDataset;
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {
        if (contentDataset == null) {
            getRatingsDataset();
        }

        return contentDataset;
    }

    @Override
    public UsersDataset getUsersDataset() throws CannotLoadUsersDataset {
        if (usersDataset == null) {
            getRatingsDataset();
        }

        return usersDataset;
    }

}
