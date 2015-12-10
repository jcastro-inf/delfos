package delfos.group.grs.hesitant;

import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.utils.hesitant.HesitantValuation;
import delfos.utils.hesitant.similarity.HesitantSimilarity;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

public final class HesitantKnnNeighborSimilarityFunction implements Function<HesitantKnnNeighborSimilarityTask, Neighbor> {

    public HesitantKnnNeighborSimilarityFunction() {
        super();
    }

    @Override
    public Neighbor apply(HesitantKnnNeighborSimilarityTask task) {
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
        return neighbor;
    }
}
