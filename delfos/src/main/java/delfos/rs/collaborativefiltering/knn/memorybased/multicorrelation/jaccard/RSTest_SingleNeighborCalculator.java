package delfos.rs.collaborativefiltering.knn.memorybased.multicorrelation.jaccard;

import delfos.ERROR_CODES;
import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.SingleTaskExecute;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.similaritymeasures.useruser.UserUserSimilarity;

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
        UserUserSimilarity userUserSimilarity = (UserUserSimilarity) rs.getParameterValue(RSTest.SIMILARITY_MEASURE);

        double similarity;
        try {
            similarity = userUserSimilarity.similarity(task.datasetLoader, idUser, idNeighbor);
            Neighbor neighbor = new Neighbor(RecommendationEntity.USER, idNeighbor, similarity);
            task.setNeighbor(neighbor);
        } catch (UserNotFound ex) {
            ERROR_CODES.USER_NOT_FOUND.exit(ex);
        } catch (CouldNotComputeSimilarity ex) {

        }

    }
}
