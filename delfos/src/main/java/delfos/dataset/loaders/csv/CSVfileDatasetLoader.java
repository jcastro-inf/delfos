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

import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.filefilters.FileFilterByExtension;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.FileParameter;
import delfos.common.parameters.restriction.ObjectParameter;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.loader.types.RatingsDatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.basic.user.UsersDatasetAdapter;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.dataset.storage.memory.DefaultMemoryRatingsDataset_ItemIndexed;
import delfos.dataset.storage.memory.DefaultMemoryRatingsDataset_ItemIndexed_withMaps;
import delfos.dataset.storage.memory.DefaultMemoryRatingsDataset_UserIndexed;
import delfos.dataset.storage.memory.DefaultMemoryRatingsDataset_UserIndexed_withMaps;
import delfos.io.csv.dataset.item.ContentDatasetToCSV;
import delfos.io.csv.dataset.item.DefaultContentDatasetToCSV;
import delfos.io.csv.dataset.rating.RatingsDatasetToCSV;
import delfos.io.csv.dataset.rating.RatingsDatasetToCSV_JavaCSV20;
import delfos.io.csv.dataset.user.DefaultUsersDatasetToCSV;
import delfos.io.csv.dataset.user.UsersDatasetToCSV;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Construye el RatingsDataset<? extends Rating>y ContentDataset a partir de dos
 * archivos CSV, uno para cada dataset
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.1 29-01-2013
 * @version 1.0 Unknown date
 */
public class CSVfileDatasetLoader extends DatasetLoaderAbstract<Rating> implements RatingsDatasetLoader<Rating>, ContentDatasetLoader, UsersDatasetLoader {

    private static final long serialVersionUID = -3387516993124229948L;
    public static final String INDEX_NONE = "INDEX_NONE";
    public static final String INDEX_USERS = "INDEX_USERS";
    public static final String INDEX_ITEMS = "INDEX_ITEMS";
    public static final String INDEX_USERS_MAPS = "INDEX_USERS_MAPS";
    public static final String INDEX_ITEMS_MAPS = "INDEX_ITEMS_MAPS";
    public static final String INDEX_BOTH = "INDEX_BOTH";
    public final static Parameter RATINGS_FILE = new Parameter("Ratings_file", new FileParameter(new File(".." + File.separator + ".." + File.separator + "datasets" + File.separator + "" + "ratings.csv"), new FileFilterByExtension("csv")));
    public final static Parameter CONTENT_FILE = new Parameter("Content_file", new FileParameter(new File(".." + File.separator + ".." + File.separator + "datasets" + File.separator + "" + "content.csv"), new FileFilterByExtension("csv")));
    public final static Parameter USERS_FILE = new Parameter("Users_file", new FileParameter(new File(".." + File.separator + ".." + File.separator + "datasets" + File.separator + "" + "users.csv"), new FileFilterByExtension("csv")));
    public final static Parameter INDEXATION;

    static {
        String indexOptions[] = {INDEX_NONE, INDEX_USERS, INDEX_ITEMS, INDEX_USERS_MAPS, INDEX_ITEMS_MAPS, INDEX_BOTH};

        INDEXATION = new Parameter("INDEXATION", new ObjectParameter(indexOptions, INDEX_BOTH), "Establece la indexación que se usará en el dataset de valoraciones una vez cargado en memoria.");
    }
    private RatingsDataset<Rating> ratingsDataset;
    private ContentDataset contentDataset;
    private UsersDataset usersDataset;
    public int tamañoDataset = -1;

    public void setTamañoDataset(int tamañoDataset) {
        this.tamañoDataset = tamañoDataset;
    }

    public CSVfileDatasetLoader() {
        addParameter(RATINGS_FILE);
        addParameter(CONTENT_FILE);
        addParameter(USERS_FILE);
        addParameter(INDEXATION);
        addParammeterListener(() -> {
            ratingsDataset = null;
            contentDataset = null;
            usersDataset = null;
        });
    }

