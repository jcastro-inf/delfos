package delfos.rs.trustbased;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.util.DatasetPrinterDeprecated;
import delfos.ERROR_CODES;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import delfos.common.exceptions.CouldNotComputeSimilarity;
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
 * @author Jorge Castro Gallardo (Sinbad2,Universidad de Jaén)
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

                    List<Float> userList = new LinkedList<Float>();
                    List<Float> neighborList = new LinkedList<Float>();
                    for (int idItem : userRatings.keySet()) {
                        if (neighborRatings.containsKey(idItem)) {
                            userList.add(userRatings.get(idItem).ratingValue.floatValue());
                            neighborList.add(neighborRatings.get(idItem).ratingValue.floatValue());
                        }
                    }

                    float value;
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
