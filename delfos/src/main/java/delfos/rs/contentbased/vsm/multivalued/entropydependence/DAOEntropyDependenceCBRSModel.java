package delfos.rs.contentbased.vsm.multivalued.entropydependence;

import delfos.ERROR_CODES;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureGenerator;
import delfos.dataset.basic.features.FeatureType;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 29-oct-2013
 */
public class DAOEntropyDependenceCBRSModel {

    public DAOEntropyDependenceCBRSModel() {
    }
    private static final String _FEATURES_TABLE_NAME = "features";
    private static final String _FEATURES_TABLE_NAME_TEMP = "features_temp";
    private static final String FEATURES_FIELD_FEATURE = "feature";
    private static final String FEATURES_FIELD_TYPE = "weight";
    private static final String _FEATURE_WEIGHTS_TABLE_NAME = "feature_weights";
    private static final String _FEATURE_WEIGHTS_TABLE_NAME_TEMP = "feature_weights_temp";
    private static final String FEATURE_WEIGHTS_FIELD_FEATURE = "feature";
    private static final String FEATURE_WEIGHTS_FIELD_WEIGHT = "weight";
    private static final String _ITEM_PROFILES_TABLE_NAME = "item_profiles";
    private static final String _ITEM_PROFILES_TABLE_NAME_TEMP = "item_profiles_temp";
    private static final String ITEM_PROFILES_FIELD_ID_ITEM = "idItem";
    private static final String ITEM_PROFILES_FIELD_FEATURE = "feature";
    private static final String ITEM_PROFILES_FIELD_FEATURE_VALUE = "featureValue";

    public final String getFEATURES_TABLE_NAME(DatabasePersistence databasePersistence) {
        return databasePersistence.getPrefix() + _FEATURES_TABLE_NAME;
    }

    public final String getFEATURES_TABLE_NAME_TEMP(DatabasePersistence databasePersistence) {
        return databasePersistence.getPrefix() + _FEATURES_TABLE_NAME_TEMP;
    }

    public final String getFEATURE_WEIGHTS_TABLE_NAME(DatabasePersistence databasePersistence) {
        return databasePersistence.getPrefix() + _FEATURE_WEIGHTS_TABLE_NAME;
    }

    public final String getFEATURE_WEIGHTS_TABLE_NAME_TEMP(DatabasePersistence databasePersistence) {
        return databasePersistence.getPrefix() + _FEATURE_WEIGHTS_TABLE_NAME_TEMP;
    }

    public final String getITEM_PROFILES_TABLE_NAME(DatabasePersistence databasePersistence) {
        return databasePersistence.getPrefix() + _ITEM_PROFILES_TABLE_NAME;
    }

    public final String getITEM_PROFILES_TABLE_NAME_TEMP(DatabasePersistence databasePersistence) {
        return databasePersistence.getPrefix() + _ITEM_PROFILES_TABLE_NAME_TEMP;
    }

    public void createModelTables(DatabasePersistence databasePersistence) throws ClassNotFoundException, SQLException {
        try (
                Statement createStatement = databasePersistence.getConection().doConnection().createStatement()) {

            String createStatementString0 = "CREATE TABLE IF NOT EXISTS " + getFEATURES_TABLE_NAME_TEMP(databasePersistence) + " (\n"
                    + FEATURES_FIELD_FEATURE + " varchar(255) NOT NULL,\n"
                    + FEATURES_FIELD_TYPE + " varchar(255) NOT NULL\n"
                    + ") DEFAULT CHARSET=latin1;\n";
            createStatement.execute("DROP TABLE IF EXISTS " + getFEATURES_TABLE_NAME_TEMP(databasePersistence) + ";");
            createStatement.execute(createStatementString0);

            String createStatementString1 = "CREATE TABLE IF NOT EXISTS " + getFEATURE_WEIGHTS_TABLE_NAME_TEMP(databasePersistence) + " (\n"
                    + FEATURE_WEIGHTS_FIELD_FEATURE + " varchar(255) NOT NULL,\n"
                    + FEATURE_WEIGHTS_FIELD_WEIGHT + " float NOT NULL\n"
                    + ") DEFAULT CHARSET=latin1;\n";
            createStatement.execute("DROP TABLE IF EXISTS " + getFEATURE_WEIGHTS_TABLE_NAME_TEMP(databasePersistence) + ";");
            createStatement.execute(createStatementString1);

            String createStatementString2 = "CREATE TABLE IF NOT EXISTS " + getITEM_PROFILES_TABLE_NAME_TEMP(databasePersistence) + " (\n"
                    + ITEM_PROFILES_FIELD_ID_ITEM + " int(11) NOT NULL,\n"
                    + ITEM_PROFILES_FIELD_FEATURE + " varchar(255) NOT NULL,\n"
                    + ITEM_PROFILES_FIELD_FEATURE_VALUE + " varchar(255) NOT NULL\n"
                    + ") DEFAULT CHARSET=latin1;";
            createStatement.execute("DROP TABLE IF EXISTS " + getITEM_PROFILES_TABLE_NAME_TEMP(databasePersistence) + ";");
            createStatement.execute(createStatementString2);
        }
    }

