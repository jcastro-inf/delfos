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

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.util.DatasetPrinterDeprecated;
import delfos.ERROR_CODES;
import delfos.rs.trustbased.WeightedGraph;
import delfos.rs.trustbased.WeightedGraphCalculation;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.Global;

/**
 * Algoritmo para calcular redes de confianza entre usuarios, utilizando las
 * valoraciones. Este método es el propuesto en:
 *
 * <p>
 * <p>
 * Qusai Shambour, Jie Lu: An Effective Recommender System by Unifying User and
 * Item Trust Information for B2B Applications.
 *
 * <p>
 * <p>
 * CORRECCIÓN DEL ALGORITMO: El cálculo de la User-Based Trust se hace de la
 * siguiente manera:
 * <p>
 * 1.- Predecir las valoraciones con el usuario vecino.
 * <p>
 * 2.- Normalizar las predicciones ¡Por filas!, es decir, las predicciones se
 * normalizan a [0,1] con Max-Min, usando la valoración máxima y mínima. Tras
 * esto se normalizan las predicciones con Max-Min [0,1] usando la predicción
 * máxima y mínima. Si el maximo y minimo son iguales se asigna 1 a todos los
 * valores a normalizar.
 * <p>
 * 3.- Cálculo del MSD y Jaccard tal cual estaba.
 * <p>
 * 4.- UBTrust = MSD*Jaccard.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 03-May-2013
 * @version 1.01 29-Agosto-2013
 */
public class ShambourLu_UserBasedImplicitTrustComputation extends WeightedGraphCalculation<Integer> {

    private static final long serialVersionUID = 1L;
    private final boolean propagate;

    public ShambourLu_UserBasedImplicitTrustComputation() {
        propagate = false;
    }

    public ShambourLu_UserBasedImplicitTrustComputation(boolean propagate) {
        this.propagate = propagate;
    }

