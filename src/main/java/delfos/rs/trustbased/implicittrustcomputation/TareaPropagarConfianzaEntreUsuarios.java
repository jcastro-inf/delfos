/* 
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package delfos.rs.trustbased.implicittrustcomputation;

import java.util.Map;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;

/**
 * Guarda los datos necesarios para propagar la confianza entre dos usuarios que
 * no tienen definida una confianza directamente.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 04-sep-2013
 */
public class TareaPropagarConfianzaEntreUsuarios {

    public final long idSourceUser;
    public final long idTargetUser;
    public final Map<Long, Map<Long, Number>> usersTrust;
    public final RatingsDataset<? extends Rating> ratingsDataset;
    private Double propagatedTrust = null;

    public TareaPropagarConfianzaEntreUsuarios(
            long idSourceUser,
            long idTargetUser,
            Map<Long, Map<Long, Number>> usersTrust,
            RatingsDataset<? extends Rating> ratingsDataset) {
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
