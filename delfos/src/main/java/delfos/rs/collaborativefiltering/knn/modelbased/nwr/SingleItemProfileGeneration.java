package delfos.rs.collaborativefiltering.knn.modelbased.nwr;

import java.util.List;
import java.util.Map;
import delfos.dataset.basic.rating.Rating;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.Global;
import delfos.common.parallelwork.SingleTaskExecute;

public final class SingleItemProfileGeneration implements SingleTaskExecute<KnnModelBasedCBRSTask> {

    public SingleItemProfileGeneration() {
        super();
    }

    @Override
    public void executeSingleTask(KnnModelBasedCBRSTask knnModelTask) {
        int idItem = knnModelTask.getIdItem();
        KnnModelBased_NWR rs = knnModelTask.getRecommenderSystem();

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
