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
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 9-Junio-2013
 */
public class MeanRatingGRS extends GroupRecommenderSystemAdapter<MeanRatingRSModel, GroupOfUsers> {

    private static final long serialVersionUID = 1L;

    public MeanRatingGRS() {
    }

    @Override
    public MeanRatingRSModel build(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {

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
        return new GroupOfUsers(groupOfUsers.getGroupMembers());
    }

    @Override
    public Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, MeanRatingRSModel RecommendationModel, GroupOfUsers groupModel, GroupOfUsers groupOfUsers, java.util.Set<Integer> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadRatingsDataset {
        List<MeanRating> media = RecommendationModel.getRangedMeanRatings();
        Collection<Recommendation> recommendationList = new ArrayList<>(candidateItems.size());
        for (MeanRating meanRating : media) {
            if (candidateItems.contains(meanRating.getIdItem())) {
                float ratingMedio = meanRating.getPreference().floatValue();
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
