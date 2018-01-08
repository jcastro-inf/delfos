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
package delfos.group.experiment.validation.predictionvalidation;

import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.generated.modifieddatasets.changeratings.RatingsDatasetOverwrite;
import delfos.dataset.loaders.given.DatasetLoaderGivenRatingsDataset;
import delfos.dataset.util.DatasetUtilities;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Implementa el protocolo de predicción todos menos uno, predice cada
 * valoración teniendo en cuenta el resto de valoraciones. Se realizan n
 * predicciones por grupo, donde n es el número de productos valorados por el
 * grupo.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 (10-12-2012)
 */
public class AllButOne extends GroupPredictionProtocol {

    public AllButOne() {
        super();
    }

    private Collection<Long> getRatedItems(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers group) throws CannotLoadRatingsDataset, UserNotFound {
        return DatasetUtilities.getMembersRatings_byItem(group, datasetLoader).keySet();
    }

    @Override
    public Collection<GroupRecommendationRequest> getGroupRecommendationRequests(
            DatasetLoader<? extends Rating> trainDatasetLoader, DatasetLoader<? extends Rating> testDatasetLoader, GroupOfUsers group) throws CannotLoadRatingsDataset, UserNotFound {

        Collection<Long> ratedItems = getRatedItems(testDatasetLoader, group);

        ArrayList<GroupRecommendationRequest> groupRecommendationRequests = new ArrayList<>(ratedItems.size());

        for (long idItem : ratedItems) {

            //Añado todos los rating en test del grupo, excepto los ratings de este item, ya que será el item que se pida predecir.
            Map<Long, Map<Long, Number>> membersRatings_byItem = DatasetUtilities.getMembersRatings_byItem(group, testDatasetLoader);
            membersRatings_byItem.remove(idItem);

            Map<Long, Map<Long, Number>> predictionMembersRatings_byUser = DatasetUtilities.transformIndexedByItemToIndexedByUser_Map(membersRatings_byItem);
            Map<Long, Map<Long, Rating>> predictionMembersRatings_byUser_Rating = DatasetUtilities.getMapOfMaps_Rating(predictionMembersRatings_byUser);

            DatasetLoader<Rating> predictionPhaseDatasetLoader
                    = new DatasetLoaderGivenRatingsDataset<>(trainDatasetLoader,
                            RatingsDatasetOverwrite.createRatingsDataset((RatingsDataset<Rating>) trainDatasetLoader.getRatingsDataset(), predictionMembersRatings_byUser_Rating));

            Set<Long> itemsToPredict = new TreeSet<>(Arrays.asList(idItem));
            groupRecommendationRequests.add(new GroupRecommendationRequest(group, predictionPhaseDatasetLoader,
                    itemsToPredict.stream().map(idItem2 -> trainDatasetLoader.getContentDataset().get(idItem2)).collect(Collectors.toSet())
            ));

        }
        return groupRecommendationRequests;
    }

}
