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
package delfos.io.csv.dataset;

import delfos.ERROR_CODES;
import delfos.common.FileUtilities;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.CannotSaveUsersDataset;
import delfos.common.filefilters.FileFilterByExtension;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.FileParameter;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.io.csv.dataset.item.ContentDatasetToCSV;
import delfos.io.csv.dataset.item.DefaultContentDatasetToCSV;
import delfos.io.csv.dataset.rating.RatingsDatasetToCSV;
import delfos.io.csv.dataset.rating.RatingsDatasetToCSV_JavaCSV20;
import delfos.io.csv.dataset.user.DefaultUsersDatasetToCSV;
import delfos.io.csv.dataset.user.UsersDatasetToCSV;
import delfos.io.types.DatasetSaver;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

/**
 * Clase que almacena datasets de valoraciones y de contenido en un archivo de
 * valores separados por comas (CSV)
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class DatasetToCSV extends DatasetSaver {

    private static final long serialVersionUID = 1L;
    private static final String[] extensions = {"csv"};
    public static final Parameter RATINGS_FILE = new Parameter("Ratings_file", new FileParameter(new File("ratings.csv"), new FileFilterByExtension(extensions)));
    public static final Parameter CONTENT_FILE = new Parameter("Content_file", new FileParameter(new File("content.csv"), new FileFilterByExtension(extensions)));
    public static final Parameter USERS_FILE = new Parameter("Users_file", new FileParameter(new File("users.csv"), new FileFilterByExtension(extensions)));

    public DatasetToCSV() {
        addParameter(RATINGS_FILE);
        addParameter(CONTENT_FILE);
        addParameter(USERS_FILE);
    }

    public DatasetToCSV(File ratingsFile, File contentFile, File usersFile) {
        this();

        setParameterValue(RATINGS_FILE, ratingsFile);
        setParameterValue(CONTENT_FILE, contentFile);
        setParameterValue(USERS_FILE, usersFile);
    }

    @Override
    public void saveRatingsDataset(RatingsDataset<? extends Rating> rd) {

        RatingsDatasetToCSV ratingsDatasetToCSV = new RatingsDatasetToCSV_JavaCSV20();

        createParentDirectoryIfNotExist(getRATINGS_FILE());

        try {
            ratingsDatasetToCSV.writeDataset(rd, getRATINGS_FILE().getPath());
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_RATINGS_DATASET.exit(ex);
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void saveContentDataset(ContentDataset cd) {

        ContentDatasetToCSV contentDatasetToCSV = new DefaultContentDatasetToCSV();

        createParentDirectoryIfNotExist(getCONTENT_FILE());
        try {
            contentDatasetToCSV.writeDataset(cd, getCONTENT_FILE().getAbsolutePath());
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_CONTENT_DATASET.exit(ex);
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void saveUsersDataset(UsersDataset usersDataset) throws CannotSaveUsersDataset {
        createParentDirectoryIfNotExist(getUSERS_FILE());

        UsersDatasetToCSV usersDatasetToCSV = new DefaultUsersDatasetToCSV();
        try {
            usersDatasetToCSV.writeDataset(usersDataset, getUSERS_FILE().getAbsolutePath());
        } catch (IOException ex) {
            throw new CannotSaveUsersDataset(ex);
        }
    }

    @Override
    public RatingsDataset<? extends Rating> loadRatingsDataset() throws CannotLoadRatingsDataset, FileNotFoundException {
        RatingsDatasetToCSV ratingsDatasetToCSV = new RatingsDatasetToCSV_JavaCSV20();
        Collection<Rating> ratings = ratingsDatasetToCSV.readRatingsDataset(getRATINGS_FILE());
        return new BothIndexRatingsDataset(ratings);
    }

    @Override
    public ContentDataset loadContentDataset() throws CannotLoadContentDataset, FileNotFoundException {
        ContentDatasetToCSV contentDatasetToCSV = new DefaultContentDatasetToCSV();
        return contentDatasetToCSV.readContentDataset(getCONTENT_FILE());
    }

    @Override
    public UsersDataset loadUsersDataset() throws CannotLoadUsersDataset, FileNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public File getRATINGS_FILE() {
        return (File) getParameterValue(RATINGS_FILE);
    }

    public File getCONTENT_FILE() {
        return (File) getParameterValue(CONTENT_FILE);
    }

    public File getUSERS_FILE() {
        return (File) getParameterValue(USERS_FILE);
    }

    private void createParentDirectoryIfNotExist(File file) {
        FileUtilities.createDirectoriesForFile(file);
    }

}
