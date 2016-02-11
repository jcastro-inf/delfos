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
import delfos.rs.trustbased.WeightedGraphAdapter;
import delfos.rs.trustbased.WeightedGraphCalculation;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.Global;

/**
 * Algoritmo para calcular redes de confianza entre productos, utilizando las
 * valoraciones. Este método es el propuesto en:
 *
 * <p>
 * <p>
 * Qusai Shambour, Jie Lu: An Effective Recommender System by Unifying User and
 * Item Trust Information for B2B Applications.
 *
 * @author Jorge Castro Gallardo (Sinbad2,Universidad de Jaén)
 * @version 1.0 29-Agosto-2013
 */
public class ShambourLu_ItemBasedImplicitTrustComputation extends WeightedGraphCalculation<Integer> {

    private static final long serialVersionUID = 1L;

    public ShambourLu_ItemBasedImplicitTrustComputation() {
    }

    /**
     * Calcula la red de confianza entre los productos especificados utilizando
     * las valoraciones indicadas.
     *
     * @param items Productos para los que se calcula la red de confianza.
     * @return
     */
    @Override
    public WeightedGraphAdapter<Integer> computeTrustValues(DatasetLoader<? extends Rating> datasetLoader, Collection<Integer> items) throws CannotLoadRatingsDataset {
        boolean printPartialResults;

        final RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

        printPartialResults = ratingsDataset.allRatedItems().size() <= 100 && ratingsDataset.allUsers().size() <= 28;

        if (!Global.isInfoPrinted()) {
            printPartialResults = false;
        }

        if (printPartialResults) {
            Global.showInfoMessage("Dataset utilizado.\n");
            DatasetPrinterDeprecated.printCompactRatingTable(ratingsDataset);
        }

        Map<Integer, Map<Integer, Number>> MSD = new TreeMap<Integer, Map<Integer, Number>>();
        Map<Integer, Map<Integer, Number>> UJaccard = new TreeMap<Integer, Map<Integer, Number>>();

        int i = 1;
        for (int idItem : items) {
            MSD.put(idItem, new TreeMap<Integer, Number>());
            UJaccard.put(idItem, new TreeMap<Integer, Number>());

            Map<Integer, ? extends Rating> itemRatings;
            try {
                itemRatings = ratingsDataset.getItemRatingsRated(idItem);
            } catch (ItemNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
                return null;
            }

            //Para cada vecino calculo el MSD
            for (int idItemNeighbor : items) {
                try {
                    double meanItem = ratingsDataset.getMeanRatingItem(idItem);
                    double meanItemNeighbour = ratingsDataset.getMeanRatingItem(idItemNeighbor);

                    Map<Integer, ? extends Rating> itemNeighbourRatings = ratingsDataset.getItemRatingsRated(idItemNeighbor);

                    Set<Integer> commonUsers = new TreeSet<Integer>(itemRatings.keySet());
                    commonUsers.retainAll(itemNeighbourRatings.keySet());
                    if (commonUsers.isEmpty()) {
                        continue;
                    }
                    double[] ratings = new double[commonUsers.size()];
                    double[] predictions = new double[commonUsers.size()];

                    //Compute predictions. At the same time, compute mand min of rating and prediction, to save cycles later on.
                    double ratingMax = -Double.MAX_VALUE;
                    double ratingMin = Double.MAX_VALUE;
                    double predictionMax = -Double.MAX_VALUE;
                    double predictionMin = Double.MAX_VALUE;

                    {
                        int index = 0;
                        for (int idUser : commonUsers) {
                            double rating = itemRatings.get(idUser).getRatingValue().doubleValue();
                            ratings[index] = rating;
                            double prediction = meanItem + itemNeighbourRatings.get(idUser).getRatingValue().doubleValue() - meanItemNeighbour;
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

                    double MSD_of_pair = 1 - (sumOfError / commonUsers.size());
                    MSD.get(idItem).put(idItemNeighbor, MSD_of_pair);

                    //Calculo el Jaccard
                    double numUserRatings = itemRatings.size();
                    double numUserNeighborRatings = itemNeighbourRatings.size();
                    double numCommon = commonUsers.size();
                    double jaccard_of_pair = numCommon / (numUserRatings + numUserNeighborRatings - commonUsers.size());
                    UJaccard.get(idItem).put(idItemNeighbor, jaccard_of_pair);
                } catch (ItemNotFound ex) {
                    ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
                }
            }

            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage(new Date().toString() + ": Item-based trust " + ((i * 100.0f) / items.size()) + "%\n");
            }

            fireProgressChanged("Item-Based trust", (int) ((i * 100f) / items.size()), -1);
            i++;
        }
        if (printPartialResults) {
            Global.showInfoMessage("MSD table \n");
            DatasetPrinterDeprecated.printCompactUserUserTable(MSD, items);

            Global.showInfoMessage("============================================================================= \n");
            Global.showInfoMessage("IT-Step1 0.4: Jaccard\n");
            Global.showInfoMessage("============================================================================= \n");

            Global.showInfoMessage("Jaccard table \n");
            DatasetPrinterDeprecated.printCompactUserUserTable(UJaccard, items);

            Global.showInfoMessage("============================================================================= \n");
            Global.showInfoMessage("IT-Step1 - A: Trust derivation\n");
            Global.showInfoMessage("============================================================================= \n");
        }

        TreeMap<Integer, Map<Integer, Number>> itemBasedTrust = new TreeMap<Integer, Map<Integer, Number>>();
        for (int idItem : items) {
            itemBasedTrust.put(idItem, new TreeMap<Integer, Number>());

            for (int idItemNeighbour : items) {

                if (!UJaccard.containsKey(idItem) || !UJaccard.get(idItem).containsKey(idItemNeighbour) || !MSD.containsKey(idItem) || !MSD.get(idItem).containsKey(idItemNeighbour)) {
                    continue;
                }

                itemBasedTrust.get(idItem).put(idItemNeighbour, MSD.get(idItem).get(idItemNeighbour).doubleValue() * UJaccard.get(idItem).get(idItemNeighbour).doubleValue());

            }
        }
        UJaccard.clear();
        MSD.clear();

        if (printPartialResults) {
            Global.showInfoMessage("Trust derivation matrix.\n");
            DatasetPrinterDeprecated.printCompactUserUserTable(itemBasedTrust, items);
        }

        return new WeightedGraphAdapter<Integer>(itemBasedTrust);
    }
}
