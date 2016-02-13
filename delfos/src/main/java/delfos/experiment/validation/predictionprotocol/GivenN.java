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

import delfos.common.Global;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.RecommenderSystemAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * Implementa la validación de predicción que realiza una validación dadas N
 * valoraciones para el usuario que se predice
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class GivenN extends PredictionProtocol {

    private static final long serialVersionUID = 1L;
    /**
     * Cantidad de valoraciones que se usan en la predicción para un usuario
     */
    public static Parameter n = new Parameter("n", new IntegerParameter(1, 10000, 4));

    /**
     * Constructor por defecto que deja el numero de valoraciones a 4
     */
    public GivenN() {
        super();
        addParameter(n);
    }

    /**
     * Constructor que asigna el n especificado en el parámetro <code>n</code>
     * para que se use dicho número de valoraciones de cada usuario
     *
     * @param n Cantidad de valoraciones que se usan en la predicción para un
     * usuario
     */
    public GivenN(int n) {
        addParameter(GivenN.n);
        setParameterValue(GivenN.n, n);
    }

    /**
     * Devuelve las valoraciones que se han de predecir en una lista
     * independiente. Para un correcto uso de los resultados, se deben extraer
     * del dataset de valoraciones que el sistema de recomendación usa todas las
     * valoraciones que se van a predecir.
     *
     * @param idUser
     * @return Lista de listas con los elementos que se predicen. Cada lista
     * representa una petición de recomendaciones, es decir, una llamada a
     * {@link RecommenderSystemAdapter#recommendOnly(java.lang.Integer, java.util.Collection) }
     * en la que la colección que se pasan son los elementos de la lista.
     * @throws UserNotFound
     */
    @Override
    public Collection<Set<Integer>> getRecommendationRequests(RatingsDataset<? extends Rating> testRatingsDataset, int idUser) throws UserNotFound {
        Random random = new Random(getSeedValue());
        Integer[] itemsRated = testRatingsDataset.getUserRatingsRated(idUser).keySet().toArray(new Integer[0]);
        int nValue = (Integer) getParameterValue(n);
        Set<Integer> dadosN = new TreeSet<>();

        if (nValue > itemsRated.length) {
            Global.showWarning("Cannot apply " + GivenN.class.getName() + "\n");
            Global.showWarning("cause: user '" + idUser + "' have less than " + nValue + " ratings");
        }

        while (dadosN.size() < nValue) {
            dadosN.add(itemsRated[random.nextInt(itemsRated.length)]);
        }

        Set<Integer> predecir = new TreeSet<>();
        for (int idItem : itemsRated) {
            if (!dadosN.contains(idItem)) {
                predecir.add(idItem);
            }
        }

        Collection<Set<Integer>> ret = new ArrayList<>(predecir.size());
        ret.add(predecir);
        return ret;
    }
}
