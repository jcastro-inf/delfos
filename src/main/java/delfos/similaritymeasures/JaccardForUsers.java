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
package delfos.similaritymeasures;

import java.util.Collection;
import java.util.TreeSet;
import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.RecommenderSystemAdapter;

/**
 * Clase que implementa la medida Jaccard para la similitud de los usuarios.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 16-Julio-2013
 */
public class JaccardForUsers {

    public double similarity(RatingsDataset<? extends Rating> ratings, int idUser1, int idUser2) throws CouldNotComputeSimilarity {

        //Calculo el Jaccard
        Collection<Integer> user1Ratings;
        try {
            user1Ratings = ratings.getUserRated(idUser1);
        } catch (UserNotFound ex) {
            throw new CouldNotComputeSimilarity(ex);
        }

        Collection<Integer> user2Ratings;
        try {
            user2Ratings = ratings.getUserRated(idUser2);
        } catch (UserNotFound ex) {
            throw new CouldNotComputeSimilarity(ex);
        }

        TreeSet<Integer> intersection = new TreeSet<>(user1Ratings);
        intersection.retainAll(user2Ratings);

        TreeSet<Integer> union = new TreeSet<>(user1Ratings);
        union.addAll(user2Ratings);

        if (union.isEmpty()) {
            return 0;
        } else {

            double numCommon = intersection.size();

            double jaccard = numCommon / union.size();
            return jaccard;
        }
    }

    public boolean RSallowed(Class<? extends RecommenderSystemAdapter> rs) {
        return true;
    }
}
