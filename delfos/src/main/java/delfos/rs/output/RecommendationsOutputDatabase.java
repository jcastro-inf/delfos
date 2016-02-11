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
package delfos.rs.output;

import delfos.ERROR_CODES;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.parameters.restriction.PasswordParameter;
import delfos.common.parameters.restriction.StringParameter;
import delfos.databaseconnections.DatabaseConection;
import delfos.databaseconnections.MySQLConnection;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Escribe las recomendaciones en una tabla de base de datos MySQL.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 28-oct-2013
 */
public class RecommendationsOutputDatabase extends RecommendationsOutputMethod {

    private static final long serialVersionUID = 1L;
    private DatabaseConection conection = null;
    /**
     * Nombre de usuario para la conexión que escribe las recomendaciones.
     */
    public static final Parameter CONNECTION_USER = new Parameter("Connection_User", new StringParameter("jcastro"));
    /**
     * Password para la conexión que escribe las recomendaciones.
     */
    public static final Parameter CONNECTION_PASSWORD = new Parameter("Connection_Password", new PasswordParameter("jcastro"));
    /**
     * Nombre de la base de datos para la conexión que escribe las
     * recomendaciones.
     */
    public static final Parameter CONNECTION_DATABASE_NAME = new Parameter("Connection_DatabaseName", new StringParameter("castro"));
    /**
     * Servidor para la conexión que escribe las recomendaciones.
     */
    public static final Parameter CONNECTION_HOST_NAME = new Parameter("Connection_ServerName", new StringParameter("localhost"));
    /**
     * Puerto para la conexión que escribe las recomendaciones.
     */
    public static final Parameter CONNECTION_PORT = new Parameter("Connection_Port", new IntegerParameter(0, 10000, 3306));
    /**
     * Nombre de la tabla en la que se almacenan las recomendaciones.
     */
    public static final Parameter TABLE_NAME = new Parameter("Table_Name", new StringParameter("recommendations_table"));
    /**
     * Nombre del campo de la tabla de recomendaciones en que se amacena el id
     * de usuario.
     */
    public static final Parameter TABLE_FIELD_TARGET_ID = new Parameter("Table_ColumnIdUser", new StringParameter("idUser"));
    /**
     * Nombre del campo de la tabla de recomendaciones en que se amacena el id
     * de producto.
     */
    public static final Parameter TABLE_FIELD_ITEM_ID = new Parameter("Table_ColumnIdItem", new StringParameter("idItem"));
    /**
     * Nombre del campo de la tabla de recomendaciones en que se amacena el
     * valor de preferencia de la recomendación.
     */
    public static final Parameter TABLE_FIELD_PREFERENCE = new Parameter("Table_ColumnPreference", new StringParameter("preference"));
    /**
     * Nombre del campo de la tabla de recomendaciones en que se amacena el
     * nombre del sistema de recomendación que genera las recomendaciones.
     */
    public static final Parameter TABLE_FIELD_RECOMMENDER = new Parameter("Table_ColumnRecommender", new StringParameter("system"));
    /**
     * Valor que se introduce como nombre de sistema de recomendación.
     */
    public static final Parameter TABLE_FIELD_RECOMMENDER_VALUE = new Parameter("Table_RecommenderValue", new StringParameter("recommender"));

    /**
     * Constructor por defecto, que añade los parámetros de este método de
     * salida de recomendaciones.
     */
    public RecommendationsOutputDatabase() {
        super();
        addParameter(CONNECTION_USER);
        addParameter(CONNECTION_PASSWORD);
        addParameter(CONNECTION_DATABASE_NAME);
        addParameter(CONNECTION_HOST_NAME);
        addParameter(CONNECTION_PORT);

        addParameter(TABLE_NAME);
        addParameter(TABLE_FIELD_TARGET_ID);
        addParameter(TABLE_FIELD_ITEM_ID);
        addParameter(TABLE_FIELD_PREFERENCE);
        addParameter(TABLE_FIELD_RECOMMENDER);
        addParameter(TABLE_FIELD_RECOMMENDER_VALUE);
    }

    /**
     * Crea la salida de recomendaciones a partir de una conexión de base de
     * datos y los parámetros que definen nombre y campos de la tabla de
     * recomendaciones.
     *
     * @param databaseConection Conexión cuyos parámetros se utilizan para
     * escribir las recomendaciones.
     * @param tableName Nombre de la tabla de recomendaciones.
     * @param field_idUser Nombre del campo del identificador de usuario.
     * @param field_idItem Nombre del campo del identificador de producto.
     * @param field_preference Nombre del campo del valor de preferencia.
     * @param field_recommender Nombre del campo que almacena el nombre del
     * sistema de recomendación.
     * @param recommenderName Nombre del sistema de recomendación.
     */
    public RecommendationsOutputDatabase(DatabaseConection databaseConection,
            String tableName,
            String field_idUser,
            String field_idItem,
            String field_preference,
            String field_recommender,
            String recommenderName) {

        this();

        setParameterValue(CONNECTION_USER, databaseConection.getUser());
        setParameterValue(CONNECTION_PASSWORD, databaseConection.getPass());
        setParameterValue(CONNECTION_DATABASE_NAME, databaseConection.getDatabaseName());
        setParameterValue(CONNECTION_HOST_NAME, databaseConection.getHostName());
        setParameterValue(CONNECTION_PORT, databaseConection.getPort());

        setParameterValue(TABLE_NAME, tableName);
        setParameterValue(TABLE_FIELD_TARGET_ID, field_idUser);
        setParameterValue(TABLE_FIELD_ITEM_ID, field_idItem);
        setParameterValue(TABLE_FIELD_PREFERENCE, field_preference);
        setParameterValue(TABLE_FIELD_RECOMMENDER, field_recommender);
        setParameterValue(TABLE_FIELD_RECOMMENDER_VALUE, recommenderName);
    }

