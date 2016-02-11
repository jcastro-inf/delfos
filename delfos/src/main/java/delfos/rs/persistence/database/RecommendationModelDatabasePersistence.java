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
