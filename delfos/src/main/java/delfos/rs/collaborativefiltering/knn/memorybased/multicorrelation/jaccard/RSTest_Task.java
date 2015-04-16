package delfos.rs.collaborativefiltering.knn.memorybased.multicorrelation.jaccard;

import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.Task;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.rs.collaborativefiltering.profile.Neighbor;

/**
 * Clase que almacena los datos necesarios para ejecutar paralelamente el
 * cálculo de la similitud con un vecino.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 14-Noviembre-2013
 */
public class RSTest_Task extends Task {

    public final int idUser;
    public final int idNeighbor;
    public RSTest rs;
    public DatasetLoader<? extends Rating> datasetLoader;
    public Neighbor neighbor = null;

    public RSTest_Task(DatasetLoader<? extends Rating> datasetLoader, int idUser, int idNeighbor, RSTest rs) throws UserNotFound {

        this.datasetLoader = datasetLoader;
        this.idUser = idUser;
        this.idNeighbor = idNeighbor;
        this.rs = rs;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("idUser ---------> ").append(idUser).append("\n");
        str.append("idNeighbor -----> ").append(idNeighbor).append("\n");
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
