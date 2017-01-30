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
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVD;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Protocolo de predicción nulo, es decir, no hace nada. Solo solicita todas las valoraciones que están en el conjunto
 * de test de una vez.
 *
 * Se utiliza cuando no se desea aplicar porotocolo de predicción, por ejemplo, en el sistema de recomendación SVD
 * {@link TryThisAtHomeSVD} no tiene sentido aplicar protocolo de predicción, ya que cuando cambian las valoraciones del
 * usuario se debe volver a construir el modelo para que se actualicen las recomendaciones.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 21-Jan-2013
 */
public class NoPredictionProtocol extends PredictionProtocol {

    public static final long serialVersionUID = 1L;

    @Override
    public List<Set<Integer>> getRecommendationRequests(RatingsDataset<? extends Rating> testRatingsDataset, int idUser) throws UserNotFound {
        List<Set<Integer>> listOfRequests = new ArrayList<>(1);

        Set<Integer> userRated = new TreeSet<>(testRatingsDataset.getUserRated(idUser));

        listOfRequests.add(userRated);

        return listOfRequests;
    }

    @Override
    public List<Set<Integer>> getRatingsToHide(RatingsDataset<? extends Rating> testRatingsDataset, int idUser) throws UserNotFound {
        return getRecommendationRequests(testRatingsDataset, idUser)
                .stream()
                .map(object -> new TreeSet<Integer>())
                .collect(Collectors.toList());
    }

}
