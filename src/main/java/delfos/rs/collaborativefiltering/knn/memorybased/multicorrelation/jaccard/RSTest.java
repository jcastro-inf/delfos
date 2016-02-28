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
package delfos.rs.collaborativefiltering.knn.memorybased.multicorrelation.jaccard;

import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.RecommenderSystemParameterRestriction;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.rs.collaborativefiltering.CollaborativeRecommender;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;

/**
 *
 * @version 08-may-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class RSTest extends CollaborativeRecommender<Object> {

    private static final long serialVersionUID = 1L;
    private static final Parameter INNER_RS = new Parameter("inner_rs", new RecommenderSystemParameterRestriction(new KnnMemoryBasedCFRS(), CollaborativeRecommender.class));

    public RSTest() {
        super();
        addParameter(INNER_RS);
    }

    @Override
    public Object buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) {
        CollaborativeRecommender rs = getInnerRS();
        Object buildRecommendationModel = rs.buildRecommendationModel(datasetLoader);

        return rs;
    }

    @Override
    public Collection<Recommendation> recommendToUser(DatasetLoader<? extends Rating> datasetLoader, Object model, Integer idUser, java.util.Set<Integer> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {

        if (Global.isVerboseAnnoying()) {
            Global.showMessageTimestamped(this.getAlias() + " --> Recommending for user '" + idUser + "'\n");
        }
        return getInnerRS().recommendToUser(datasetLoader, model, idUser, candidateItems);
    }

    @Override
    public Object loadRecommendationModel(DatabasePersistence databasePersistence, Collection<Integer> users, Collection<Integer> items, DatasetLoader<? extends Rating> datasetLoader) throws FailureInPersistence {
        return new Object();
    }

    @Override
    public void saveRecommendationModel(DatabasePersistence databasePersistence, Object model) throws FailureInPersistence {
        //No hay modelo que guardar.

    }

    private CollaborativeRecommender getInnerRS() {
        return (CollaborativeRecommender) getParameterValue(INNER_RS);
    }
}
