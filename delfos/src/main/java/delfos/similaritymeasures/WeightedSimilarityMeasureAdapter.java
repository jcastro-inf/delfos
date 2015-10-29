package delfos.similaritymeasures;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.RecommenderSystemAdapter;
import delfos.rs.collaborativefiltering.knn.CommonRating;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Clase que adapta las medidas de similitud completando los métodos de medida
 * de simlitud básica mediante la adición de ponderación en la que todas las
 * dimensiones del vector tienen la misma ponderación.
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
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public abstract class WeightedSimilarityMeasureAdapter extends SimilarityMeasureAdapter implements BasicSimilarityMeasure, WeightedSimilarityMeasure, CollaborativeSimilarityMeasure {

    @Override
    public final float similarity(float[] v1, float[] v2) {

        if (v1.length != v2.length) {
            throw new IllegalArgumentException("The length of vectors is different");
        }

        List<Float> v1List = new ArrayList<>(v1.length);
        List<Float> v2List = new ArrayList<>(v2.length);
        List<Float> weightsList = new ArrayList<>(v1.length);

        for (int i = 0; i < v1.length; i++) {
            v1List.add(v1[i]);
            v2List.add(v2[i]);
            weightsList.add(1.0f / v1.length);
        }
        return weightedSimilarity(v1List, v2List, weightsList);
    }

    @Override
    public final float weightedSimilarity(float[] v1, float[] v2, float[] weights) {

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
    public float similarity(List<Float> v1, List<Float> v2) {
        List<Float> weights = new ArrayList<>(v1.size());
        for (int i = 0; i < v1.size(); i++) {
            weights.add((float) (1.0 / v1.size()));
        }
        return weightedSimilarity(v1, v2, weights);
    }

    @Override
    public float similarity(Collection<CommonRating> commonRatings, RatingsDataset<? extends Rating> ratings) {
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
            return Float.NaN;
        }
    }

    @Override
    public boolean RSallowed(Class<? extends RecommenderSystemAdapter> rs) {
        return true;
    }
}
