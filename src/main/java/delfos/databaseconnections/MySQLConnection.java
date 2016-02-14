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
package delfos.databaseconnections;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import delfos.ERROR_CODES;
import delfos.common.Global;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Almacena los datos para hacer una conexión a base de datos mysql. Todas las
 * conexiones que se hagan a partir de las instancias de esta clase con el
 * método {@link MySQLConnection#doConnection() }
 * deben ser cerradas correctamente.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 (1 de Marzo de 2013)
 * @version 2.0 6-mayo-2014 Ahora no es una conexión en sí, sino que almacena
 * los datos para hacer la conexión.
 */
public class MySQLConnection implements DatabaseConection {

    /**
     * Usuario de la conexión.
     */
    private final String user;
    /**
     * Contraseña de la conexión.
     *
     * @deprecated No se puede guardar la contraseña en memoria.
     */
    @Deprecated
    private final String pass;
    /**
     * Nombre de la base de datos.
     */
    private final String databaseName;
    /**
     * Dirección del servidor que tiene la base de datos.
     */
    private final String serverName;
    /**
     * Puerto por el que se realiza la conexión.
     */
    private final int port;
    /**
     * Prefijo de las tablas.
     */
    private final String prefix;

    private Connection activeConnection = null;

    /**
     * Constructor de la conexión con una base de datos MySQL.
     *
     * @param user Usuario de la conexión.
     * @param pass Contraseña de la conexión.
     * @param databaseName Nombre de la base de datos.
     * @param serverName Dirección del servidor que tiene la base de datos.
     * @param port Puerto por el que se realiza la conexión
     * @param prefix Prefijo de las tablas.
     * @throws ClassNotFoundException Si no se encuentra la clase para la
     * conexión con MySQL, porque no se encuentra el conector de MySQL.
     * @throws SQLException Si ocurre un error en la conexión. Normalmente se
     * produce por inexistencia del usuario, error en la contraseña o nombre de
     * base de datos erroneo.
     */
    public MySQLConnection(String user, String pass, String databaseName, String serverName, int port, String prefix) throws ClassNotFoundException, SQLException {
        if (Global.isVerboseAnnoying()) {
            Global.showInfoMessage(MySQLConnection.class + ": Making database connection with properties:\n");
            Global.showInfoMessage("\tUser -----------> " + user + "\n");
            Global.showInfoMessage("\tPass -----------> " + pass + "\n");
            Global.showInfoMessage("\tDatabase Name --> " + databaseName + "\n");
            Global.showInfoMessage("\tServer Name ----> " + serverName + "\n");
            Global.showInfoMessage("\tPort -----------> " + port + "\n");
            Global.showInfoMessage("\tPrefix ---------> " + prefix + "\n");
        }

        this.user = user;
        this.pass = pass;
        this.databaseName = databaseName;
        this.serverName = serverName;
        this.port = port;
        this.prefix = prefix;

        doConnection();

    }

    @Override
    public final Connection doConnection() throws SQLException {
        if (activeConnection == null) {

            try {
                // Cargamos el controlador JDBC
                Class.forName("com.mysql.jdbc.Driver");
                MysqlDataSource dataSource = new MysqlDataSource();

                dataSource.setUser(user);
                dataSource.setPassword(pass);
                dataSource.setDatabaseName(databaseName);
                dataSource.setServerName(serverName);
                dataSource.setPort(port);

                Connection connection = dataSource.getConnection();
                connection.setAutoCommit(true);
                activeConnection = connection;
            } catch (ClassNotFoundException ex) {
                ERROR_CODES.DEPENDENCY_NOT_FOUND.exit(ex);
                throw new IllegalArgumentException(ex);
            }
        }

        return activeConnection;
    }

    @Override
    @Deprecated
    public String getPass() {
        return pass;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public String getHostName() {
        return serverName;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public DatabaseConection copyWithPrefix(String prefix) throws SQLException {
        try {
            return new MySQLConnection(user, pass, databaseName, serverName, port, this.prefix + prefix);
        } catch (ClassNotFoundException ex) {
            ERROR_CODES.DEPENDENCY_NOT_FOUND.exit(ex);
            throw new IllegalArgumentException(ex);
        }
    }

    public static boolean existsTable(MySQLConnection mysqlConnection, String tableName) {
        return existsTableWithPrefix(mysqlConnection, mysqlConnection.getPrefix() + tableName);
    }

    public static boolean existsTableWithPrefix(MySQLConnection mysqlConnection, String tableNameWithPrefix) {

        String select = "Select count(*) from " + tableNameWithPrefix + ";";
        try (Statement statement = mysqlConnection.doConnection().createStatement()) {
            statement.execute(select);
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    @Override
    public void close() throws SQLException {
        final Connection connection = doConnection();
        if (!connection.getAutoCommit()) {
            doConnection().commit();
        }
        doConnection().close();
        activeConnection = null;
    }

    public void execute(String sentence) throws SQLException {
        Connection connection = doConnection();
        try (Statement statement = connection.createStatement()) {
            statement.execute(sentence);
        } catch (Exception ex) {
            Global.showInfoMessage("ERROR in sentence:\n");
            Global.showInfoMessage(sentence + "\n");
            throw new SQLException(ex);
        }
    }

    public ResultSet executeQuery(String sentence) throws SQLException {
        Connection connection = doConnection();
        try {
            Statement statement = connection.createStatement();
            return statement.executeQuery(sentence);
        } catch (SQLException ex) {
            Global.showInfoMessage("ERROR in sentence:\n");
            Global.showInfoMessage("\t" + sentence + "\n");
            throw ex;
        }
    }

    public void commit() throws SQLException {
        if (!doConnection().getAutoCommit()) {
            doConnection().commit();
        }
    }
}
