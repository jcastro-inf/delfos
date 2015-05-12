package delfos;

import delfos.databaseconnections.DatabaseConection;
import delfos.rs.persistence.DatabasePersistenceTest;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 04-Noviembre-2013
 */
public class MySQLConnectionMock implements DatabaseConection {

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
    private final String hostName;
    /**
     * Puerto por el que se realiza la conexión.
     */
    private final int port;

    public MySQLConnectionMock() {
        this.user = DatabasePersistenceTest.user;
        this.pass = DatabasePersistenceTest.pass;
        this.databaseName = DatabasePersistenceTest.databaseName;
        this.hostName = DatabasePersistenceTest.hostName;
        this.port = DatabasePersistenceTest.port;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String getPass() {
        return pass;
    }

    @Override
    public String getPrefix() {
        return "";
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public String getHostName() {
        return hostName;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public DatabaseConection copyWithPrefix(String prefix) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Connection doConnection() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
