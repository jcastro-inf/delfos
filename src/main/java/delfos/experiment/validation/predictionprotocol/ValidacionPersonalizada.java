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
package delfos.experiment.validation.predictionprotocol;

import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.DoubleParameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * Prediction protocol that allows to control, in an advanced way, the number of users for which a prediction is
 * requested and the number of ratings predicted for each of them. It has two parameters: one to control the number of
 * items predicted and other to ensure a minimum number of ratings for users in the test set.
 * <p>
 * <p>
 * This prediction protocol is used without validation, it does all the job by itself.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 */
public class ValidacionPersonalizada extends PredictionProtocol {

    private static final long serialVersionUID = 1L;
    /**
     * Minimum ratings that a test user has.
     */
    public static Parameter minRatings = new Parameter("minRatings", new IntegerParameter(1, 10000, 4));
    /**
     * Percent of users in the test set.
     */
    public static Parameter userPercent = new Parameter("userPercent", new DoubleParameter(0.0001f, 1, 1));
    /**
     * Percent of ratings requested to each user in the test set.
     */
    public static Parameter ratingsToPredictPercent = new Parameter("ratingsToPredictPercent", new DoubleParameter(0.0001f, 1, 1));

    public ValidacionPersonalizada() {
        super();
        addParameter(minRatings);
        addParameter(ratingsToPredictPercent);
        addParameter(userPercent);
    }

    /**
     * Crea la técnica de validación asignando los valores de los parámetros que controlan el número de datos que se
     * evaluan.
     *
     * @param minRatingsValue Minimum ratings that a test user has.
     * @param userPercentValue Percent of users in the test set.
     * @param ratingsToPredictPercentValue Percent of ratings requested to each user in the test set.
     */
    public ValidacionPersonalizada(int minRatingsValue, double userPercentValue, double ratingsToPredictPercentValue) {
        addParameter(minRatings);
        addParameter(ratingsToPredictPercent);
        addParameter(userPercent);

        setParameterValue(minRatings, minRatingsValue);
        setParameterValue(ratingsToPredictPercent, ratingsToPredictPercentValue);
        setParameterValue(userPercent, userPercentValue);
    }

    @Override
    public <RatingType extends Rating> List<Set<Integer>> getRecommendationRequests(
            DatasetLoader<RatingType> trainingDatasetLoader,
            DatasetLoader<RatingType> testDatasetLoader,
            int idUser) throws UserNotFound {
        Random random = new Random(getSeedValue());

        double userPercentValue = (Double) getParameterValue(userPercent);
        if (random.nextDouble() > userPercentValue) {
            return new ArrayList<>();
        }
        int minRatingsValue = (Integer) getParameterValue(minRatings);
        double ratingsToPredictPercentValue = (Double) getParameterValue(ratingsToPredictPercent);

        Integer[] itemsRated = testDatasetLoader.getRatingsDataset().getUserRatingsRated(idUser).keySet().toArray(new Integer[0]);
        int extraer = (int) (itemsRated.length * (1 - ratingsToPredictPercentValue));
        if (extraer < minRatingsValue) {
            extraer = minRatingsValue;
        }

        Set<Integer> extraidos = new TreeSet<>();
        if (extraer > itemsRated.length) {
            return new ArrayList<>();
        }

        while (extraidos.size() < extraer) {
            int index = random.nextInt(itemsRated.length);
            extraidos.add(itemsRated[index]);
        }

        List<Set<Integer>> ret = new ArrayList<>(extraidos.size());
        ret.add(extraidos);
        return ret;
    }
}
