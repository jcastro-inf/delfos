package delfos.rs.collaborativefiltering.knn.modelbased;

import delfos.common.parallelwork.SingleTaskExecute;
import delfos.dataset.basic.item.Item;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import java.util.List;

public final class KnnModelBasedCBRS_TaskExecutor implements SingleTaskExecute<KnnModelBasedCBRS_Task> {

    public KnnModelBasedCBRS_TaskExecutor() {
        super();
    }

    @Override
    public void executeSingleTask(KnnModelBasedCBRS_Task knnModelTask) {
        Item item = knnModelTask.getItem();
        KnnModelBasedCFRS rs = knnModelTask.getRecommenderSystem();

        List<Neighbor> neighbors = rs.getNeighbors(knnModelTask.getDatasetLoader(), item);
        knnModelTask.setNeighbors(neighbors);
    }
}
