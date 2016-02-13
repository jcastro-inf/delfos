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
package delfos.similaritymeasures.useruser;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.similaritymeasures.SimilarityMeasureAdapter;

/**
 *
 * @version 24-Junio-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class RelevanceFactor extends SimilarityMeasureAdapter implements UserUserSimilarity {

    public static final Parameter RELEVANCE_FACTOR = new Parameter(
            "RelevanceFactorValue",
            new IntegerParameter(0, 50000, 30),
            "Relevance factor constructs similarity from items rated intersection. If relevance factor value is zero, similarity is always 1.");

    public RelevanceFactor() {
        super();
        addParameter(RELEVANCE_FACTOR);
    }

    public RelevanceFactor(int relevanceFactor) {
        this();
        setParameterValue(RELEVANCE_FACTOR, relevanceFactor);
    }

    @Override
    public double similarity(DatasetLoader<? extends Rating> datasetLoader, int idUser1, int idUser2) throws UserNotFound, CouldNotComputeSimilarity {

        final int relevanceFactorValule = (Integer) getParameterValue(RELEVANCE_FACTOR);

        if (relevanceFactorValule == 0) {
            return 1;
        }

        Collection<Integer> user1Ratings = datasetLoader.getRatingsDataset().getUserRated(idUser1);
        Collection<Integer> user2Ratings = datasetLoader.getRatingsDataset().getUserRated(idUser2);

        Set<Integer> intersection = new TreeSet<>(user1Ratings);
        intersection.retainAll(user2Ratings);

        double similarity;
        if (intersection.size() >= relevanceFactorValule) {
            similarity = 1;
        } else {
            similarity = ((double) intersection.size()) / relevanceFactorValule;
        }

        return similarity;
    }
}
