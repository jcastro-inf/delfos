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
package delfos.rs.contentbased.vsm.booleanvsm;

import delfos.ERROR_CODES;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.math4.util.Pair;

/**
 *
 * @author jcastro
 * @param <Key>
 */
public class SparseVector<Key extends Comparable<Key>> implements Cloneable {

    private final Map<Key, Double> map;
    private final Set<Key> domain;

    public SparseVector(Collection<Key> domain) {
        map = Collections.synchronizedMap(new HashMap<>());

        this.domain = Collections.synchronizedSet(domain.stream().collect(Collectors.toSet()));
    }

    public static final <Key extends Comparable<Key>> SparseVector<Key> createFromPairs(Pair<Key, Double>... pairs) {

        SparseVector<Key> sparseVector = new SparseVector<>(Arrays.stream(pairs).map(entry -> entry.getKey()).collect(Collectors.toList()));
        Arrays.stream(pairs).forEach(pair -> sparseVector.map.put(pair.getKey(), pair.getValue()));

        return sparseVector;
    }

    public static <Key extends Comparable<Key>> SparseVector<Key> create(Collection<Key> domain) {
        return new SparseVector<>(domain);
    }

    public List<Pair<Key, Double>> entrySet() {
        return map.entrySet().stream()
                .sorted((entry1, entry2) -> entry1.getKey().compareTo(entry2.getKey()))
                .map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<Pair<Key, Double>> fast() {
        return entrySet();
    }

    public void set(Key key, double value) {
        map.put(key, value);
    }

    @Override
    public SparseVector<Key> clone() {
        try {
            return (SparseVector<Key>) super.clone();
        } catch (CloneNotSupportedException ex) {
            ERROR_CODES.UNDEFINED_ERROR.exit(ex);
            throw new IllegalStateException(ex);
        }
    }

    public boolean containsKey(Key key) {
        return map.containsKey(key);
    }

    public Double get(Key key) {
        return map.get(key);
    }

    public void fill(double value) {
        map.clear();
        domain.parallelStream().forEach(key -> map.put(key, value));
    }

    public void sum(SparseVector<Key> itemProfile) {
        itemProfile.fast().parallelStream().forEach(itemEntry -> {
            Key key = itemEntry.getKey();
            Double value = itemEntry.getValue();
            Double thisValue = map.get(key);
            final double addedValue = value + thisValue;
            map.put(key, addedValue);
        });
    }

    public double multiply(SparseVector<Key> iuf) {

        Set<Key> intersectionDomain = map.keySet().stream().filter(key -> iuf.containsKey(key)).collect(Collectors.toSet());

        double sum = intersectionDomain.stream().mapToDouble(key -> map.get(key) * iuf.map.get(key)).sum();
        return sum;
    }

    public double norm() {
        return map.values().stream().mapToDouble(value -> value).sum();
    }

    public void add(Key key, double value) {
        if (map.containsKey(key)) {
            double addedValue = map.get(key) + value;
            map.put(key, addedValue);
        } else {
            map.put(key, value);
        }
    }

    public void add(SparseVector<Key> sparseVector) {
        this.sum(sparseVector);
    }

    public Set<Key> keySet() {
        return map.keySet();
    }
}
