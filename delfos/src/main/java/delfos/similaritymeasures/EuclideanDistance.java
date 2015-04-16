package delfos.similaritymeasures;

import java.util.Iterator;
import java.util.List;

/**
 * Medida de similitud basada en la distancia euclídea. Ha sido modificada para
 * que 1 corresponda a similitud perfecta y sea menor cuanto más diferente hasta
 * 0. Ésto se ha conseguido sumandole 1 (para evitar división por cero) y
 * calculando la inversa. NOTA: fórmula extraída de Programming Collective
 * Intelligence by Toby Segaran. Copyright 2007 Toby Segaran, 978-0-596-52932-1
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class EuclideanDistance extends BasicSimilarityMeasureAdapter {

    private static final long serialVersionUID = 1L;

    @Override
    public float similarity(List<Float> v1, List<Float> v2) {
        double sum = 0;
        Iterator<Float> i1 = v1.listIterator();
        Iterator<Float> i2 = v2.listIterator();
        for (int i = 0; i < v1.size(); i++) {
            sum += Math.pow(i1.next() - i2.next(), 2);
        }
        sum = 1 / (1 + Math.sqrt(sum));
        return (float) sum;
    }

    @Override
    public float weightedSimilarity(List<Float> v1, List<Float> v2, List<Float> weights) {
        double sum = 0;
        Iterator<Float> i1 = v1.listIterator();
        Iterator<Float> i2 = v2.listIterator();
        Iterator<Float> w = weights.listIterator();
        for (int i = 0; i < v1.size(); i++) {
            sum += w.next() * Math.pow(i1.next() - i2.next(), 2);
        }
        sum = 1 / (1 + Math.sqrt(sum));
        return (float) sum;
    }
}
