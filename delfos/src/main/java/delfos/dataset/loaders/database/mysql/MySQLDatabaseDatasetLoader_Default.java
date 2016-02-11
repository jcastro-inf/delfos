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
package delfos.dataset.loaders.database.mysql;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.databaseconnections.MySQLConnection;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.UsersDataset;
import delfos.io.database.mysql.dataset.ContentDatasetToMySQL;
import delfos.io.database.mysql.dataset.RatingsDatasetToMySQL;
import delfos.io.database.mysql.dataset.UsersDatasetToMySQL;
import java.sql.SQLException;

/**
 * Clase para recuperar los datasets almacenados mediante las clases
 * {@link RatingsDatasetToMySQL} y {@link ContentDatasetToMySQL} y utilizarlos
 * para la recomendación.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 15-Mar-2013
 */
public class MySQLDatabaseDatasetLoader_Default extends MySQLDatabaseDatasetLoaderAbstract implements ContentDatasetLoader, UsersDatasetLoader {

    private static final long serialVersionUID = 1L;
    private RatingsDataset<? extends Rating> ratingsDataset;
    private ContentDataset contentDataset;
    private UsersDataset usersDataset;

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
                RatingsDatasetToMySQL datasetToMySQL = new RatingsDatasetToMySQL(getMySQLConnection());
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
        if (contentDataset == null) {
            try {
                ContentDatasetToMySQL contentDatasetToMySQL = new ContentDatasetToMySQL(getMySQLConnection());
                contentDataset = contentDatasetToMySQL.readDataset();
            } catch (ClassNotFoundException ex) {
                ERROR_CODES.DEPENDENCY_NOT_FOUND.exit(ex);
            } catch (SQLException ex) {
                throw new CannotLoadContentDataset(ex);
            }
        }
        return contentDataset;
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

    @Override
    public UsersDataset getUsersDataset() throws CannotLoadUsersDataset {
        if (usersDataset == null) {
            try {
                UsersDatasetToMySQL usersDatasetToMySQL = new UsersDatasetToMySQL(getMySQLConnection());
                usersDataset = usersDatasetToMySQL.readDataset();
            } catch (ClassNotFoundException ex) {
                ERROR_CODES.DEPENDENCY_NOT_FOUND.exit(ex);
            } catch (SQLException ex) {
                throw new CannotLoadContentDataset(ex);
            }
        }
        return usersDataset;
    }
}
