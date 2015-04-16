package delfos.similaritymeasures;

import java.util.Iterator;
import java.util.List;
import delfos.common.exceptions.CouldNotComputeSimilarity;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * 
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class RefinedHammingDistance extends BasicSimilarityMeasureAdapter {

    @Override
    public float similarity(List<Float> v1, List<Float> v2) throws CouldNotComputeSimilarity {
        if (v1.size() != v2.size() || v1.isEmpty()) {
            throw new CouldNotComputeSimilarity("Not enought values");
        }
        float distancia = 0;
        Iterator<Float> i1 = v1.listIterator();
        Iterator<Float> i2 = v2.listIterator();
        for (int i = 0; i < v1.size(); i++) {
            distancia += Math.abs(i1.next() - i2.next());
        }
        return 1 / (1 + distancia);
    }
}
