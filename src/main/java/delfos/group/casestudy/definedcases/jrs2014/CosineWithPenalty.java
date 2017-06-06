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

import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.similaritymeasures.CosineCoefficient;
import delfos.similaritymeasures.WeightedSimilarityMeasure;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Calcula la confianza entre dos usuarios.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 24-feb-2014
 */
public class CosineWithPenalty implements PairwiseUserTrust {

    private final int penalty;

    public CosineWithPenalty(int penalty) {
        this.penalty = penalty;
    }

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

        Map<Long, ? extends Rating> user1ratings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser1);
        Map<Long, ? extends Rating> user2ratings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser2);

        Set<Long> commonItems = new TreeSet<Long>(user1ratings.keySet());
        commonItems.retainAll(user2ratings.keySet());

        WeightedSimilarityMeasure similarityMeasure = new CosineCoefficient();
        ArrayList<Double> user1 = new ArrayList<Double>(commonItems.size());
        ArrayList<Double> user2 = new ArrayList<Double>(commonItems.size());
        for (long idItem : commonItems) {
            user1.add(user1ratings.get(idItem).getRatingValue().doubleValue());
            user2.add(user2ratings.get(idItem).getRatingValue().doubleValue());
        }
        double similarity;
        try {
            similarity = similarityMeasure.similarity(user1, user2);
            similarity = (similarity + 1) / 2;
        } catch (CouldNotComputeSimilarity ex) {
            return Double.NaN;
        }
        if (commonItems.size() < penalty) {
            similarity = commonItems.size() * (similarity / penalty);
        }

        if (similarity < 0) {
            similarity = 0;
        }

        if (similarity > 1) {
            similarity = 1;
        }
        return similarity;
    }
}
