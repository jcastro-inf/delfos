package delfos.rs.collaborativefiltering.knn.modelbased.nwr;

import java.util.Collections;
import java.util.List;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.collaborativefiltering.profile.Neighbor;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 19-Noviembre-2013
 */
public class KnnModelBasedCBRS_NeighborsWithRatingsTask {

    public final int idItem;
    private KnnModelBased_NWR rs;
    private RatingsDataset<? extends Rating> ratingsDataset;
    private List<Neighbor> neighbors = null;

    public KnnModelBasedCBRS_NeighborsWithRatingsTask(int idItem, KnnModelBased_NWR rs, RatingsDataset<? extends Rating> ratingsDataset) {
        this.idItem = idItem;
        this.rs = rs;
        this.ratingsDataset = ratingsDataset;
    }

    public int getIdItem() {
        return idItem;
    }

    public KnnModelBased_NWR getRecommenderSystem() {
        return rs;
    }

    public RatingsDataset<? extends Rating> getRatingsDataset() {
        return ratingsDataset;
    }

    public void setNeighbors(List<Neighbor> neighbors) {
        rs = null;
        ratingsDataset = null;
        this.neighbors = neighbors;
    }

    public List<Neighbor> getNeighbors() {
        return Collections.unmodifiableList(neighbors);
    }
}
