package delfos.rs.collaborativefiltering.knn.memorybased.multicorrelation;

import delfos.ERROR_CODES;
import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.SingleTaskExecute;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.similaritymeasures.useruser.UserUserSimilarity;

public final class KnnMultiCorrelation_SingleNeighborCalculator implements SingleTaskExecute<KnnMultiCorrelation_Task> {

    public KnnMultiCorrelation_SingleNeighborCalculator() {
        super();
    }

    @Override
    public void executeSingleTask(KnnMultiCorrelation_Task task) {
        int idUser = task.idUser;
        int idNeighbor = task.idNeighbor;
        KnnMultiCorrelation rs = task.rs;
        RatingsDataset<? extends Rating> ratingsDataset = task.datasetLoader.getRatingsDataset();

        if (idUser == idNeighbor) {
            return;
        }
        UserUserSimilarity userUserSimilarity = (UserUserSimilarity) rs.getParameterValue(KnnMultiCorrelation.MULTI_CORRELATION_SIMILARITY_MEASURE);

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
