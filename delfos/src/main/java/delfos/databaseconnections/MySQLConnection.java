package delfos.databaseconnections;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import delfos.ERROR_CODES;
import delfos.common.Global;

/**
 * Almacena los datos para hacer una conexión a base de datos mysql. Todas las
 * conexiones que se hagan a partir de las instancias de esta clase con el
 * método {@link MySQLConnection#doConnection() }
 * deben ser cerradas correctamente.
 *
 * @author Jorge Castro Gallardo
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
            Global.showMessage(MySQLConnection.class + ": Making database connection with properties:\n");
            Global.showMessage("\tUser -----------> " + user + "\n");
            Global.showMessage("\tPass -----------> " + pass + "\n");
            Global.showMessage("\tDatabase Name --> " + databaseName + "\n");
            Global.showMessage("\tServer Name ----> " + serverName + "\n");
            Global.showMessage("\tPort -----------> " + port + "\n");
            Global.showMessage("\tPrefix ---------> " + prefix + "\n");
        }

        this.user = user;
        this.pass = pass;
        this.databaseName = databaseName;
        this.serverName = serverName;
        this.port = port;
        this.prefix = prefix;

        try (Connection conn = doConnection()) {
        }

    }

    @Override
    public final Connection doConnection() throws SQLException {
        try {
            // Cargamos el controlador JDBC
            Class.forName("com.mysql.jdbc.Driver");
            MysqlDataSource dataSource = new MysqlDataSource();

            dataSource.setUser(user);
            dataSource.setPassword(pass);
            dataSource.setDatabaseName(databaseName);
            dataSource.setServerName(serverName);
            dataSource.setPort(port);

            return dataSource.getConnection();
        } catch (ClassNotFoundException ex) {
            ERROR_CODES.DEPENDENCY_NOT_FOUND.exit(ex);
            throw new IllegalArgumentException(ex);
        }
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
        try (Connection connection = mysqlConnection.doConnection()) {
            String select = "Select count(*) from " + tableNameWithPrefix + ";";
            try (Statement statement = connection.createStatement()) {
                statement.execute(select);
                return true;
            } catch (SQLException ex) {
                return false;
            }
        } catch (SQLException ex) {
            ERROR_CODES.DATABASE_NOT_READY.exit(ex);
            throw new IllegalArgumentException(ex);
        }
    }
}