    /**
     * Calcula la red de confianza entre los usuarios especificados utilizando
     * las valoraciones indicadas.
     *
     * @param datasetLoader Dataset del que se extrae la confianza.
     * @param users Usuarios para los que se calcula la red de confianza.
     * @return
     */
    @Override
    public WeightedGraph<Integer> computeTrustValues(DatasetLoader<? extends Rating> datasetLoader, Collection<Integer> users) throws CannotLoadRatingsDataset {
        boolean printPartialResults;

        final RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        printPartialResults = ratingsDataset.allRatedItems().size() <= 100 && ratingsDataset.allUsers().size() <= 28;

        if (!Global.isInfoPrinted()) {
            printPartialResults = false;
        }

        if (printPartialResults) {
            Global.showInfoMessage("Dataset de training \n");
            DatasetPrinterDeprecated.printCompactRatingTable(ratingsDataset);
        }

        int numTrustValuesThatNeedToBePropagated = 0;

        Map<Integer, Map<Integer, Number>> MSD = new TreeMap<Integer, Map<Integer, Number>>();
        Map<Integer, Map<Integer, Number>> UJaccard = new TreeMap<Integer, Map<Integer, Number>>();
        {
            int i = 1;
            for (int idUser : users) {
                MSD.put(idUser, new TreeMap<Integer, Number>());
                UJaccard.put(idUser, new TreeMap<Integer, Number>());

                Map<Integer, ? extends Rating> userRatings;
                try {
                    userRatings = ratingsDataset.getUserRatingsRated(idUser);
                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                    return null;
                }

                //Para cada vecino calculo el MSD
                for (int idUserNeighbor : users) {
                    try {
                        double meanUser = ratingsDataset.getMeanRatingUser(idUser);
                        double meanUserNeighbour = ratingsDataset.getMeanRatingUser(idUserNeighbor);

                        Map<Integer, ? extends Rating> userNeighbourRatings = ratingsDataset.getUserRatingsRated(idUserNeighbor);

                        Set<Integer> commonItems = new TreeSet<Integer>(userRatings.keySet());
                        commonItems.retainAll(userNeighbourRatings.keySet());
                        if (commonItems.isEmpty()) {
                            numTrustValuesThatNeedToBePropagated++;
                            continue;
                        }
                        double[] ratings = new double[commonItems.size()];
                        double[] predictions = new double[commonItems.size()];

                        //Compute predictions. At the same time, compute mand min of rating and prediction, to save cycles later on.
                        double ratingMax = -Double.MAX_VALUE;
                        double ratingMin = Double.MAX_VALUE;
                        double predictionMax = -Double.MAX_VALUE;
                        double predictionMin = Double.MAX_VALUE;

                        {
                            int index = 0;
                            for (int idItem : commonItems) {
                                double rating = userRatings.get(idItem).getRatingValue().doubleValue();
                                ratings[index] = rating;
                                double prediction = meanUser + userNeighbourRatings.get(idItem).getRatingValue().doubleValue() - meanUserNeighbour;
                                predictions[index] = prediction;
                                index++;

                                ratingMax = Math.max(ratingMax, rating);
                                ratingMin = Math.min(ratingMin, rating);
                                predictionMax = Math.max(predictionMax, prediction);
                                predictionMin = Math.min(predictionMin, prediction);
                            }
                        }

                        //Ratings MaxMin normalisation
                        if (ratingMax == ratingMin) {
                            for (int index = 0; index < ratings.length; index++) {
                                ratings[index] = 1;
                            }
                        } else {
                            for (int index = 0; index < ratings.length; index++) {
                                ratings[index] = (ratings[index] - ratingMin) / (ratingMax - ratingMin);
                            }
                        }

                        //Predictions MaxMin normalisation
                        if (predictionMax == predictionMin) {
                            for (int index = 0; index < predictions.length; index++) {
                                predictions[index] = 1;
                            }
                        } else {
                            for (int index = 0; index < ratings.length; index++) {
                                predictions[index] = (predictions[index] - predictionMin) / (predictionMax - predictionMin);
                            }
                        }

                        //Calculo el MSD
                        double sumOfError = 0;
                        for (int index = 0; index < ratings.length; index++) {
                            sumOfError += Math.pow(ratings[index] - predictions[index], 2);
                        }

                        double MSD_of_pair = 1 - (sumOfError / commonItems.size());
                        MSD.get(idUser).put(idUserNeighbor, MSD_of_pair);

                        //Calculo el Jaccard
                        double numUserRatings = userRatings.size();
                        double numUserNeighborRatings = userNeighbourRatings.size();
                        double numCommon = commonItems.size();
                        double jaccard_of_pair = numCommon / (numUserRatings + numUserNeighborRatings - commonItems.size());
                        UJaccard.get(idUser).put(idUserNeighbor, jaccard_of_pair);
                    } catch (UserNotFound ex) {
                        ERROR_CODES.USER_NOT_FOUND.exit(ex);
                    }
                }

                fireProgressChanged("User-Based trust", (int) ((i * 100.0f) / users.size()), -1);

                if (Global.isVerboseAnnoying()) {
                    Global.showInfoMessage(new Date().toString() + ": User-based trust " + ((i * 100.0f) / users.size()) + "%\n");
                }
                i++;
            }
        }
        if (printPartialResults) {
            Global.showInfoMessage("MSD table \n");
            DatasetPrinterDeprecated.printCompactUserUserTable(MSD, users);

            Global.showInfoMessage("============================================================================= \n");
            Global.showInfoMessage("UT-Step1 0.4: Jaccard\n");
            Global.showInfoMessage("============================================================================= \n");

            Global.showInfoMessage("Jaccard table \n");
            DatasetPrinterDeprecated.printCompactUserUserTable(UJaccard, users);

            Global.showInfoMessage("============================================================================= \n");
            Global.showInfoMessage("UT-Step1 - A: Trust derivation\n");
            Global.showInfoMessage("============================================================================= \n");
        }

        TreeMap<Integer, Map<Integer, Number>> usersTrust = new TreeMap<Integer, Map<Integer, Number>>();
        for (int idUser : users) {
            usersTrust.put(idUser, new TreeMap<Integer, Number>());

            for (int idUserNeighbour : users) {

                if (!UJaccard.containsKey(idUser) || !UJaccard.get(idUser).containsKey(idUserNeighbour) || !MSD.containsKey(idUser) || !MSD.get(idUser).containsKey(idUserNeighbour)) {
                    continue;
                }

                usersTrust.get(idUser).put(idUserNeighbour, MSD.get(idUser).get(idUserNeighbour).doubleValue() * UJaccard.get(idUser).get(idUserNeighbour).doubleValue());

            }
        }

        UJaccard.clear();
        MSD.clear();

        if (printPartialResults) {
            Global.showInfoMessage("Trust derivation matrix.\n");
            DatasetPrinterDeprecated.printCompactUserUserTable(usersTrust, users);

            Global.showInfoMessage("============================================================================= \n");
            Global.showInfoMessage("UT-Step1 - B: Trust Propagation\n");
            Global.showInfoMessage("============================================================================= \n");
        }

        if (propagate == false) {
            return new WeightedGraph<Integer>(usersTrust);
        }

        fireProgressChanged("Trust propagation", 0, -1);

        /**
         * Las confianzas propagadas se incluyen aqui para no ser tenidas en
         * cuenta al propagar confianza, sólo cuando ha finalizado el proceso.
         */
        Map<Integer, Map<Integer, Double>> propagatedTrusts = new TreeMap<Integer, Map<Integer, Double>>();
        {
            int i = 0;
            for (int idSourceUser : users) {
                for (int idTargetUser : users) {
                    if (usersTrust.get(idSourceUser).containsKey(idTargetUser)) {
                        //Los usuarios ya son adyacentes, no es necesario propagar.
                    } else {
                        if (!propagatedTrusts.containsKey(idSourceUser)) {
                            propagatedTrusts.put(idSourceUser, new TreeMap<Integer, Double>());
                        }
                        //Propagate trust to complete this connection.
                        if (printPartialResults) {
                            Global.showInfoMessage("Propagating trust between user " + idSourceUser + " and user " + idTargetUser + "\n");
                        }

                        Set<Integer> adyacentesAAmbos = new TreeSet<Integer>(usersTrust.get(idSourceUser).keySet());
                        adyacentesAAmbos.retainAll(usersTrust.get(idTargetUser).keySet());

//                        //Obtengo todos los usuarios adyacentes al usuario:
//                        Set<Integer> adyacentesATarget = new TreeSet<Integer>(usersTrust.get(idTargetUser).keySet());
//                        Set<Integer> adyacentesAAmbos = new TreeSet<Integer>(usersTrust.get(idSourceUser).keySet());
//                        for (Iterator<Integer> it = adyacentesAAmbos.iterator(); it.hasNext();) {
//                            int idAdyacente = it.next();
//                            if (!usersTrust.get(idAdyacente).containsKey(idTargetUser)) {
//                                it.remove();
//                            }
//                        }
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
                            propagatedTrusts.get(idSourceUser).put(idTargetUser, PTrustAB);
                            if (printPartialResults) {
                                Global.showInfoMessage("Propagated trust between user " + idSourceUser + " and user " + idTargetUser + " --> " + PTrustAB + " (" + (int) ((i * 100f) / numTrustValuesThatNeedToBePropagated) + "%)\n");
                            }
                        }
                        fireProgressChanged("User trust propagation", (int) ((i * 100f) / numTrustValuesThatNeedToBePropagated), -1);
                        i++;
                    }
                }
            }
        }

        //Una vez todas las confianzas propagadas se han calculado, se introducen en el grafo.
        for (int idSourceUser : propagatedTrusts.keySet()) {
            for (int idTargetUser : propagatedTrusts.get(idSourceUser).keySet()) {
                double PTrustAB = propagatedTrusts.get(idSourceUser).get(idTargetUser);
                usersTrust.get(idSourceUser).put(idTargetUser, PTrustAB);
            }
        }

        if (printPartialResults) {
            Global.showInfoMessage("Trust propagation matrix.\n");
            DatasetPrinterDeprecated.printCompactUserUserTable(usersTrust, users);
        }

        fireProgressChanged("Finished trust calculation", 100, -1);
        return new WeightedGraph<Integer>(usersTrust);
    }
}
