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
import delfos.databaseconnections.DatabaseConection;
import delfos.databaseconnections.MySQLConnection;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;

/**
 * Clase que añade los parámetros generales necesarios para recuperar datasets
 * de una base de datos MySQL. Con esta clase abstracta, se añaden como
 * parámetro las variables comunes, tal como el nombre de usuario, puerto,
 * servidor, etc.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 (05/12/2012)
 */
public abstract class MySQLDatabaseDatasetLoaderAbstract extends DatasetLoaderAbstract implements MySQLDatabaseDatasetLoader {

    private MySQLConnection conexion = null;

    public MySQLDatabaseDatasetLoaderAbstract() {
        addParameter(CONNECTION_CONFIGURATION_USER);
        addParameter(CONNECTION_CONFIGURATION_PASSWORD);
        addParameter(CONNECTION_CONFIGURATION_DATABASE_NAME);
        addParameter(CONNECTION_CONFIGURATION_HOST_NAME);
        addParameter(CONNECTION_CONFIGURATION_PORT);
        addParameter(CONNECTION_CONFIGURATION_PREFIX);

        addParammeterListener(() -> {
            conexion = null;
        });
    }

    @Override
    public DatabaseConection getConnection() {
        if (conexion == null) {
            try {
                String _user = (String) getParameterValue(CONNECTION_CONFIGURATION_USER);
                String _pass = (String) getParameterValue(CONNECTION_CONFIGURATION_PASSWORD);
                String _databaseName = (String) getParameterValue(CONNECTION_CONFIGURATION_DATABASE_NAME);
                String _serverName = (String) getParameterValue(CONNECTION_CONFIGURATION_HOST_NAME);
                int _port = (Integer) getParameterValue(CONNECTION_CONFIGURATION_PORT);

                String _prefix = (String) getParameterValue(CONNECTION_CONFIGURATION_PREFIX);
                conexion = new MySQLConnection(_user, _pass, _databaseName, _serverName, _port, _prefix);
            } catch (Exception ex) {
                ERROR_CODES.DATABASE_NOT_READY.exit(ex);
                throw new IllegalStateException(ex);
            }
        }
        return conexion;
    }
}
