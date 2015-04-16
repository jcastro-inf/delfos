package delfos.rs.trustbased.similaritymodification;

import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.Task;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.collaborativefiltering.profile.Neighbor;

/**
 * Clase que almacena los datos necesarios para ejecutar paralelamente el
 * cálculo de la similitud con un vecino.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 14-abril-2014
 */
public class NeighborCalculationTask extends Task {

    public final int idUser;
    public final int idNeighbor;
    public TrustModificationKnnMemory rs;
    public RatingsDataset<? extends Rating> ratingsDataset;
    public Neighbor neighbor = null;

    public NeighborCalculationTask(RatingsDataset<? extends Rating> ratingsDataset, int idUser, int idNeighbor, TrustModificationKnnMemory rs) throws UserNotFound {
        this.ratingsDataset = ratingsDataset;
        this.idUser = idUser;
        this.idNeighbor = idNeighbor;
        this.rs = rs;
        try {
            ratingsDataset.getUserRated(idUser);
            ratingsDataset.getUserRated(idNeighbor);
        } catch (UserNotFound ex) {
            ratingsDataset.getUserRated(idUser);
            ratingsDataset.getUserRated(idNeighbor);
            throw ex;
        }
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
        ratingsDataset = null;
    }

    public Neighbor getNeighbor() {
        return neighbor;
    }
}
