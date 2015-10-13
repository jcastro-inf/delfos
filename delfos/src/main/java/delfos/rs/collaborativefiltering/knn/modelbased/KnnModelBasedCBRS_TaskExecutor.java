package delfos.rs.collaborativefiltering.knn.modelbased;

import delfos.common.Global;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.parallelwork.SingleTaskExecute;
import delfos.dataset.basic.rating.Rating;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import java.util.List;
import java.util.Map;

public final class KnnModelBasedCBRS_TaskExecutor implements SingleTaskExecute<KnnModelBasedCBRS_Task> {

    public KnnModelBasedCBRS_TaskExecutor() {
        super();
    }

    @Override
    public void executeSingleTask(KnnModelBasedCBRS_Task knnModelTask) {
        int idItem = knnModelTask.getIdItem();
        KnnModelBasedCFRS rs = knnModelTask.getRecommenderSystem();

        //Calculo el perfil y lo guardo
        try {
            Map<Integer, ? extends Rating> get = knnModelTask.getDatasetLoader()
                    .getRatingsDataset().getItemRatingsRated(idItem);
            if (!get.isEmpty()) {
                List<Neighbor> neighbors = rs.getNeighbors(knnModelTask.getDatasetLoader(), idItem);
                knnModelTask.setNeighbors(neighbors);
            }
        } catch (ItemNotFound ex) {
            Global.showWarning("Item " + idItem + " has no ratings.");
        }
    }
}
