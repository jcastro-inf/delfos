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
package delfos.rs.trustbased;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.util.DatasetPrinterDeprecated;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Algoritmo para calcular redes de confianza entre usuarios, utilizando las
 * valoraciones. Este método es el propuesto en:
 *
 * <p>
 * <p>
 * Qusai Shambour, Jie Lu: An Effective Recommender System by Unifying User and
 * Item Trust Information for B2B Applications.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 03-May-2013
 */
public class ImplicitTrustComputation_pearson {

    /**
     * Calcula la red de confianza entre los usuarios especificados utilizando
     * las valoraciones indicadas.
     *
     * @param ratingsDataset Dataset de valoraciones del que se extrae la
     * confianza.
     * @param users Usuarios para los que se calcula la red de confianza.
     * @return
     */
    public WeightedGraphAdapter<Integer> computeTrustValues(RatingsDataset<? extends Rating> ratingsDataset, Collection<Integer> users) {
        boolean printPartialResults;

        printPartialResults = ratingsDataset.allRatedItems().size() <= 100 && ratingsDataset.allUsers().size() <= 28;

        if (!Global.isInfoPrinted()) {
            printPartialResults = false;
        }

        if (printPartialResults) {
            Global.showInfoMessage("Dataset de training \n");
            DatasetPrinterDeprecated.printCompactRatingTable(ratingsDataset);
        }

        Map<Integer, Map<Integer, Number>> pearson = new TreeMap<Integer, Map<Integer, Number>>();

        PearsonCorrelationCoefficient pearsonCorrelationCoefficient = new PearsonCorrelationCoefficient();

        for (int idUser : users) {

            pearson.put(idUser, new TreeMap<Integer, Number>());

            Map<Integer, ? extends Rating> userRatings = null;
            try {
                userRatings = ratingsDataset.getUserRatingsRated(idUser);
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
                throw new IllegalArgumentException(ex);
            }

            //Para cada vecino calculo el MSD
            for (int idUserNeighbor : users) {

                Map<Integer, ? extends Rating> neighborRatings;
                try {
                    neighborRatings = ratingsDataset.getUserRatingsRated(idUserNeighbor);

                    List<Double> userList = new LinkedList<Double>();
                    List<Double> neighborList = new LinkedList<Double>();
                    for (int idItem : userRatings.keySet()) {
                        if (neighborRatings.containsKey(idItem)) {
                            userList.add(userRatings.get(idItem).getRatingValue().doubleValue());
                            neighborList.add(neighborRatings.get(idItem).getRatingValue().doubleValue());
                        }
                    }

                    double value;
                    try {
                        value = pearsonCorrelationCoefficient.similarity(userList, neighborList);
                        pearson.get(idUser).put(idUserNeighbor, value);
                    } catch (CouldNotComputeSimilarity ex) {

                    }

                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                }
            }
        }

        if (printPartialResults) {
            Global.showInfoMessage("Pearson to [0,1] table \n");
            DatasetPrinterDeprecated.printCompactUserUserTable(pearson, users);
        }

        return new WeightedGraphAdapter<Integer>(pearson);
    }
}
