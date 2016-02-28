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
package delfos.rs.nonpersonalised.mostpopular;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.RecommenderSystemAdapter;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 25-Noviembre-2013
 */
public class MostPopularRS extends RecommenderSystemAdapter<Collection<Recommendation>> {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean isRatingPredictorRS() {
        return false;
    }

    @Override
    public Collection<Recommendation> buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset {

        final double numUsers = datasetLoader.getRatingsDataset().allUsers().size();
        Collection<Recommendation> model = new ArrayList<>(datasetLoader.getRatingsDataset().allRatedItems().size());
        RatingsDataset<? extends Rating> ratingDataset = datasetLoader.getRatingsDataset();
        for (int idItem : ratingDataset.allRatedItems()) {
            try {
                int numRatings = datasetLoader.getRatingsDataset().sizeOfItemRatings(idItem);

                model.add(new Recommendation(idItem, numRatings / numUsers));
            } catch (ItemNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            }
        }

        return model;
    }

    @Override
    public Collection<Recommendation> recommendToUser(DatasetLoader<? extends Rating> datasetLoader, Collection<Recommendation> model, Integer idUser, java.util.Set<Integer> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        Collection<Recommendation> ret = new ArrayList<>(candidateItems.size());

        Set<Integer> added = new TreeSet<>();
        for (Recommendation recommendation : model) {
            if (candidateItems.contains(recommendation.getIdItem())) {
                ret.add(new Recommendation(recommendation.getIdItem(), recommendation.getPreference()));
                added.add(recommendation.getIdItem());
            }
        }

        //Para que la cobertura sea 1 en todos los casos.
        Set<Integer> toAdd = new TreeSet<>(candidateItems);
        toAdd.removeAll(added);
        for (int idItem : toAdd) {
            ret.add(new Recommendation(idItem, 0));
        }

        return ret;
    }
}
