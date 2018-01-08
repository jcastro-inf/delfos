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
public class PairwiseJaccard implements PairwiseUserTrust {

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

        double Jaccard;

        {

            Map<Long, ? extends Rating> userRatings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser1);
            Map<Long, ? extends Rating> userNeighbourRatings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser2);

            Set<Long> commonItems = new TreeSet<Long>(userRatings.keySet());
            commonItems.retainAll(userNeighbourRatings.keySet());

            //Calculo el Jaccard
            double numUserRatings = userRatings.size();
            double numUserNeighborRatings = userNeighbourRatings.size();
            double numCommon = commonItems.size();
            double jaccard_of_pair = numCommon / (numUserRatings + numUserNeighborRatings - commonItems.size());
            Jaccard = jaccard_of_pair;
        }

        double ret = Jaccard;
        return ret;
    }
}
