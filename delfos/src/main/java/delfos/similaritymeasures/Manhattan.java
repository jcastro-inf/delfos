package delfos.similaritymeasures;

import java.util.Iterator;
import java.util.List;
import delfos.common.exceptions.CouldNotComputeSimilarity;

/**
 * Medida de similitud que utiliza la medida de manhatan. La medida de manhatan
 * se comporta como la distancia en una cuadrícula, sin utilizar desplazamientos
 * en diagonal.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class Manhattan extends BasicSimilarityMeasureAdapter {

    private static final long serialVersionUID = 1L;

    @Override
    public float similarity(List<Float> v1, List<Float> v2) throws CouldNotComputeSimilarity {
        if (v1.size() != v2.size()) {
            throw new CouldNotComputeSimilarity("Vector size is diferent");
        }
        Iterator<Float> i1 = v1.listIterator();
        Iterator<Float> i2 = v2.listIterator();
        float distance = 0;
        while (i1.hasNext()) {
            Float n1 = i1.next();
            Float n2 = i2.next();
            distance += Math.abs(n1 - n2);
        }
        return 1 / (1 + distance);
    }
}
