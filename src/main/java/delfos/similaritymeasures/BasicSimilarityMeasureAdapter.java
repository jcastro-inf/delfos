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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.rs.collaborativefiltering.knn.CommonRating;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.RecommenderSystemAdapter;

/**
 * Clase que adapta la medida de similitud completando los métodos de medida de
 * simlitud ponderada. Esto se hace multiplicando cada valor de los vectores a
 * comparar por la ponderación de la dimensión.
 *
 * <p>
 * <p>
 * La similitud es un valor entre 0 y 1, 0 cuando los vectores son completamente
 * distintos y 1 cuando son completamente iguales.
 *
 * @see SimilarityMeasure
 * @see BasicSimilarityMeasure
 * @see WeightedSimilarityMeasure
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public abstract class BasicSimilarityMeasureAdapter extends SimilarityMeasureAdapter implements BasicSimilarityMeasure, WeightedSimilarityMeasure, CollaborativeSimilarityMeasure {

    @Override
    public final float similarity(float[] v1, float[] v2) throws CouldNotComputeSimilarity {

        if (v1.length != v2.length) {
            throw new IllegalArgumentException("The length of vectors is different");
        }

        List<Float> v1List = new ArrayList<>(v1.length);
        List<Float> v2List = new ArrayList<>(v2.length);

        for (int i = 0; i < v1.length; i++) {
            v1List.add(v1[i]);
            v2List.add(v2[i]);
        }
        return similarity(v1List, v2List);
    }

    @Override
    public final float weightedSimilarity(float[] v1, float[] v2, float[] weights) throws CouldNotComputeSimilarity {

        if (v1.length != v2.length || v1.length != weights.length) {
            throw new IllegalArgumentException("The length of vectors is different");
        }

        List<Float> v1List = new ArrayList<>(v1.length);
        List<Float> v2List = new ArrayList<>(v2.length);
        List<Float> weightsList = new ArrayList<>(weights.length);

        for (int i = 0; i < v1.length; i++) {
            v1List.add(v1[i]);
            v2List.add(v2[i]);
            weightsList.add(weights[i]);
        }
        return weightedSimilarity(v1List, v2List, weightsList);
    }

    @Override
    public float similarity(Collection<CommonRating> commonRatings, RatingsDataset<? extends Rating> ratings) throws CouldNotComputeSimilarity {
        Iterator<CommonRating> it = commonRatings.iterator();
        if (it.hasNext()) {
            if (it.next().isWeighted()) {
                float[] v1 = new float[commonRatings.size()];
                float[] v2 = new float[commonRatings.size()];
                float[] weights = new float[commonRatings.size()];
                int i = 0;
                for (CommonRating c : commonRatings) {
                    v1[i] = c.getRating1();
                    v2[i] = c.getRating2();
                    weights[i] = c.getWeight();
                    i++;
                }
                return weightedSimilarity(v1, v2, weights);

            } else {
                float[] v1 = new float[commonRatings.size()];
                float[] v2 = new float[commonRatings.size()];
                int i = 0;
                for (CommonRating c : commonRatings) {
                    v1[i] = c.getRating1();
                    v2[i] = c.getRating2();
                    i++;
                }

                return similarity(v1, v2);
            }
        } else {
            throw new CouldNotComputeSimilarity("Not enouth common ratings");
        }
    }

    @Override
    public boolean RSallowed(Class<? extends RecommenderSystemAdapter> rs) {
        return true;
    }

    @Override
    public float weightedSimilarity(List<Float> v1, List<Float> v2, List<Float> weights) throws CouldNotComputeSimilarity {
        return similarity(v1, v2);
    }
}
