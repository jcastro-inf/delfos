/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delfos.rs.collaborativefiltering.knn.memorybased;

import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.predictiontechniques.WeightedSum;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.similaritymeasures.CosineCoefficient;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class KnnMemoryNeighborCalculatorTest {

    public KnnMemoryNeighborCalculatorTest() {
    }

    @Test
    public void testCosineSimilarityML100kUser1User2() {
        KnnMemoryBasedCFRS knnMemory = new KnnMemoryBasedCFRS(new CosineCoefficient(), null, null, false, 1, 20, new WeightedSum());

        DatasetLoader ml100k = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        UsersDataset usersDataset = ((UsersDatasetLoader) ml100k).getUsersDataset();
        RatingsDataset ratingsDataset = ml100k.getRatingsDataset();
        ContentDataset contentDataset = ((ContentDatasetLoader) ml100k).getContentDataset();

        User user = usersDataset.getUser(1);
        User neighborUser = usersDataset.getUser(2);

        KnnMemoryNeighborTask task = new KnnMemoryNeighborTask(ml100k, user, neighborUser, knnMemory);

        KnnMemoryNeighborCalculator executor = new KnnMemoryNeighborCalculator();

        Neighbor neighbor = executor.apply(task);
        Neighbor expectedNeighbor = new Neighbor(RecommendationEntity.USER, neighborUser, 0.9605819);

        Assert.assertEquals("Neighbors are not equals", expectedNeighbor, neighbor);
    }

    @Test
    public void testPearsonSimilarityML100kUser1User2() {
        KnnMemoryBasedCFRS knnMemory = new KnnMemoryBasedCFRS(new PearsonCorrelationCoefficient(), null, null, false, 1, 20, new WeightedSum());

        DatasetLoader ml100k = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        UsersDataset usersDataset = ((UsersDatasetLoader) ml100k).getUsersDataset();
        RatingsDataset ratingsDataset = ml100k.getRatingsDataset();
        ContentDataset contentDataset = ((ContentDatasetLoader) ml100k).getContentDataset();

        User user = usersDataset.getUser(1);
        User neighborUser = usersDataset.getUser(2);

        KnnMemoryNeighborTask task = new KnnMemoryNeighborTask(ml100k, user, neighborUser, knnMemory);

        KnnMemoryNeighborCalculator executor = new KnnMemoryNeighborCalculator();

        Neighbor neighbor = executor.apply(task);
        Neighbor expectedNeighbor = new Neighbor(RecommendationEntity.USER, neighborUser, 0.16084123);

        Assert.assertEquals("Neighbors are not equals", expectedNeighbor, neighbor);
    }

}
