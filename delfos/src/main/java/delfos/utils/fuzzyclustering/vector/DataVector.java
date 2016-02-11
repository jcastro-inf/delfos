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
package delfos.utils.fuzzyclustering.vector;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @version 15-sep-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * @param <Key>
 */
public class DataVector<Key> {

    private final Map<Key, Double> values;

    public DataVector() {
        values = new TreeMap<>();
    }

    public DataVector(DataVector<Key> dataVector) {
        this.values = new TreeMap<>(dataVector.values);
    }

    public DataVector(Map<Key, Double> mapValues) {
        this.values = new TreeMap<>(mapValues);
    }

    public Double put(Key key, Double value) {
        return values.put(key, value);
    }

    public int size() {
        return values.size();
    }

    boolean isEmpty() {
        return values.isEmpty();
    }

    public boolean containsKey(Key key) {
        return values.containsKey(key);
    }

    public Set<Key> keySet() {
        return values.keySet();
    }

    public Set<Entry<Key, Double>> entrySet() {
        return values.entrySet();
    }

    public Double get(Key key) {
        return values.get(key);
    }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat();

        df.setMinimumFractionDigits(3);
        df.setMaximumFractionDigits(3);

        String ret = "{";

        Iterator<Entry<Key, Double>> iterator = values.entrySet().iterator();

        {
            Map.Entry<Key, Double> entry = iterator.next();

            Key key
                    = entry.getKey();
            Double value = entry.getValue();

            ret = ret + key.toString() + "=" + df.format(value);
        }

        for (; iterator.hasNext();) {
            Map.Entry<Key, Double> entry = iterator.next();

            Key key = entry.getKey();
            Double value = entry.getValue();

            ret = ret + ", " + key.toString() + "=" + df.format(value);
        }

        ret = ret + "}";
        return ret;
    }
}
