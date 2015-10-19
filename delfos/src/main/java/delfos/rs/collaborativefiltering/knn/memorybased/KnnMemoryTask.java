package delfos.rs.collaborativefiltering.knn.memorybased;

import delfos.common.parallelwork.Task;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.rs.collaborativefiltering.profile.Neighbor;

/**
 * Clase que almacena los datos necesarios para ejecutar paralelamente el
 * cálculo de la similitud con un vecino.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 14-Noviembre-2013
 */
public class KnnMemoryTask extends Task {

    public final User user;
    public final User neighborUser;
    public KnnMemoryBasedCFRS rs;
    public DatasetLoader<? extends Rating> datasetLoader;
    public Neighbor neighbor = null;

    public KnnMemoryTask(DatasetLoader<? extends Rating> datasetLoader, User user, User neighborUser, KnnMemoryBasedCFRS rs) {
        this.datasetLoader = datasetLoader;
        this.user = user;
        this.neighborUser = neighborUser;
        this.rs = rs;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("idUser ---------> ").append(user.getId()).append("(").append(user.getName()).append("\n");
        str.append("idNeighbor -----> ").append(neighborUser.getId()).append("(").append(neighborUser.getName()).append("\n");
        str.append("rs -------------> ").append(rs.getAlias()).append("\n");
        str.append("\n").append(rs.getNameWithParameters());

        return str.toString();
    }

    public void setNeighbor(Neighbor neighbor) {
        this.neighbor = neighbor;
        rs = null;
        datasetLoader = null;
    }

    public Neighbor getNeighbor() {
        return neighbor;
    }
}