    public void fixModel(DatabasePersistence databasePersistence) throws FailureInPersistence {
        try (
                Statement st = databasePersistence.getConection().doConnection().createStatement()) {

            st.execute("DROP TABLE IF EXISTS " + getFEATURES_TABLE_NAME(databasePersistence) + ";");
            st.execute("ALTER TABLE " + getFEATURES_TABLE_NAME_TEMP(databasePersistence) + " RENAME TO " + getFEATURES_TABLE_NAME(databasePersistence) + ";");

            st.execute("DROP TABLE IF EXISTS " + getFEATURE_WEIGHTS_TABLE_NAME(databasePersistence) + ";");
            st.execute("ALTER TABLE " + getFEATURE_WEIGHTS_TABLE_NAME_TEMP(databasePersistence) + " RENAME TO " + getFEATURE_WEIGHTS_TABLE_NAME(databasePersistence) + ";");

            st.execute("DROP TABLE IF EXISTS " + getITEM_PROFILES_TABLE_NAME(databasePersistence) + ";");
            st.execute("ALTER TABLE " + getITEM_PROFILES_TABLE_NAME_TEMP(databasePersistence) + " RENAME TO " + getITEM_PROFILES_TABLE_NAME(databasePersistence) + ";");

            st.close();

        } catch (SQLException ex) {
            throw new FailureInPersistence(ex);
        } catch (ClassNotFoundException ex) {
            ERROR_CODES.DEPENDENCY_NOT_FOUND.exit(ex);
        }

    }

    public void saveModel(DatabasePersistence databasePersistence, EntropyDependenceCBRSModel model) throws FailureInPersistence, ClassNotFoundException, SQLException {

        //Creo las tablas
        createModelTables(databasePersistence);

        try (
                Statement statement = databasePersistence.getConection().doConnection().createStatement()) {

            //Las lleno de información
            for (Feature feature : model.getAllFeatures()) {
                //Hago el insert en la tabla de características.
                String featureName = feature.getName();
                String featureType = feature.getType().name();
                String insertFeatureType = "Insert into " + getFEATURES_TABLE_NAME_TEMP(databasePersistence) + "(" + FEATURES_FIELD_FEATURE + "," + FEATURES_FIELD_TYPE + ") values"
                        + "('" + featureName + "','" + featureType + "')";
                statement.executeUpdate(insertFeatureType);

                //Hago el insert en la tabla de ponderaciones.
                String featureWeight = Float.toString(model.getEntropy(feature));
                String insertFeatureWeight = "Insert into " + getFEATURE_WEIGHTS_TABLE_NAME_TEMP(databasePersistence) + "(" + FEATURE_WEIGHTS_FIELD_FEATURE + "," + FEATURE_WEIGHTS_FIELD_WEIGHT + ") values"
                        + "('" + featureName + "'," + featureWeight + ")";
                statement.executeUpdate(insertFeatureWeight);
            }

            for (EntropyDependenceCBRSItemProfile itemProfile : model.values()) {
                int idItemI = itemProfile.getId();
                String idItem = Integer.toString(idItemI);
                for (Feature feature : itemProfile.getFeatures()) {
                    String featureName = feature.getName();
                    String featureValue = itemProfile.getFeatureValue(feature).toString();

                    String insertItemProfileFeatureValue = "Insert into " + getITEM_PROFILES_TABLE_NAME_TEMP(databasePersistence) + " (" + ITEM_PROFILES_FIELD_ID_ITEM + "," + ITEM_PROFILES_FIELD_FEATURE + "," + ITEM_PROFILES_FIELD_FEATURE_VALUE + ") \n"
                            + " values (" + idItem + ",'" + featureName + "','" + featureValue + "')";
                    statement.executeUpdate(insertItemProfileFeatureValue);
                }
            }

            //Las renombro.
            fixModel(databasePersistence);
        } catch (ClassNotFoundException ex) {
            ERROR_CODES.DEPENDENCY_NOT_FOUND.exit(ex);
        } catch (SQLException ex) {
            throw new FailureInPersistence(ex);
        }
    }

