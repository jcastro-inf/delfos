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
package delfos.utils.hesitant.similarity.factory;

import delfos.utils.hesitant.similarity.HesitantPearson;
import delfos.utils.hesitant.similarity.HesitantSimilarity;
import delfos.utils.hesitant.similarity.basic.HesitantMeanAggregation;
import delfos.utils.hesitant.similarity.basic.HesitantMinAggregation;
import delfos.utils.hesitant.similarity.basic.HesitantRMSMeanAggregation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class HesitantSimilarityFactory {

    private static final Collection< HesitantSimilarity> hesitantSimilarities;

    static {
        ArrayList<HesitantSimilarity> similarities = new ArrayList<>(1);

        similarities.add(new HesitantPearson());
        similarities.add(new HesitantMeanAggregation());
        similarities.add(new HesitantMinAggregation());
        similarities.add(new HesitantRMSMeanAggregation());

        hesitantSimilarities = Collections.unmodifiableCollection(similarities);
    }

    public static Collection<HesitantSimilarity> getAll() {
        return hesitantSimilarities;
    }

    public static HesitantSimilarity getHesitantSimilarity(String name) {
        Optional<HesitantSimilarity> hesitantSimilarity
                = hesitantSimilarities.stream()
                .filter(hesitantSimilarityIterated
                        -> hesitantSimilarityIterated.getClass().getSimpleName().equalsIgnoreCase(name))
                .findFirst();

        if (hesitantSimilarity.isPresent()) {
            return hesitantSimilarity.get();
        } else {
            throw new IllegalArgumentException("Hesitant similarity '" + name + "' not found");
        }

    }
}
