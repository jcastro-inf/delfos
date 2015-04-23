package delfos.rs.persistence.database;

import java.util.Collection;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;

/**
 * Interfaz que implementan los objetos para almacenar-recuperar un modelo de
 * recomendación en base de datos.
 *
 * @author Jorge Castro Gallardo (Sinbad2,Universidad de Jaén)
 * @version 1.0 28-May-2013
 */
public interface RecommendationModelDatabasePersistence<RecommendationModel> {

    public RecommendationModel loadModel(DatabasePersistence databasePersistence, Collection<Integer> users, Collection<Integer> items) throws FailureInPersistence;

    public void saveModel(DatabasePersistence databasePersistence, RecommendationModel model) throws FailureInPersistence;
}
