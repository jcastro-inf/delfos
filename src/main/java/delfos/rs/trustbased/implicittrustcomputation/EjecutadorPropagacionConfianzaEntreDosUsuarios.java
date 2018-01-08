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
import java.util.Set;
import java.util.TreeSet;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.Global;
import delfos.common.parallelwork.SingleTaskExecute;

/**
 * Ejecuta la propagación de confianza entre dos usuarios dados.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 04-sep-2013
 */
public class EjecutadorPropagacionConfianzaEntreDosUsuarios implements SingleTaskExecute<TareaPropagarConfianzaEntreUsuarios> {

    @Override
    public void executeSingleTask(TareaPropagarConfianzaEntreUsuarios task) {
        final Map<Long, Map<Long, Number>> usersTrust = task.usersTrust;
        final long idSourceUser = task.idSourceUser;
        final long idTargetUser = task.idTargetUser;
        final RatingsDataset<? extends Rating> ratingsDataset = task.ratingsDataset;

        if (usersTrust.get(idSourceUser).containsKey(idTargetUser)) {
            //Los usuarios ya son adyacentes, no es necesario propagar.
        } else {
            //Propagate trust to complete this connection.
            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage("Propagating trust between user " + idSourceUser + " and user " + idTargetUser + "\n");
            }

            Set<Long> adyacentesAAmbos = new TreeSet<Long>(usersTrust.get(idSourceUser).keySet());
            adyacentesAAmbos.retainAll(usersTrust.get(idTargetUser).keySet());

            if (adyacentesAAmbos.isEmpty()) {
                //No tienen ningún adyacente en común, por lo que no se puede propagar, hay que contar este enlace para el porcentaje de progreso.
            } else {
                double numerador = 0;
                double denominador = 0;
                for (long idIntermediateUser : adyacentesAAmbos) {
                    double usersTrustAB = usersTrust.get(idSourceUser).get(idIntermediateUser).doubleValue();
                    double usersTrustBC = usersTrust.get(idIntermediateUser).get(idTargetUser).doubleValue();
                    int numCommonAB;
                    try {
                        TreeSet<Long> commonAB = new TreeSet<Long>(ratingsDataset.getUserRated(idSourceUser));
                        commonAB.retainAll(ratingsDataset.getUserRated(idIntermediateUser));
                        numCommonAB = commonAB.size();
                    } catch (UserNotFound ex) {
                        numCommonAB = 0;
                        ERROR_CODES.USER_NOT_FOUND.exit(ex);
                    }

                    int numCommonBC;
                    try {
                        TreeSet<Long> commonBC = new TreeSet<Long>(ratingsDataset.getUserRated(idIntermediateUser));
                        commonBC.retainAll(ratingsDataset.getUserRated(idTargetUser));
                        numCommonBC = commonBC.size();
                    } catch (UserNotFound ex) {
                        numCommonBC = 0;
                        ERROR_CODES.USER_NOT_FOUND.exit(ex);
                    }

                    //Efectivamente, numCommonAB y/o numCommonBC pueden ser cero si la confianza viene de haber sido propagada previamente (Esto ya no se aplica ya que la confianza propagada no se tiene en cuenta para propagar otros valores).
                    double contribucionNumerador = numCommonAB * usersTrustAB + numCommonBC * usersTrustBC;
                    double contribucionDenominador = numCommonAB + numCommonBC;
                    numerador += contribucionNumerador;
                    denominador += contribucionDenominador;
                }
                double PTrustAB = numerador / denominador;
                task.setPropagatedTrust(PTrustAB);
            }
        }
    }
}
