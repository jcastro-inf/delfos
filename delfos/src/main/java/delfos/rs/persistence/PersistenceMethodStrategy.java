package delfos.rs.persistence;

import java.util.Collection;
import delfos.common.Global;
import delfos.configfile.rs.single.RecommenderSystemConfiguration;
import delfos.rs.GenericRecommenderSystem;

/**
 *
 * @version 30-abr-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class PersistenceMethodStrategy {

    public static <RecommendationModel> RecommendationModel loadModel(GenericRecommenderSystem<RecommendationModel> recommenderSystem, PersistenceMethod persistenceMethod, Collection<Integer> users, Collection<Integer> items) throws FailureInPersistence {
        RecommendationModel RecommendationModel = null;

        boolean methodOK = false;

        if (persistenceMethod instanceof DatabasePersistence) {
            methodOK = true;
            DatabasePersistence databasePersistence = (DatabasePersistence) persistenceMethod;

            Global.showMessageTimestamped("Loading recommendation model from database");
            RecommendationModel = recommenderSystem.loadRecommendationModel(databasePersistence, users, items);
            Global.showMessageTimestamped("Loaded recommendation model from database");
        }

        if (persistenceMethod instanceof FilePersistence) {
            methodOK = true;
            FilePersistence filePersistence = (FilePersistence) persistenceMethod;

            Global.showMessageTimestamped("Loading recommendation model from file");
            RecommendationModel = recommenderSystem.loadRecommendationModel(filePersistence, users, items);
            Global.showMessageTimestamped("Loaded recommendation model from file");
        }

        if (!methodOK) {
            throw new IllegalStateException("Persistence method " + persistenceMethod.getName() + " not known.");
        }

        return RecommendationModel;
    }

    public static void saveModel(GenericRecommenderSystem recommenderSystem, PersistenceMethod persistenceMethod, Object RecommendationModel) throws FailureInPersistence {

        boolean methodOK = false;

        if (persistenceMethod instanceof DatabasePersistence) {
            methodOK = true;
            DatabasePersistence databasePersistence = (DatabasePersistence) persistenceMethod;

            Global.showMessageTimestamped("Saving recommendation model in database");
            recommenderSystem.saveRecommendationModel(databasePersistence, RecommendationModel);
            Global.showMessageTimestamped("Saved recommendation model in database");
        }

        if (persistenceMethod instanceof FilePersistence) {
            methodOK = true;
            FilePersistence filePersistence = (FilePersistence) persistenceMethod;

            Global.showMessageTimestamped("Saving recommendation model in file");
            recommenderSystem.saveRecommendationModel(filePersistence, RecommendationModel);
            Global.showMessageTimestamped("Saved recommendation model in file");
        }

        if (!methodOK) {
            throw new IllegalStateException("Persistence method " + persistenceMethod.getName() + " not known.");
        }
    }

    public static Object loadModel(RecommenderSystemConfiguration rsc) throws FailureInPersistence {
        return loadModel(rsc.recommenderSystem, rsc.persistenceMethod, null, null);
    }

    public static void saveModel(RecommenderSystemConfiguration rsc, Object recommendationModel) throws FailureInPersistence {
        saveModel(rsc.recommenderSystem, rsc.persistenceMethod, recommendationModel);
    }
}
