package delfos.rs.persistence;

import java.sql.SQLException;
import delfos.ERROR_CODES;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.parameters.restriction.PasswordParameter;
import delfos.common.parameters.restriction.StringParameter;
import delfos.databaseconnections.DatabaseConection;
import delfos.databaseconnections.MySQLConnection;

/**
 * Objeto que almacena la conexion de base de datos que se utiliza para
 * almacenar el modelo generado por un sistema de recomendación
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class DatabasePersistence extends ParameterOwnerAdapter implements PersistenceMethod {

    private static final long serialVersionUID = 1L;
    private DatabaseConection conection = null;
    public static final Parameter USER = new Parameter("User", new StringParameter("jcastro"));
    public static final Parameter PASSWORD = new Parameter("Password", new PasswordParameter("jcastro"));
    public static final Parameter DATABASE_NAME = new Parameter("databaseName", new StringParameter("castro"));
    public static final Parameter HOST_NAME = new Parameter("serverName", new StringParameter("localhost"));
    public static final Parameter PORT = new Parameter("port", new IntegerParameter(1000, 10000, 3306));
    public static final Parameter PREFIX = new Parameter("prefix", new StringParameter("rs_model_"));

    public DatabasePersistence() {
        super();
        addParameter(USER);
        addParameter(PASSWORD);
        addParameter(DATABASE_NAME);
        addParameter(HOST_NAME);
        addParameter(PORT);
        addParameter(PREFIX);

        addParammeterListener(() -> {
            conection = null;
        });
    }

    public DatabasePersistence(DatabaseConection databaseConection) {
        this(
                databaseConection.getUser(),
                databaseConection.getPass(),
                databaseConection.getDatabaseName(),
                databaseConection.getHostName(),
                databaseConection.getPort(),
                databaseConection.getPrefix()
        );

    }

    public DatabasePersistence(String user, String pass, String databaseName, String hostName, int port, String prefix) {
        this();

        setParameterValue(USER, user);
        setParameterValue(PASSWORD, pass);
        setParameterValue(DATABASE_NAME, databaseName);
        setParameterValue(HOST_NAME, hostName);
        setParameterValue(PORT, port);
        setParameterValue(PREFIX, prefix);
    }

    public DatabaseConection getConection() throws SQLException, ClassNotFoundException {
        if (conection == null) {
            try {
                conection = new MySQLConnection(
                        getUser(), getPass(), getDatabase(), getServer(), getPort(), getPrefix());
            } catch (ClassNotFoundException ex) {
                ERROR_CODES.DEPENDENCY_NOT_FOUND.exit(ex);
                throw ex;
            } catch (SQLException ex) {
                ERROR_CODES.DATABASE_NOT_READY.exit(ex);
                throw ex;
            }
        }
        return conection;
    }

    public String getUser() {
        return (String) getParameterValue(USER);
    }

    /**
     *
     * @return @deprecated No se debe usar el método por seguridad de las
     * claves.
     */
    public String getPass() {
        return (String) getParameterValue(PASSWORD);
    }

    public String getDatabase() {
        return (String) getParameterValue(DATABASE_NAME);
    }

    public String getServer() {
        return (String) getParameterValue(HOST_NAME);
    }

    public int getPort() {
        return (Integer) getParameterValue(PORT);
    }

    public String getPrefix() {
        return (String) getParameterValue(PREFIX);
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.PERSISTENCE_METHOD;
    }
}
