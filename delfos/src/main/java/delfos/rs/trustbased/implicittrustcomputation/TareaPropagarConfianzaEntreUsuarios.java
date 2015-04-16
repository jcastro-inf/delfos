package delfos.rs.trustbased.implicittrustcomputation;

import java.util.Map;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;

/**
 * Guarda los datos necesarios para propagar la confianza entre dos usuarios que
 * no tienen definida una confianza directamente.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 04-sep-2013
 */
public class TareaPropagarConfianzaEntreUsuarios {

    public final int idSourceUser;
    public final int idTargetUser;
    public final Map<Integer, Map<Integer, Number>> usersTrust;
    public final RatingsDataset<? extends Rating> ratingsDataset;
    private Double propagatedTrust = null;

    public TareaPropagarConfianzaEntreUsuarios(int idSourceUser, int idTargetUser, Map<Integer, Map<Integer, Number>> usersTrust, RatingsDataset<? extends Rating> ratingsDataset) {
        this.idSourceUser = idSourceUser;
        this.idTargetUser = idTargetUser;
        this.usersTrust = usersTrust;
        this.ratingsDataset = ratingsDataset;
    }

    public Double getPropagatedTrust() {
        return propagatedTrust;
    }

    public void setPropagatedTrust(Double propagatedTrust) {
        this.propagatedTrust = propagatedTrust;
    }
}
