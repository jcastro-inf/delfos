package delfos.group.grs.hesitant;

import delfos.common.parallelwork.SingleTaskExecute;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import es.jcastro.hesitant.HesitantValuation;
import es.jcastro.hesitant.similarity.HesitantSimilarity;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public final class HesitantKnnNeighborSimilarityTaskExecutor implements SingleTaskExecute<HesitantKnnNeighborSimilarityTask> {

    public HesitantKnnNeighborSimilarityTaskExecutor() {
        super();
    }

    @Override
    public void executeSingleTask(HesitantKnnNeighborSimilarityTask task) {
        User neighborUser = task.neighborUser;

        DatasetLoader<? extends Rating> datasetLoader = task.datasetLoader;
        HesitantValuation<Item, Double> hesitantGroupModel = task.groupModel;
        HesitantValuation<Item, Double> neighborProfile
                = HesitantKnnGroupUser.getHesitantProfile(datasetLoader, Arrays.asList(neighborUser));
        HesitantSimilarity hesitantSimilarity = task.hesitantSimilarity;

        Set<Item> intersection = new TreeSet<>();
        intersection.addAll(hesitantGroupModel.getTerms());
        intersection.retainAll(neighborProfile.getTerms());

        double sim = hesitantSimilarity.similarity(
                hesitantGroupModel.select(intersection),
                neighborProfile.select(intersection));

        Neighbor neighbor = new Neighbor(RecommendationEntity.USER, neighborUser, sim);
        task.setNeighbor(neighbor);
    }
}
