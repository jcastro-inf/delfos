package delfos.dataset.loaders.database.mysql;

import java.sql.SQLException;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.databaseconnections.MySQLConnection;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.io.database.mysql.dataset.ContentDatasetToMySQL;
import delfos.io.database.mysql.dataset.RatingDatasetToMySQL;

/**
 * Clase para recuperar los datasets almacenados mediante las clases
 * {@link RatingDatasetToMySQL} y {@link ContentDatasetToMySQL} y utilizarlos
 * para la recomendación.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 15-Mar-2013
 */
public class MySQLDatabaseDatasetLoader_Default extends MySQLDatabaseDatasetLoaderAbstract implements ContentDatasetLoader {

    private static final long serialVersionUID = 1L;
    private RatingsDataset<? extends Rating> ratingsDataset;
    private ContentDataset cd;

    public MySQLDatabaseDatasetLoader_Default() {
        super();
    }

    public MySQLDatabaseDatasetLoader_Default(String user, String pass, String databaseName, String hostName, int port, String tableNamePrefix) {
        super();

        setParameterValue(CONNECTION_CONFIGURATION_USER, user);
        setParameterValue(CONNECTION_CONFIGURATION_PASSWORD, pass);
        setParameterValue(CONNECTION_CONFIGURATION_DATABASE_NAME, databaseName);
        setParameterValue(CONNECTION_CONFIGURATION_HOST_NAME, hostName);
        setParameterValue(CONNECTION_CONFIGURATION_PORT, port);
        setParameterValue(CONNECTION_CONFIGURATION_PREFIX, tableNamePrefix);
    }

    @Override
    public RatingsDataset<? extends Rating> getRatingsDataset() throws CannotLoadRatingsDataset {
        if (ratingsDataset == null) {
            try {
                RatingDatasetToMySQL datasetToMySQL = new RatingDatasetToMySQL(getMySQLConnection());
                ratingsDataset = datasetToMySQL.readDataset();
            } catch (ClassNotFoundException ex) {
                ERROR_CODES.DEPENDENCY_NOT_FOUND.exit(ex);
            } catch (SQLException ex) {
                throw new CannotLoadRatingsDataset(ex);
            }
        }
        return ratingsDataset;
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {
        if (cd == null) {
            try {
                ContentDatasetToMySQL contentDatasetToMySQL = new ContentDatasetToMySQL(getMySQLConnection());
                cd = contentDatasetToMySQL.readDataset();
            } catch (ClassNotFoundException ex) {
                ERROR_CODES.DEPENDENCY_NOT_FOUND.exit(ex);
            } catch (SQLException ex) {
                throw new CannotLoadContentDataset(ex);
            }
        }
        return cd;
    }

    @Override
    public RelevanceCriteria getDefaultRelevanceCriteria() {
        return new RelevanceCriteria(4);
    }

    private MySQLConnection getMySQLConnection() throws ClassNotFoundException, SQLException {
        return new MySQLConnection(
                getUser(),
                getPass(),
                getDatasbaseName(),
                getHostName(),
                getPort(),
                getTableNamePrefix()
        );
    }

    private String getUser() {
        return (String) getParameterValue(CONNECTION_CONFIGURATION_USER);
    }

    private String getPass() {
        return (String) getParameterValue(CONNECTION_CONFIGURATION_PASSWORD);
    }

    private String getDatasbaseName() {
        return (String) getParameterValue(CONNECTION_CONFIGURATION_DATABASE_NAME);
    }

    private String getHostName() {
        return (String) getParameterValue(CONNECTION_CONFIGURATION_HOST_NAME);
    }

    private int getPort() {
        return (Integer) getParameterValue(CONNECTION_CONFIGURATION_PORT);
    }

    private String getTableNamePrefix() {
        return (String) getParameterValue(CONNECTION_CONFIGURATION_PREFIX);
    }
}
