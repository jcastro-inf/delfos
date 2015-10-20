package delfos.rs.collaborativefiltering.knn.modelbased;

import delfos.common.parallelwork.Task;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 19-Noviembre-2013
 */
public class KnnModelBasedCBRS_Task extends Task {

    public final Item item;
    private KnnModelBasedCFRS rs;
    private DatasetLoader<? extends Rating> datasetLoader;
    private List<Neighbor> neighbors = null;

    public KnnModelBasedCBRS_Task(Item item, KnnModelBasedCFRS rs, DatasetLoader<? extends Rating> datasetLoader) {
        this.item = item;
        this.rs = rs;
        this.datasetLoader = datasetLoader;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("idItem ---------> ").append(item).append("\n");
        str.append("rs -------------> ").append(rs.getAlias()).append("\n");
        str.append("\n").append(rs.getNameWithParameters());

        return str.toString();
    }

    public Item getItem() {
        return item;
    }

    public KnnModelBasedCFRS getRecommenderSystem() {
        return rs;
    }

    public DatasetLoader<? extends Rating> getDatasetLoader() {
        return datasetLoader;
    }

    public void setNeighbors(List<Neighbor> neighbors) {
        this.neighbors = neighbors;
    }

    public List<Neighbor> getNeighbors() {
        return Collections.unmodifiableList(neighbors);
    }
}
