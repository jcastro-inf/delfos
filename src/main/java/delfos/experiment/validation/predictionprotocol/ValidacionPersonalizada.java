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
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * Técnica de validación que permite controlar de una manera avanzada el número
 * usuarios para los que se realiza una predicción y el número de valoraciones
 * que se predicen para cada uno de ellos. Consta de dos parámetros que
 * controlan el número de datos que se predicen y otro parámetro que funciona
 * como restricción
 *
 * Esta técnica de predicción surge al comprobar que la validación
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 *
 * @version 1.1 21-Jan-2013
 * @version 1.0 Unknow date
 */
public class ValidacionPersonalizada extends PredictionProtocol {

    private static final long serialVersionUID = 1L;
    /**
     * Cantidad de valoraciones que se usan en la predicción para un usuario
     */
    public static Parameter minRatings = new Parameter("minRatings", new IntegerParameter(1, 10000, 4));
    /**
     * Porcentaje de usuarios que se comprueban en la validación.
     */
    public static Parameter userPercent = new Parameter("userPercent", new DoubleParameter(0.0001f, 1, 1));
    /**
     * Porcentaje de valoraciones que se comprueban de cada usuario.
     */
    public static Parameter ratingsToPredictPercent = new Parameter("ratingsToPredictPercent", new DoubleParameter(0.0001f, 1, 1));

    /**
     * Constructor por defecto que deja el numero de valoraciones a 4
     */
    public ValidacionPersonalizada() {
        super();
        addParameter(minRatings);
        addParameter(ratingsToPredictPercent);
        addParameter(userPercent);
    }

    /**
     * Crea la técnica de validación asignando los valores de los parámetros que
     * controlan el número de datos que se evaluan.
     *
     * @param minRatingsValue Número mínimo de ratings que un usuario tiene en
     * el conjunto de entrenamiento
     * @param userPercentValue Porcentaje de usuarios que se comprueban
     * @param ratingsToPredictPercentValue Porcentaje de valoraciones de un
     * usuario que se comprueban.
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
    public Collection<Set<Integer>> getRecommendationRequests(RatingsDataset<? extends Rating> testRatingsDataset, int idUser) throws UserNotFound {
        Random random = new Random(getSeedValue());

        double userPercentValue = (Double) getParameterValue(userPercent);
        if (random.nextDouble() > userPercentValue) {
            return new ArrayList<>();
        }
        int minRatingsValue = (Integer) getParameterValue(minRatings);
        double ratingsToPredictPercentValue = (Double) getParameterValue(ratingsToPredictPercent);

        Integer[] itemsRated = testRatingsDataset.getUserRatingsRated(idUser).keySet().toArray(new Integer[0]);
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

        Collection<Set<Integer>> ret = new ArrayList<>(extraidos.size());
        ret.add(extraidos);
        return ret;
    }
}