    public EntropyDependenceCBRSModel loadModel(DatabasePersistence databasePersistence, Collection<Integer> users, Collection<Integer> items) throws FailureInPersistence {
        try (
                Statement statement = databasePersistence.getConection().doConnection().createStatement()) {
            FeatureGenerator featureGenerator = new FeatureGenerator();

            Map<Integer, EntropyDependenceCBRSItemProfile> itemProfiles = new TreeMap<>();
            Map<Feature, Number> weights = new TreeMap<>();
            Map<String, FeatureType> featureTypes = new TreeMap<>();

            {
                //Leo las características.
                String selectFeatures = "Select " + FEATURES_FIELD_FEATURE + "," + FEATURES_FIELD_TYPE + " \n"
                        + " from " + getFEATURES_TABLE_NAME(databasePersistence) + " \n;";
                ResultSet executeQuery = statement.executeQuery(selectFeatures);

                while (executeQuery.next()) {
                    String featureName = executeQuery.getString(FEATURES_FIELD_FEATURE);
                    String featureTypeString = executeQuery.getString(FEATURES_FIELD_TYPE);

                    FeatureType featureType = FeatureType.getFeatureType(featureTypeString);
                    if (!featureGenerator.containsFeature(featureName)) {
                        featureGenerator.createFeature(featureName, featureType);
                    }

                    featureTypes.put(featureName, featureType);
                }
            }
            {
                // Leo las ponderaciones de las características
                String selectFeatures = "Select " + FEATURE_WEIGHTS_FIELD_FEATURE + "," + FEATURE_WEIGHTS_FIELD_WEIGHT + " \n"
                        + " from " + getFEATURE_WEIGHTS_TABLE_NAME(databasePersistence) + " \n;";
                ResultSet executeQuery = statement.executeQuery(selectFeatures);

                while (executeQuery.next()) {
                    String featureName = executeQuery.getString(FEATURE_WEIGHTS_FIELD_FEATURE);
                    Double featureWeight = executeQuery.getDouble(FEATURE_WEIGHTS_FIELD_WEIGHT);
                    Feature feature = featureGenerator.searchFeature(featureName);

                    weights.put(feature, featureWeight);
                }
            }

            {

                Map<Integer, Map<Feature, Object>> itemFeatureValues = new TreeMap<>();
                //Reading item profiles.
                String selectItemProfiles = "Select " + ITEM_PROFILES_FIELD_ID_ITEM + "," + ITEM_PROFILES_FIELD_FEATURE + "," + ITEM_PROFILES_FIELD_FEATURE_VALUE + " \n"
                        + " from " + getITEM_PROFILES_TABLE_NAME(databasePersistence) + " \n;";
                ResultSet executeQuery = statement.executeQuery(selectItemProfiles);

                while (executeQuery.next()) {
                    int idItem = executeQuery.getInt(ITEM_PROFILES_FIELD_ID_ITEM);
                    String featureName = executeQuery.getString(ITEM_PROFILES_FIELD_FEATURE);
                    String featureValueString = executeQuery.getString(ITEM_PROFILES_FIELD_FEATURE_VALUE);

                    if (!itemFeatureValues.containsKey(idItem)) {
                        itemFeatureValues.put(idItem, new TreeMap<>());
                    }
                    Map<Feature, Object> itemProfile = itemFeatureValues.get(idItem);

                    Feature feature = featureGenerator.searchFeature(featureName);

                    Object featureValue = feature.getType().parseFeatureValue(featureValueString);

                    itemProfile.put(feature, featureValue);
                }

                for (Map.Entry<Integer, Map<Feature, Object>> entry : itemFeatureValues.entrySet()) {
                    int idItem = entry.getKey();
                    Map<Feature, Object> values = entry.getValue();
                    itemProfiles.put(idItem, new EntropyDependenceCBRSItemProfile(idItem, values));
                }
            }

            return new EntropyDependenceCBRSModel(itemProfiles, weights);

        } catch (SQLException ex) {
            throw new IllegalArgumentException(ex);
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
