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
package delfos.group.grs.mean;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.MultiThreadExecutionManager;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.experiment.casestudy.ExecutionProgressListener;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.rs.nonpersonalised.meanrating.arithmeticmean.MeanRating;
import delfos.rs.nonpersonalised.meanrating.arithmeticmean.MeanRatingRSModel;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Recomendador para grupos de usuarios que recomienda los productos con mejor
 * valoración media. No se recomienda usar este algoritmo en un sistema real
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 9-Junio-2013
 */
public class MeanRatingGRS extends GroupRecommenderSystemAdapter<MeanRatingRSModel, GroupOfUsers> {

    private static final long serialVersionUID = 1L;

    public MeanRatingGRS() {
    }

    @Override
    public MeanRatingRSModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {

        Set<Integer> allItems = new TreeSet(datasetLoader.getRatingsDataset().allRatedItems());
        List<MeanRating> meanRatings = new ArrayList<>(allItems.size());

        List<MeanRatingTask> tasks = new ArrayList<>(allItems.size());
        for (int idItem : allItems) {
            tasks.add(new MeanRatingTask(datasetLoader.getRatingsDataset(), idItem));
        }
        MultiThreadExecutionManager<MeanRatingTask> multiThreadExecutionManager = new MultiThreadExecutionManager<>(
                "Building mean rating profile",
                tasks,
                MeanRatingSingleExecution.class);
        multiThreadExecutionManager.addExecutionProgressListener(new ExecutionProgressListener() {
            @Override
            public void executionProgressChanged(String proceso, int percent, long remainingMiliSeconds) {
                fireBuildingProgressChangedEvent(proceso, percent, remainingMiliSeconds);
            }
        });
        multiThreadExecutionManager.run();
        for (MeanRatingTask finished : multiThreadExecutionManager.getAllFinishedTasks()) {
            meanRatings.add(finished.getMeanRating());
        }
        Collections.sort(meanRatings);
        return new MeanRatingRSModel(meanRatings);
    }

    @Override
    public GroupOfUsers buildGroupModel(DatasetLoader<? extends Rating> datasetLoader, MeanRatingRSModel RecommendationModel, GroupOfUsers groupOfUsers) throws UserNotFound {
        return new GroupOfUsers(groupOfUsers.getIdMembers());
    }

    @Override
    public Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, MeanRatingRSModel RecommendationModel, GroupOfUsers groupModel, GroupOfUsers groupOfUsers, java.util.Set<Integer> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadRatingsDataset {
        List<MeanRating> media = RecommendationModel.getRangedMeanRatings();
        Collection<Recommendation> recommendationList = new ArrayList<>(candidateItems.size());
        for (MeanRating meanRating : media) {
            if (candidateItems.contains(meanRating.getIdItem())) {
                double ratingMedio = meanRating.getPreference().doubleValue();
                recommendationList.add(new Recommendation(meanRating.getIdItem(), ratingMedio));
            }
        }

        return recommendationList;
    }

    @Override
    public boolean isRatingPredictorRS() {
        return true;
    }
}
