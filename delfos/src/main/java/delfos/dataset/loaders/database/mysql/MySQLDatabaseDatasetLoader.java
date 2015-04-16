/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
