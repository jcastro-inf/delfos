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
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 04-sep-2013
 */
public class EjecutadorPropagacionConfianzaEntreDosUsuarios implements SingleTaskExecute<TareaPropagarConfianzaEntreUsuarios> {

    @Override
    public void executeSingleTask(TareaPropagarConfianzaEntreUsuarios task) {
        final Map<Integer, Map<Integer, Number>> usersTrust = task.usersTrust;
        final int idSourceUser = task.idSourceUser;
        final int idTargetUser = task.idTargetUser;
        final RatingsDataset<? extends Rating> ratingsDataset = task.ratingsDataset;

        if (usersTrust.get(idSourceUser).containsKey(idTargetUser)) {
            //Los usuarios ya son adyacentes, no es necesario propagar.
        } else {
            //Propagate trust to complete this connection.
            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage("Propagating trust between user " + idSourceUser + " and user " + idTargetUser + "\n");
            }

            Set<Integer> adyacentesAAmbos = new TreeSet<Integer>(usersTrust.get(idSourceUser).keySet());
            adyacentesAAmbos.retainAll(usersTrust.get(idTargetUser).keySet());

            if (adyacentesAAmbos.isEmpty()) {
                //No tienen ningún adyacente en común, por lo que no se puede propagar, hay que contar este enlace para el porcentaje de progreso.
            } else {
                double numerador = 0;
                double denominador = 0;
                for (int idIntermediateUser : adyacentesAAmbos) {
                    double usersTrustAB = usersTrust.get(idSourceUser).get(idIntermediateUser).doubleValue();
                    double usersTrustBC = usersTrust.get(idIntermediateUser).get(idTargetUser).doubleValue();
                    int numCommonAB;
                    try {
                        TreeSet<Integer> commonAB = new TreeSet<Integer>(ratingsDataset.getUserRated(idSourceUser));
                        commonAB.retainAll(ratingsDataset.getUserRated(idIntermediateUser));
                        numCommonAB = commonAB.size();
                    } catch (UserNotFound ex) {
                        numCommonAB = 0;
                        ERROR_CODES.USER_NOT_FOUND.exit(ex);
                    }

                    int numCommonBC;
                    try {
                        TreeSet<Integer> commonBC = new TreeSet<Integer>(ratingsDataset.getUserRated(idIntermediateUser));
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
