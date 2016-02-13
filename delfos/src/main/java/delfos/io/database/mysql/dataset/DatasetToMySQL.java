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
package delfos.io.database.mysql.dataset;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.databaseconnections.MySQLConnection;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.UsersDataset;
import static delfos.dataset.loaders.database.mysql.MySQLDatabaseDatasetLoader.CONNECTION_CONFIGURATION_DATABASE_NAME;
import static delfos.dataset.loaders.database.mysql.MySQLDatabaseDatasetLoader.CONNECTION_CONFIGURATION_HOST_NAME;
import static delfos.dataset.loaders.database.mysql.MySQLDatabaseDatasetLoader.CONNECTION_CONFIGURATION_PASSWORD;
import static delfos.dataset.loaders.database.mysql.MySQLDatabaseDatasetLoader.CONNECTION_CONFIGURATION_PORT;
import static delfos.dataset.loaders.database.mysql.MySQLDatabaseDatasetLoader.CONNECTION_CONFIGURATION_PREFIX;
import static delfos.dataset.loaders.database.mysql.MySQLDatabaseDatasetLoader.CONNECTION_CONFIGURATION_USER;
import delfos.dataset.loaders.database.mysql.changeable.ChangeableMySQLDatasetLoader;
import delfos.io.types.DatasetSaver;
import java.io.FileNotFoundException;
import java.sql.SQLException;

/**
 * Class to load and save datasets to a mysql database.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class DatasetToMySQL extends DatasetSaver {

    private static final long serialVersionUID = 1L;

    public DatasetToMySQL() {
        //Database configuration parameters
        addParameter(CONNECTION_CONFIGURATION_USER);
        addParameter(CONNECTION_CONFIGURATION_PASSWORD);
        addParameter(CONNECTION_CONFIGURATION_DATABASE_NAME);
        addParameter(CONNECTION_CONFIGURATION_HOST_NAME);
        addParameter(CONNECTION_CONFIGURATION_PORT);
        addParameter(CONNECTION_CONFIGURATION_PREFIX);
    }

    public DatasetToMySQL(MySQLConnection mySQLConnection) {
        this();

        setParameterValue(CONNECTION_CONFIGURATION_USER, mySQLConnection.getUser());
        setParameterValue(CONNECTION_CONFIGURATION_PASSWORD, mySQLConnection.getPass());
        setParameterValue(CONNECTION_CONFIGURATION_DATABASE_NAME, mySQLConnection.getDatabaseName());
        setParameterValue(CONNECTION_CONFIGURATION_HOST_NAME, mySQLConnection.getHostName());
        setParameterValue(CONNECTION_CONFIGURATION_PORT, mySQLConnection.getPort());
        setParameterValue(CONNECTION_CONFIGURATION_PREFIX, mySQLConnection.getPrefix());
    }

    @Override
    public void saveRatingsDataset(RatingsDataset<? extends Rating> ratingsDataset) {
        try {
            RatingsDatasetToMySQL ratingsDatasetToMySQL = new RatingsDatasetToMySQL(getMySQLConnectionDescription());
            ratingsDatasetToMySQL.writeDataset(ratingsDataset);
        } catch (SQLException ex) {
            ERROR_CODES.CANNOT_WRITE_RATINGS_DATASET.exit(ex);
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void saveContentDataset(ContentDataset contentDataset) {
        try {
            ContentDatasetToMySQL contentDatasetToMySQL = new ContentDatasetToMySQL(getMySQLConnectionDescription());
            contentDatasetToMySQL.writeDataset(contentDataset);
        } catch (SQLException ex) {
            ERROR_CODES.CANNOT_WRITE_CONTENT_DATASET.exit(ex);
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void saveUsersDataset(UsersDataset usersDataset) {

        try {
            UsersDatasetToMySQL usersDatasetToMySQL = new UsersDatasetToMySQL(getMySQLConnectionDescription());
            usersDatasetToMySQL.writeDataset(usersDataset);
        } catch (SQLException ex) {
            ERROR_CODES.CANNOT_WRITE_USERS_DATASET.exit(ex);
            throw new IllegalStateException(ex);
        }
    }

    public synchronized final MySQLConnection getMySQLConnectionDescription() throws SQLException {
        try {
            String _user = (String) getParameterValue(CONNECTION_CONFIGURATION_USER);
            String _pass = (String) getParameterValue(CONNECTION_CONFIGURATION_PASSWORD);
            String _databaseName = (String) getParameterValue(CONNECTION_CONFIGURATION_DATABASE_NAME);
            String _serverName = (String) getParameterValue(CONNECTION_CONFIGURATION_HOST_NAME);
            int _port = (Integer) getParameterValue(CONNECTION_CONFIGURATION_PORT);

            String _prefix = (String) getParameterValue(CONNECTION_CONFIGURATION_PREFIX);

            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage(ChangeableMySQLDatasetLoader.class + ": Making database connection with properties:\n");

                Global.showInfoMessage("\tUser -----------> " + _user + "\n");
                Global.showInfoMessage("\tPass -----------> " + _pass + "\n");
                Global.showInfoMessage("\tDatabase Name --> " + _databaseName + "\n");
                Global.showInfoMessage("\tServer Name ----> " + _serverName + "\n");
                Global.showInfoMessage("\tPort -----------> " + _port + "\n");
                Global.showInfoMessage("\tPrefix ---------> " + _prefix + "\n");
            }

            return new MySQLConnection(_user, _pass, _databaseName, _serverName, _port, _prefix);
        } catch (ClassNotFoundException ex) {
            ERROR_CODES.DEPENDENCY_NOT_FOUND.exit(ex);
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public ContentDataset loadContentDataset() throws CannotLoadContentDataset, FileNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RatingsDataset<? extends Rating> loadRatingsDataset() throws CannotLoadRatingsDataset, FileNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public UsersDataset loadUsersDataset() throws CannotLoadUsersDataset, FileNotFoundException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
