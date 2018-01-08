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
package delfos.group.casestudy.definedcases.jrs2014;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.common.exceptions.dataset.users.UserNotFound;

/**
 * Calcula la confianza entre dos usuarios.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 24-feb-2014
 */
public class PairwiseTrust_QusaiShambourAndJieLu implements PairwiseUserTrust {

    /**
     * Devuelve la confianza entre dos usuarios, usando
     *
     * @param datasetLoader
     * @param idUser1
     * @param idUser2
     * @return
     */
    @Override
    public double getTrust(DatasetLoader<? extends Rating> datasetLoader, long idUser1, long idUser2) throws UserNotFound {
        double MSD;
        double Jaccard;

        {
            double meanUser = datasetLoader.getRatingsDataset().getMeanRatingUser(idUser1);
            double meanUserNeighbour = datasetLoader.getRatingsDataset().getMeanRatingUser(idUser2);

            Map<Long, ? extends Rating> userRatings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser1);
            Map<Long, ? extends Rating> userNeighbourRatings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser2);

            Set<Long> commonItems = new TreeSet<Long>(userRatings.keySet());
            commonItems.retainAll(userNeighbourRatings.keySet());

            double[] ratings = new double[commonItems.size()];
            double[] predictions = new double[commonItems.size()];

            //Compute predictions. At the same time, compute mand min of rating and prediction, to save cycles later on.
            double ratingMax = -Double.MAX_VALUE;
            double ratingMin = Double.MAX_VALUE;
            double predictionMax = -Double.MAX_VALUE;
            double predictionMin = Double.MAX_VALUE;

            {
                int index = 0;
                for (long idItem : commonItems) {
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
            MSD = MSD_of_pair;

            //Calculo el Jaccard
            double numUserRatings = userRatings.size();
            double numUserNeighborRatings = userNeighbourRatings.size();
            double numCommon = commonItems.size();
            double jaccard_of_pair = numCommon / (numUserRatings + numUserNeighborRatings - commonItems.size());
            Jaccard = jaccard_of_pair;
        }

        double ret = MSD * Jaccard;
        return ret;
    }
}
