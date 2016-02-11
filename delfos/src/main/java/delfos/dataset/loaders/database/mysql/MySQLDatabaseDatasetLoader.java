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

import delfos.databaseconnections.DatabaseConection;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.parameters.restriction.PasswordParameter;
import delfos.common.parameters.restriction.StringParameter;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 28-Noviembre-2013
 */
public interface MySQLDatabaseDatasetLoader {

    public Parameter CONNECTION_CONFIGURATION_DATABASE_NAME = new Parameter("databaseName", new StringParameter("jcastro"));
    public Parameter CONNECTION_CONFIGURATION_HOST_NAME = new Parameter("serverName", new StringParameter("localhost"));
    public Parameter CONNECTION_CONFIGURATION_PASSWORD = new Parameter("Password", new PasswordParameter("jcastro"));
    public Parameter CONNECTION_CONFIGURATION_PORT = new Parameter("port", new IntegerParameter(1000, 10000, 3306));
    public Parameter CONNECTION_CONFIGURATION_PREFIX = new Parameter("prefix", new StringParameter("rs_model_"));
    public Parameter CONNECTION_CONFIGURATION_USER = new Parameter("User", new StringParameter("jcastro"));

    /**
     * Devuelve la conexión que se debe utilizar para hacer las consultas a la
     * base de datos.
     *
     * @return
     */
    public DatabaseConection getConnection();
}
