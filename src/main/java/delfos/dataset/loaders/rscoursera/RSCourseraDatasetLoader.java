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

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.filefilters.FileFilterByExtension;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.FileParameter;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.loader.types.TagsDatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.tags.TagsDataset;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.io.csv.dataset.item.ContentDatasetToCSV;
import delfos.io.csv.dataset.rating.RatingsDatasetToCSV;
import delfos.io.csv.dataset.user.UsersDatasetToCSV;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Construye el RatingsDataset<? extends Rating>y ContentDataset a partir de dos archivos CSV, uno para cada dataset
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.1 29-01-2013
 * @version 1.0 Unknown date
 */
public class RSCourseraDatasetLoader extends DatasetLoaderAbstract<Rating> implements TagsDatasetLoader {

    private static final long serialVersionUID = -33875169931948L;

    public static final String DEFAULT_DIRECTORY = "temp" + File.separator + "rs-coursera" + File.separator;

    public final static Parameter RATINGS_FILE = new Parameter(
            "Ratings_file",
            new FileParameter(new File(DEFAULT_DIRECTORY + "ratings.csv"), new FileFilterByExtension("csv")));

    public final static Parameter CONTENT_FILE = new Parameter(
            "Content_file",
            new FileParameter(new File(DEFAULT_DIRECTORY + "movie_titles.csv"), new FileFilterByExtension("csv")));

    public final static Parameter USERS_FILE = new Parameter(
            "Users_file",
            new FileParameter(new File(DEFAULT_DIRECTORY + "users.csv"), new FileFilterByExtension("csv")));

    public final static Parameter TAGS_FILE = new Parameter(
            "Tags_file",
            new FileParameter(new File(DEFAULT_DIRECTORY + "movie_tags.csv"), new FileFilterByExtension("csv")));

    private RatingsDataset<Rating> ratingsDataset;
    private ContentDataset contentDataset;
    private UsersDataset usersDataset;
    private TagsDataset tagsDataset;

    public RSCourseraDatasetLoader() {
        addParameter(RATINGS_FILE);
        addParameter(CONTENT_FILE);
        addParameter(USERS_FILE);
        addParameter(TAGS_FILE);

        addParammeterListener(() -> {
            ratingsDataset = null;
            contentDataset = null;
            usersDataset = null;
            tagsDataset = null;
        });
    }

    @Override
    public RatingsDataset<Rating> getRatingsDataset() throws CannotLoadRatingsDataset {
        if (ratingsDataset == null) {
            try {
                RatingsDatasetToCSV ratingsDatasetToCSV = new RSCourseraRatingsDatasetToCSV();

                getUsersDataset();
                getContentDataset();

                List<Rating> ratings = ratingsDatasetToCSV.readRatingsDataset(getRatingsDatasetFile())
                        .stream().map(rating -> {
                            User user = usersDataset.get(rating.getIdUser());
                            Item item = contentDataset.get(rating.getIdItem());
                            Number ratingValue = rating.getRatingValue();

                            return new Rating(user, item, ratingValue);
                        })
                        .collect(Collectors.toList());

                ratingsDataset = new BothIndexRatingsDataset(ratings);
            } catch (FileNotFoundException ex) {
                throw new CannotLoadRatingsDataset(ex);
            }
        }
        return ratingsDataset;
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {

        File contentCSV = (File) getParameterValue(CONTENT_FILE);

        if (contentDataset == null) {
            ContentDatasetToCSV contentDatasetToCSV = new RSCourseraContentDatasetToCSV();
            try {
                contentDataset = contentDatasetToCSV.readContentDataset(contentCSV);
            } catch (FileNotFoundException ex) {
                throw new CannotLoadContentDataset(ex);
            }
        }
        return contentDataset;
    }

    @Override
    public synchronized UsersDataset getUsersDataset() throws CannotLoadUsersDataset {

        if (usersDataset == null) {
            UsersDatasetToCSV usersDatasetToCSV = new RSCourseraUsersDatasetToCSV();
            try {
                usersDataset = usersDatasetToCSV.readUsersDataset(getUsersDatasetFile().getAbsoluteFile());
            } catch (FileNotFoundException ex) {
                throw new CannotLoadUsersDataset(ex);
            }
        }
        return usersDataset;
    }

    @Override
    public TagsDataset getTagsDataset() {

        if (tagsDataset == null) {
            RSCourseraTagsDatasetLoaderFromCSV csvReader = new RSCourseraTagsDatasetLoaderFromCSV();
            try {
                tagsDataset = csvReader.readTagsDataset(getContentDataset(), getTagsDatasetFile());
            } catch (FileNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
        }

        return tagsDataset;

    }

    @Override
    public RelevanceCriteria getDefaultRelevanceCriteria() {
        return new RelevanceCriteria(3.5);
    }

    /**
     * Devuelve el nombre del archivo en que se almacena el dataset de usuarios.
     *
     * @return
     */
    public File getUsersDatasetFile() {
        return (File) getParameterValue(USERS_FILE);
    }

    /**
     * Devuelve el nombre del archivo en que se almacena el dataset de valoraciones.
     *
     * @return
     */
    public File getRatingsDatasetFile() {
        return (File) getParameterValue(RATINGS_FILE);
    }

    /**
     * Devuelve el nombre del archivo en que se almacena el dataset de contenido de los productos.
     *
     * @return
     */
    public File getContentDatasetFile() {
        return (File) getParameterValue(CONTENT_FILE);
    }

    /**
     * Devuelve el nombre del archivo en que se almacena el dataset de contenido de los productos.
     *
     * @return
     */
    public File getTagsDatasetFile() {
        return (File) getParameterValue(TAGS_FILE);
    }

    public void setRatingsDatasetFile(File file) {
        setParameterValue(RATINGS_FILE, file);
    }

    public void setContentDatasetFile(File file) {
        setParameterValue(CONTENT_FILE, file);
    }

    public void setUsersDatasetFile(File file) {
        setParameterValue(USERS_FILE, file);
    }

    public void setTagsDatasetFile(File file) {
        setParameterValue(TAGS_FILE, file);
    }
}
