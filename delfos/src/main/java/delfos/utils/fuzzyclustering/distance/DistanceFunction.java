package delfos.utils.fuzzyclustering.distance;

import delfos.utils.fuzzyclustering.vector.DataVector;

/**
 *
 * @version 15-sep-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public abstract class DistanceFunction {

    public abstract <Key> double distance(DataVector<Key> vector1, DataVector<Key> vector2);
}
