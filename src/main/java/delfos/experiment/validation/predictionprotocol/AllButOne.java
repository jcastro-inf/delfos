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
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

/**
 * Esta técnica realiza la composición de recomendaciones de manera que se
 * utilizan todos los ratings del usuario en la predicción excepto el que se
 * desea predecir.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class AllButOne extends PredictionProtocol {

    private static final long serialVersionUID = 1L;

    @Override
    public Collection<Set<Integer>> getRecommendationRequests(RatingsDataset<? extends Rating> testRatingsDataset, int idUser) throws UserNotFound {
        Collection<Integer> userRated = testRatingsDataset.getUserRated(idUser);

        Collection<Set<Integer>> collectionOfSetsOfRequests = new LinkedList<>();

        for (int idItem : userRated) {
            Set<Integer> oneRequestSet = new TreeSet<>();
            oneRequestSet.add(idItem);
            collectionOfSetsOfRequests.add(oneRequestSet);
        }

        return collectionOfSetsOfRequests;
    }
}
