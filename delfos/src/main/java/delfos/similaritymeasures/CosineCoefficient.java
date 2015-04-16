package delfos.similaritymeasures;

import java.util.Iterator;
import java.util.List;
import delfos.common.exceptions.CouldNotComputeSimilarity;

/**
 * Clase que implementa la medida del coseno para realizar una medida de
 * similitud de dos vectores
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class CosineCoefficient extends WeightedSimilarityMeasureAdapter {

    private static final long serialVersionUID = 1L;

    @Override
    public float weightedSimilarity(List<Float> v1, List<Float> v2, List<Float> weights) throws CouldNotComputeSimilarity {
        if (v1.size() != v2.size() || v1.size() != weights.size()) {
            throw new IllegalArgumentException("The vector lengths are different");
        }

        float numerator = 0;
        float denominator1 = 0, denominator2 = 0;
        float sumPesos = 0;
        Iterator<Float> i1 = v1.listIterator();
        Iterator<Float> i2 = v2.listIterator();
        Iterator<Float> iw = weights.listIterator();
        for (int i = 0; i < v1.size(); i++) {
            float r1 = i1.next();
            float r2 = i2.next();
            float w = iw.next();

            numerator = numerator + r1 * r2 * w;
            denominator1 = denominator1 + r1 * r1 * w;
            denominator2 = denominator2 + r2 * r2 * w;

            sumPesos += w;
        }

        if (sumPesos == 0) {
            throw new CouldNotComputeSimilarity("Sum of weights is zero");
        }
        if (sumPesos > 1.01) {
            throw new CouldNotComputeSimilarity("Sum of weights is greater than 1: '" + sumPesos + "'.");
        }

        if (denominator1 == 0 || denominator2 == 0) {
            return 0;
        } else {
            float coseno = (float) (numerator / (Math.sqrt(denominator1) * Math.sqrt(denominator2)));
            return coseno;
        }
    }
}
