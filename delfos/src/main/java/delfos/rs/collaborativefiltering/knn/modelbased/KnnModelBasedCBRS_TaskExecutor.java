package delfos.rs.collaborativefiltering.knn.modelbased;

import java.util.List;
import java.util.Map;
import delfos.dataset.basic.rating.Rating;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.Global;
import delfos.common.parallelwork.SingleTaskExecute;

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
            Map<Integer, ? extends Rating> get = knnModelTask.getRatingsDataset().getItemRatingsRated(idItem);
            if (!get.isEmpty()) {
                List<Neighbor> neighbors = rs.getNeighbors(knnModelTask.getRatingsDataset(), idItem);
                knnModelTask.setNeighbors(neighbors);
            }
        } catch (ItemNotFound ex) {
            Global.showWarning("Item " + idItem + " has no ratings.");
        }
    }
}