    /**
     * Constructor sin fichero de usuarios.
     *
     * @param ratingsFile
     * @param contentFile
     *
     * @deprecated Todos los datasets deben tener un fichero de usuarios.
     */
    @Deprecated
    public CSVfileDatasetLoader(String ratingsFile, String contentFile) {
        this();
        setParameterValue(RATINGS_FILE, ratingsFile);
        setParameterValue(CONTENT_FILE, contentFile);
    }

    public CSVfileDatasetLoader(String ratingsFile, String contentFile, String usersFile) {
        this();
        setParameterValue(RATINGS_FILE, ratingsFile);
        setParameterValue(CONTENT_FILE, contentFile);
        setParameterValue(USERS_FILE, usersFile);
    }

    public CSVfileDatasetLoader(String ratingsFile, String contentFile, String usersFile, String indexationMode) {
        this(ratingsFile, contentFile, usersFile);

        setParameterValue(INDEXATION, indexationMode);
    }

    @Override
    public RatingsDataset<Rating> getRatingsDataset() throws CannotLoadRatingsDataset {
        if (ratingsDataset == null) {
            try {
                RatingsDatasetToCSV ratingsDatasetToCSV = new RatingsDatasetToCSV_JavaCSV20();
                Collection<Rating> ratings = ratingsDatasetToCSV.readRatingsDataset(getRatingsDatasetFile());

                String indexationMode = getIndexationMode();
                if (indexationMode.equals(INDEX_NONE)) {
                    throw new CannotLoadRatingsDataset("Indexation method INDEX_NONE not supported yet.");
                }
                if (indexationMode.equals(INDEX_USERS)) {
                    ratingsDataset = new DefaultMemoryRatingsDataset_UserIndexed(ratings);
                }
                if (indexationMode.equals(INDEX_ITEMS)) {
                    ratingsDataset = new DefaultMemoryRatingsDataset_ItemIndexed(ratings);
                }
                if (indexationMode.equals(INDEX_BOTH)) {
                    ratingsDataset = new BothIndexRatingsDataset(ratings);
                }
                if (indexationMode.equals(INDEX_USERS_MAPS)) {
                    ratingsDataset = new DefaultMemoryRatingsDataset_UserIndexed_withMaps(ratings);
                }
                if (indexationMode.equals(INDEX_ITEMS_MAPS)) {
                    ratingsDataset = new DefaultMemoryRatingsDataset_ItemIndexed_withMaps(ratings);
                }
                if (ratingsDataset == null) {
                    throw new IllegalStateException("The indexation mode is unknown: " + indexationMode);
                }
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
            ContentDatasetToCSV contentDatasetToCSV = new DefaultContentDatasetToCSV();
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
            UsersDatasetToCSV usersDatasetToCSV = new DefaultUsersDatasetToCSV();

            try {
                usersDataset = usersDatasetToCSV.readUsersDataset(getUsersDatasetFile().getAbsoluteFile());
            } catch (CannotLoadUsersDataset | FileNotFoundException ex) {
                Global.showWarning("Fail at loading users CSV, generating usersDataset from ratingsDataset");
                try {
                    RatingsDataset<Rating> ratingsDataset1 = getRatingsDataset();

                    Set<User> users = ratingsDataset1
                            .allUsers().stream()
                            .map((idUser) -> new User(idUser))
                            .collect(Collectors.toSet());

                    usersDataset = new UsersDatasetAdapter(users);

                } catch (CannotLoadRatingsDataset | CannotLoadUsersDataset ex1) {
                    throw new CannotLoadUsersDataset(ex1);
                }
            }

        }
        return usersDataset;
    }

    @Override
    public RelevanceCriteria getDefaultRelevanceCriteria() {
        return new RelevanceCriteria(4);
    }

    private String getIndexationMode() {
        return (String) getParameterValue(INDEXATION);
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
     * Devuelve el nombre del archivo en que se almacena el dataset de
     * valoraciones.
     *
     * @return
     */
    public File getRatingsDatasetFile() {
        return (File) getParameterValue(RATINGS_FILE);
    }

    /**
     * Devuelve el nombre del archivo en que se almacena el dataset de contenido
     * de los productos.
     *
     * @return
     */
    public File getContentDatasetFile() {
        return (File) getParameterValue(CONTENT_FILE);
    }
}
