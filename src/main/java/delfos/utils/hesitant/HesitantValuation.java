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
package delfos.utils.hesitant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @param <Term>
 * @param <Value>
 */
public class HesitantValuation<Term, Value> {

    public static class HesitantSingleValuation<Term, Value> {

        public final Term term;
        public final Collection<Value> values;

        public HesitantSingleValuation(Term term, Value... values) {
            this.term = term;
            this.values = Arrays.asList(values);
        }

    }

    Map<Term, List<Value>> valuations;

    private HesitantValuation(Map<Term, List<Value>> valuations) {
        this.valuations = Collections.synchronizedMap(new TreeMap<>());
        valuations.entrySet().parallelStream().forEach((entry) -> {
            this.valuations.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
        });
        this.valuations = Collections.unmodifiableMap(this.valuations);
    }

    public HesitantValuation(Collection<HesitantSingleValuation<Term, Value>> valuations) {
        this.valuations = Collections.synchronizedMap(new TreeMap<>());

        for (HesitantSingleValuation<Term, Value> valuation : valuations) {
            Term term = valuation.term;
            Collection<Value> values = valuation.values;

            if (!this.valuations.containsKey(term)) {
                this.valuations.put(term, new ArrayList<>());
            }

            this.valuations.get(term).addAll(values);
        }
        this.valuations = Collections.unmodifiableMap(this.valuations);
    }

    public Set<Term> getTerms() {
        return Collections.unmodifiableSet(valuations.keySet());
    }

    public Collection<Value> getTermsValues(Term term) {
        return Collections.unmodifiableCollection(valuations.get(term));
    }

    public HesitantValuation<Term, Value> select(Set<? extends Term> terms) {

        Map<Term, List<Value>> valuationsSelected = new TreeMap<>();
        terms.stream().forEach((term) -> {
            valuationsSelected.put(term, new ArrayList<>(this.valuations.get(term)));
        });
        return new HesitantValuation<>(valuationsSelected);
    }

    @Override
    public String toString() {

        StringBuilder str = new StringBuilder();

        str.append("{");
        for (Term term : getTerms()) {
            str.append("(").append(term).append(", {");

            for (Value value : getTermsValues(term)) {
                str.append(value).append(", ");
            }
            str.setLength(str.length() - 2);
            str.append("}), ");
        }
        str.setLength(str.length() - 2);
        str.append("}");

        return str.toString();
    }

    public HesitantValuation<Term, Value> deleteRepeated(Comparator<Value> comparator) {
        Map<Term, List<Value>> valuationsCleaned = new TreeMap<>();

        for (Term term : valuations.keySet()) {

            List<Value> values = valuations.get(term);

            Set<Value> valuesCleaned = new TreeSet<>(comparator);

            valuesCleaned.addAll(values);

            valuationsCleaned.put(term, valuesCleaned.stream().collect(Collectors.toList()));
        }

        HesitantValuation<Term, Value> hesitantValuationCleaned = new HesitantValuation<>(valuationsCleaned);

        return hesitantValuationCleaned;
    }

    public int size() {
        int accumulator = 0;

        for (Term term : valuations.keySet()) {
            for (Value value : valuations.get(term)) {
                accumulator++;
            }
        }

        return accumulator;
    }
}
