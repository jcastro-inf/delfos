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

import delfos.common.Global;
import delfos.databaseconnections.DatabaseConection;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.rs.nonpersonalised.meanrating.arithmeticmean.MeanRating;
import delfos.rs.nonpersonalised.meanrating.arithmeticmean.MeanRatingRS;
import delfos.rs.nonpersonalised.meanrating.arithmeticmean.MeanRatingRSModel;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * DAO para base de datos que almacena el modelo de recomendación del sistema {@link MeanRatingRS}.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 28-Mar-2013
 */
public class DAOMeanRatingProfile implements RecommendationModelDatabasePersistence<MeanRatingRSModel> {

    private final String PREFIX = MeanRatingRS.class.getSimpleName();
    private final String PROFILE_TABLE = PREFIX + "_profile";
    private final String ID_COLUMN_NAME = "idItem";
    private final String PREFERENCE_COLUMN_NAME = "preference";

    public DAOMeanRatingProfile() {
    }

    private void createStructure(DatabaseConection databaseConection) throws SQLException {
        //Creacion de las tablas
        try (
                Statement statement = databaseConection.doConnection().createStatement()) {

            statement.execute("DROP TABLE IF EXISTS `" + PROFILE_TABLE + "`;");

            statement.execute("CREATE TABLE `" + PROFILE_TABLE + "` ("
                    + ID_COLUMN_NAME + " int(10) unsigned NOT NULL,"
                    + PREFERENCE_COLUMN_NAME + " double unsigned NOT NULL,"
                    + "PRIMARY KEY (" + ID_COLUMN_NAME + ")"
                    + ") ENGINE=MyISAM DEFAULT CHARSET=latin1;");
        }
    }

    @Override
    public MeanRatingRSModel loadModel(DatabasePersistence databasePersistence, Collection<Long> users, Collection<Long> items, DatasetLoader<? extends Rating> datasetLoader) throws FailureInPersistence {
        try (Statement statement = databasePersistence.getConection().doConnection().createStatement()) {

            List<MeanRating> profiles = new LinkedList<>();

            final String stringStatement = "SELECT " + ID_COLUMN_NAME + "," + PREFERENCE_COLUMN_NAME + " FROM " + PROFILE_TABLE + " ORDER BY " + PREFERENCE_COLUMN_NAME + " DESC;";
            ResultSet rst = statement.executeQuery(stringStatement);

            while (rst.next()) {
                int idItem = rst.getInt(1);
                double preference = rst.getDouble(2);

                Item item = datasetLoader.getContentDataset().get(idItem);
                MeanRating meanRating = new MeanRating(item, preference);
                profiles.add(meanRating);
            }

            return new MeanRatingRSModel(profiles);
        } catch (Exception ex) {
            throw new FailureInPersistence(ex);
        }
    }

    @Override
    public void saveModel(DatabasePersistence databasePersistence, MeanRatingRSModel model) throws FailureInPersistence {

        try {
            createStructure(databasePersistence.getConection());

            try (Statement statement = databasePersistence.getConection().doConnection().createStatement()) {
                List<MeanRating> meanProfile = model.getSortedMeanRatings();

                for (MeanRating mr : meanProfile) {
                    if (Double.isNaN(mr.getPreference())) {
                        continue;
                    }
                    String statementString = "INSERT INTO " + PROFILE_TABLE + "(" + ID_COLUMN_NAME + "," + PREFERENCE_COLUMN_NAME + ") "
                            + "VALUES (" + mr.getItem().getId() + "," + mr.getPreference() + ");";
                    Global.showInfoMessage(statementString + "\n");
                    statement.executeUpdate(statementString);
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            throw new FailureInPersistence(ex);
        }
    }
}
