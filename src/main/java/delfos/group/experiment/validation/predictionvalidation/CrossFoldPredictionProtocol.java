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
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.generated.modifieddatasets.changeratings.RatingsDatasetOverwrite;
import delfos.dataset.loaders.given.DatasetLoaderGivenRatingsDataset;
import delfos.dataset.util.DatasetUtilities;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Implementa el protocolo de predicción similar a la validación cruzada.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 (10-12-2012)
 */
public class CrossFoldPredictionProtocol extends GroupPredictionProtocol {

    public static final Parameter numFolds = new Parameter("c", new IntegerParameter(2, Integer.MAX_VALUE, 5));

    public CrossFoldPredictionProtocol() {
        super();

        addParameter(numFolds);
    }

    public CrossFoldPredictionProtocol(int c) {
        this();
        setParameterValue(numFolds, c);
    }

    private Collection<Long> getRatedItems(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers group) throws CannotLoadRatingsDataset, UserNotFound {
        return DatasetUtilities.getMembersRatings_byItem(group, datasetLoader).keySet();
    }

    @Override
    public Collection<GroupRecommendationRequest> getGroupRecommendationRequests(DatasetLoader<? extends Rating> trainDatasetLoader, DatasetLoader<? extends Rating> testDatasetLoader, GroupOfUsers group) throws CannotLoadRatingsDataset, UserNotFound {

        Random random = new Random(getSeedValue());

        ArrayList<Set<Long>> crossFoldValidations = new ArrayList<>();
        Collection<Long> items = getRatedItems(testDatasetLoader, group);

        for (int i = 0; i < getNumPartitions(); i++) {
            crossFoldValidations.add(new TreeSet<>());
        }
        int n = 0;
        while (!items.isEmpty()) {
            long idItem = items.toArray(new Long[0])[random.nextInt(items.size())];
            items.remove(idItem);
            int partition = n % getNumPartitions();
            crossFoldValidations.get(partition).add(idItem);
            n++;
        }

        Collection<Long> ratedItems = getRatedItems(testDatasetLoader, group);

        ArrayList<GroupRecommendationRequest> groupRecommendationRequests = new ArrayList<>(ratedItems.size());

        for (Set<Long> itemsInThisFold : crossFoldValidations) {

            Map<Long, Map<Long, Number>> membersRatings_byItem = DatasetUtilities.getMembersRatings_byItem(group, testDatasetLoader);

            itemsInThisFold.stream().forEach((idItem) -> membersRatings_byItem.remove(idItem));

            Map<Long, Map<Long, Number>> predictionMembersRatings_byUser = DatasetUtilities.transformIndexedByItemToIndexedByUser_Map(membersRatings_byItem);
            Map<Long, Map<Long, Rating>> predictionMembersRatings_byUser_Rating = DatasetUtilities.getMapOfMaps_Rating(predictionMembersRatings_byUser);
            DatasetLoader<Rating> predictionPhaseDatasetLoader
                    = new DatasetLoaderGivenRatingsDataset<>(trainDatasetLoader,
                            RatingsDatasetOverwrite.createRatingsDataset(
                                    (RatingsDataset<Rating>) trainDatasetLoader.getRatingsDataset(),
                                    predictionMembersRatings_byUser_Rating));

            groupRecommendationRequests.add(new GroupRecommendationRequest(group, predictionPhaseDatasetLoader,
                    itemsInThisFold.stream().map(idItem -> trainDatasetLoader.getContentDataset().get(idItem)).collect(Collectors.toSet()))
            );

        }
        return groupRecommendationRequests;
    }

    protected int getNumPartitions() {
        return (Integer) getParameterValue(numFolds);
    }
}
