package delfos.dataset.loaders.csv.changeable;

import java.io.File;
import java.util.LinkedList;
import java.util.TreeSet;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.User;
import delfos.dataset.changeable.ChangeableContentDataset;
import delfos.dataset.changeable.ChangeableDatasetLoaderAbstract;
import delfos.dataset.changeable.ChangeableRatingsDataset;
import delfos.dataset.changeable.ChangeableUsersDataset;
import delfos.dataset.loaders.csv.CSVfileDatasetLoader;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.items.ItemAlreadyExists;
import delfos.common.exceptions.dataset.users.UserAlreadyExists;
import delfos.common.filefilters.FileFilterByExtension;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterListener;
import delfos.common.parameters.restriction.FileParameter;

/**
 * Dataset almacenado en CSV que permite la modificación de sus datos.
 *
* @author Jorge Castro Gallardo
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
        addParammeterListener(new ParameterListener() {
            @Override
            public void parameterChanged() {
                rd = null;
                cd = null;
            }
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
        } else {
            if (parameterValue instanceof File) {
                return (File) parameterValue;
            } else {
                throw new IllegalArgumentException("The parameter type is not correct (" + parameterValue + ").");
            }
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
        } else {
            if (parameterValue instanceof File) {
                return (File) parameterValue;
            } else {
                throw new IllegalArgumentException("The parameter type is not correct (" + parameterValue + ").");
            }
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
        } else {
            if (parameterValue instanceof File) {
                return (File) parameterValue;
            } else {
                throw new IllegalArgumentException("The parameter type is not correct (" + parameterValue + ").");
            }
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
            try {
                this.cd = new ChangeableContentDatasetCSV(this, staticDatasetLoader.getContentDataset());
            } catch (ItemAlreadyExists ex) {
                throw new CannotLoadContentDataset("Cannot load items dataset, there are repeated items: " + ex.getIdItem() + "");
            }

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
            try {
                this.ud = new ChangeableUsersDatasetCSV(this, staticDatasetLoader.getUsersDataset());
            } catch (UserAlreadyExists ex) {
                throw new CannotLoadUsersDataset("Cannot load users dataset, there are repeated idUser: " + ex.getIdUser() + "");
            }

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
            rd = new ChangeableRatingsDatasetCSV(this, new TreeSet<Rating>());
        }

        try {
            cd = getChangeableContentDataset();
        } catch (CannotLoadContentDataset ex) {
            try {
                cd = new ChangeableContentDatasetCSV(this, new LinkedList<Item>());
            } catch (ItemAlreadyExists ex1) {
                //This situation never happens
                ERROR_CODES.UNDEFINED_ERROR.exit(ex1);
                throw new IllegalArgumentException(ex1);
            }
        }

        try {
            ud = getChangeableUsersDataset();
        } catch (CannotLoadUsersDataset ex) {
            try {
                ud = new ChangeableUsersDatasetCSV(this, new LinkedList<User>());
            } catch (UserAlreadyExists ex1) {
                //This situation never happens
                ERROR_CODES.UNDEFINED_ERROR.exit(ex1);
                throw new IllegalArgumentException(ex1);
            }
        }
    }
}
