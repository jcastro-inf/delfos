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
package delfos.rs.persistence;

import java.util.Collection;
import delfos.common.Global;
import delfos.configfile.rs.single.RecommenderSystemConfiguration;
import delfos.rs.GenericRecommenderSystem;

/**
 *
 * @version 30-abr-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
