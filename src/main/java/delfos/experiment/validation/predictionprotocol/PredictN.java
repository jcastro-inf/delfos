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
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Implementa la validación de predicción que realiza una validación que predice N valoraciones de cada usuario. Por lo
 * tanto, en cada perfil de usuario quedarán X-n valoraciones, donde X es el número d evaloraciones original que el
 * usuario había hecho.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.1 21-Jan-2013
 * @version 1.0 Unknow date
 */
public class PredictN extends PredictionProtocol {

    public static final long serialVersionUID = 1L;
    /**
     * Cantidad de valoraciones que se predicen para un usuario
     */
    public static Parameter n = new Parameter("n", new IntegerParameter(1, 10000, 4));

    /**
     * Constructor por defecto que deja el numero de valoraciones a 4
     */
    public PredictN() {
        super();
        addParameter(n);
    }

    /**
     * Constructor que asigna el n especificado en el parámetro <code>n</code> para que se prediga dicho número de
     * valoraciones de cada usuario
     *
     * @param nValue Cantidad de valoraciones que se predicen para un usuario
     */
    public PredictN(int nValue) {
        addParameter(n);
        setParameterValue(n, nValue);
    }

    @Override
    public <RatingType extends Rating> List<Set<Item>> getRecommendationRequests(
            DatasetLoader<RatingType> trainingDatasetLoader,
            DatasetLoader<RatingType> testDatasetLoader,
            User user) throws UserNotFound {

        Random random = new Random(getSeedValue());
        Collection<Item> userRated = new TreeSet<>(testDatasetLoader.getRatingsDataset().getUserRatingsRated(user.getId())
        .values().stream().map(rating -> rating.getItem()).collect(Collectors.toSet()));
        Set<Item> extraidos = new TreeSet<>();
        Number extraer = (Number) getParameterValue(PredictN.n);

        if (extraer.intValue() > userRated.size()) {
            //no se pueden extraer el número que se solicita, qué hacer?
            Global.showWarning("User " + user.getId() + " has not enough test rating to extract " + extraer + "\n");

            List<Set<Item>> ret = new ArrayList<>(extraidos.size());
            Set<Item> l = new TreeSet<>(userRated);
            ret.add(l);
            return ret;
        } else {
            while (!userRated.isEmpty() && extraidos.size() != extraer.doubleValue()) {
                int index = random.nextInt(userRated.size());
                Item item = userRated.toArray(new Item[1])[index];
                userRated.remove(item);
                extraidos.add(item);
            }

            List<Set<Item>> ret = new ArrayList<>(extraidos.size());
            ret.add(extraidos);
            return ret;
        }
    }
}