    @Override
    public void writeRecommendations(Recommendations recommendationsToUser) {
        try {
            createTableIfNotExists();
        } catch (ClassNotFoundException ex) {
            ERROR_CODES.DEPENDENCY_NOT_FOUND.exit(ex);
        } catch (SQLException ex) {
            ERROR_CODES.CANNOT_WRITE_RECOMMENDATIONS.exit(ex);
        }

        final String idUser = recommendationsToUser.getTargetIdentifier();

        //Primero borro de la tabla si el usuario tenía recomendaciones.
        try (
                Statement deleteStatement = getConection().doConnection().createStatement()) {

            //CleanUserRecomendations.
            String deleteStatementString = "delete from " + getTableName() + "\n"
                    + " where \n"
                    + getIdTargetField() + "=" + idUser + " \n"
                    + "and " + getRecommenderField() + " = '" + getRecommenderFieldValue() + "';";

            deleteStatement.executeUpdate(deleteStatementString);
            deleteStatement.close();
        } catch (ClassNotFoundException ex) {
            ERROR_CODES.DEPENDENCY_NOT_FOUND.exit(ex);
        } catch (SQLException ex) {
            ERROR_CODES.CANNOT_WRITE_RECOMMENDATIONS.exit(ex);
        }

        List<Recommendation> topNrecommendations = new ArrayList<>(recommendationsToUser.getRecommendations());

        if (getNumberOfRecommendations() > 0) {
            Collections.sort(topNrecommendations);
            topNrecommendations = topNrecommendations.subList(0, Math.min(topNrecommendations.size(), getNumberOfRecommendations()));
        }

        //Escribo las recomendaciones.
        try (
                Statement statement = getConection().doConnection().createStatement()) {

            for (Recommendation r : topNrecommendations) {
                String insert = "INSERT INTO " + getTableName() + "(" + getIdTargetField() + "," + getIdItemField() + "," + getPreferenceField() + "," + getRecommenderField() + ") VALUES "
                        + "(" + idUser + "," + r.getIdItem() + "," + r.getPreference().doubleValue() + ",'" + getRecommenderFieldValue() + "');";
                statement.executeUpdate(insert);
            }
            statement.close();
        } catch (ClassNotFoundException ex) {
            ERROR_CODES.DEPENDENCY_NOT_FOUND.exit(ex);
        } catch (SQLException ex) {
            ERROR_CODES.CANNOT_WRITE_RECOMMENDATIONS.exit(ex);
        }
    }

    public DatabaseConection getConection() throws SQLException, ClassNotFoundException {
        if (conection == null) {
            try {
                conection = new MySQLConnection(
                        getUser(), getPass(), getDatabase(), getServer(), getPort(), "");
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
        return (String) getParameterValue(CONNECTION_USER);
    }

    /**
     *
     * @return @deprecated No se debe usar el método por seguridad de las
     * claves.
     */
    public String getPass() {
        return (String) getParameterValue(CONNECTION_PASSWORD);
    }

    public String getDatabase() {
        return (String) getParameterValue(CONNECTION_DATABASE_NAME);
    }

    public String getServer() {
        return (String) getParameterValue(CONNECTION_HOST_NAME);
    }

    public int getPort() {
        return (Integer) getParameterValue(CONNECTION_PORT);
    }

    private void createTableIfNotExists() throws ClassNotFoundException, SQLException {
        StringBuilder create = new StringBuilder();

        create.append("CREATE TABLE IF NOT EXISTS ").append(getTableName()).append(" (\n");
        create.append(getIdTargetField()).append(" varchar(45) NOT NULL,\n");
        create.append(getIdItemField()).append(" int(11) NOT NULL,\n");
        create.append(getPreferenceField()).append(" double NOT NULL,\n");
        create.append(getRecommenderField()).append(" varchar(45) NOT NULL,\n");
        create.append("  id int(11) NOT NULL AUTO_INCREMENT,\n");
        create.append("  PRIMARY KEY (id)\n");
        create.append(") ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=1;");

        try (
                Statement statement = getConection().doConnection().createStatement()) {
            statement.execute(create.toString());
        }
    }

    public String getTableName() {
        return (String) getParameterValue(TABLE_NAME);
    }

    public String getIdTargetField() {
        return (String) getParameterValue(TABLE_FIELD_TARGET_ID);
    }

    public String getIdItemField() {
        return (String) getParameterValue(TABLE_FIELD_ITEM_ID);
    }

    public String getPreferenceField() {
        return (String) getParameterValue(TABLE_FIELD_PREFERENCE);
    }

    public String getRecommenderField() {
        return (String) getParameterValue(TABLE_FIELD_RECOMMENDER);
    }

    public String getRecommenderFieldValue() {
        return (String) getParameterValue(TABLE_FIELD_RECOMMENDER_VALUE);
    }
}
