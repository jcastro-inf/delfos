package delfos.rs.collaborativefiltering.knn.memorybased.multicorrelation.jaccard;

import delfos.common.parallelwork.SingleTaskExecute;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import delfos.similaritymeasures.useruser.UserUserSimilarity;
import delfos.similaritymeasures.useruser.UserUserSimilarityWrapper;

public final class RSTest_SingleNeighborCalculator implements SingleTaskExecute<RSTest_Task> {

    public RSTest_SingleNeighborCalculator() {
        super();
    }

    @Override
    public void executeSingleTask(RSTest_Task task) {
        int idUser = task.idUser;
        int idNeighbor = task.idNeighbor;
        RSTest rs = task.rs;

        if (idUser == idNeighbor) {
            return;
        }
        UserUserSimilarity userUserSimilarity = (UserUserSimilarity) new UserUserSimilarityWrapper(new PearsonCorrelationCoefficient());

        double similarity = userUserSimilarity.similarity(task.datasetLoader, idUser, idNeighbor);
        Neighbor neighbor = new Neighbor(RecommendationEntity.USER, idNeighbor, similarity);
        task.setNeighbor(neighbor);

    }
}
