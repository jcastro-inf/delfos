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
package delfos.dataset.loaders.csv.changeable;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.filefilters.FileFilterByExtension;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.FileParameter;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.changeable.ChangeableContentDataset;
import delfos.dataset.changeable.ChangeableDatasetLoaderAbstract;
import delfos.dataset.changeable.ChangeableRatingsDataset;
import delfos.dataset.changeable.ChangeableUsersDataset;
import delfos.dataset.loaders.csv.CSVfileDatasetLoader;
import java.io.File;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Dataset almacenado en CSV que permite la modificación de sus datos.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 16-sep-2013
 */
public class ChangeableCSVFileDatasetLoader extends ChangeableDatasetLoaderAbstract {

    private static final long serialVersionUID = 1L;
    public final static Parameter RATINGS_FILE = new Parameter("Ratings_file", new FileParameter(new File(".." + File.separator + ".." + File.separator + "datasets" + File.separator + "" + "ratings.csv"), new FileFilterByExtension("csv")));
    public final static Parameter CONTENT_FILE = new Parameter("Content_file", new FileParameter(new File(".." + File.separator + ".." + File.separator + "datasets" + File.separator + "" + "content.csv"), new FileFilterByExtension("csv")));
    public final static Parameter USERS_FILE = new Parameter("Users_file", new FileParameter(new File(".." + File.separator + ".." + File.separator + "datasets" + File.separator + "" + "users.csv"), new FileFilterByExtension("csv")));
    private ChangeableRatingsDataset rd;
    private ChangeableContentDataset cd;
    private ChangeableUsersDataset ud;

    public ChangeableCSVFileDatasetLoader() {
        addParameter(RATINGS_FILE);
        addParameter(CONTENT_FILE);
        addParameter(USERS_FILE);
        addParammeterListener(() -> {
            rd = null;
            cd = null;
        });
    }

    public ChangeableCSVFileDatasetLoader(String ratingsFile, String contentFile, String usersFile) {
        this();
        setParameterValue(RATINGS_FILE, ratingsFile);
        setParameterValue(CONTENT_FILE, contentFile);
        setParameterValue(USERS_FILE, usersFile);
    }

    /**
     * Devuelve el nombre del archivo en que se almacena el dataset de usuarios.
     *
     * @return
     */
    public File getUsersDatasetFile() {
        Object parameterValue = getParameterValue(USERS_FILE);
        if (parameterValue instanceof String) {
            return new File((String) parameterValue);
        } else if (parameterValue instanceof File) {
            return (File) parameterValue;
        } else {
            throw new IllegalArgumentException("The parameter type is not correct (" + parameterValue + ").");
        }
    }

    /**
     * Devuelve el nombre del archivo en que se almacena el dataset de usuarios.
     *
     * @return
     */
    public File getContentDatasetFile() {
        Object parameterValue = getParameterValue(CONTENT_FILE);
        if (parameterValue instanceof String) {
            return new File((String) parameterValue);
        } else if (parameterValue instanceof File) {
            return (File) parameterValue;
        } else {
            throw new IllegalArgumentException("The parameter type is not correct (" + parameterValue + ").");
        }
    }

    /**
     * Devuelve el nombre del archivo en que se almacena el dataset de usuarios.
     *
     * @return
     */
    public File getRatingsDatasetFile() {
        Object parameterValue = getParameterValue(RATINGS_FILE);
        if (parameterValue instanceof String) {
            return new File((String) parameterValue);
        } else if (parameterValue instanceof File) {
            return (File) parameterValue;
        } else {
            throw new IllegalArgumentException("The parameter type is not correct (" + parameterValue + ").");
        }
    }

    @Override
    public ChangeableRatingsDataset getChangeableRatingsDataset() throws CannotLoadRatingsDataset {
        if (rd == null) {

            CSVfileDatasetLoader staticDatasetLoader = new CSVfileDatasetLoader(
                    getRatingsDatasetFile().getAbsolutePath(),
                    getContentDatasetFile().getAbsolutePath(),
                    getUsersDatasetFile().getAbsolutePath());
            this.rd = new ChangeableRatingsDatasetCSV(this, staticDatasetLoader.getRatingsDataset());

        }
        return rd;
    }

    @Override
    public ChangeableContentDataset getChangeableContentDataset() throws CannotLoadContentDataset {
        if (cd == null) {

            CSVfileDatasetLoader staticDatasetLoader = new CSVfileDatasetLoader(
                    getRatingsDatasetFile().getAbsolutePath(),
                    getContentDatasetFile().getAbsolutePath(),
                    getUsersDatasetFile().getAbsolutePath());

            this.cd = new ChangeableContentDatasetCSV(this, staticDatasetLoader
                    .getContentDataset().stream()
                    .collect(Collectors.toSet()));

        }
        return cd;
    }

    @Override
    public ChangeableUsersDataset getChangeableUsersDataset() throws CannotLoadUsersDataset {
        if (ud == null) {

            CSVfileDatasetLoader staticDatasetLoader = new CSVfileDatasetLoader(
                    getRatingsDatasetFile().getAbsolutePath(),
                    getContentDatasetFile().getAbsolutePath(),
                    getUsersDatasetFile().getAbsolutePath());

            this.ud = new ChangeableUsersDatasetCSV(this, staticDatasetLoader.getUsersDataset().stream().collect(Collectors.toSet()));

        }
        return ud;
    }

    @Override
    public RelevanceCriteria getDefaultRelevanceCriteria() {
        return new RelevanceCriteria();
    }

    @Override
    public void initStructures() {
        try {
            rd = getChangeableRatingsDataset();
        } catch (CannotLoadRatingsDataset ex) {
            rd = new ChangeableRatingsDatasetCSV(this, new TreeSet<>());
        }

        try {
            cd = getChangeableContentDataset();
        } catch (CannotLoadContentDataset ex) {
            cd = new ChangeableContentDatasetCSV(this, new TreeSet<>());
        }

        try {
            ud = getChangeableUsersDataset();
        } catch (CannotLoadUsersDataset ex) {
            ud = new ChangeableUsersDatasetCSV(this, new TreeSet<>());

        }
    }
}
