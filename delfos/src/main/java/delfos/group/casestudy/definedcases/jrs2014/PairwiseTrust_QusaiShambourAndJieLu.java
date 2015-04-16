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
* @author Jorge Castro Gallardo
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
     * @throws delfos.common.Exceptions.Dataset.Users.UserNotFound
     */
    @Override
    public double getTrust(DatasetLoader<? extends Rating> datasetLoader, int idUser1, int idUser2) throws UserNotFound {
        double MSD;
        double Jaccard;

        {
            double meanUser = datasetLoader.getRatingsDataset().getMeanRatingUser(idUser1);
            double meanUserNeighbour = datasetLoader.getRatingsDataset().getMeanRatingUser(idUser2);

            Map<Integer, ? extends Rating> userRatings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser1);
            Map<Integer, ? extends Rating> userNeighbourRatings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser2);

            Set<Integer> commonItems = new TreeSet<Integer>(userRatings.keySet());
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
                for (int idItem : commonItems) {
                    double rating = userRatings.get(idItem).ratingValue.doubleValue();
                    ratings[index] = rating;
                    double prediction = meanUser + userNeighbourRatings.get(idItem).ratingValue.doubleValue() - meanUserNeighbour;
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
