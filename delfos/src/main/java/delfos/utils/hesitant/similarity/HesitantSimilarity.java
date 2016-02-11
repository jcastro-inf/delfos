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
package delfos.utils.hesitant.similarity;

import delfos.utils.hesitant.HesitantValuation;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author jcastro
 * @param <Term>
 */
public abstract class HesitantSimilarity<Term> implements Comparable<HesitantSimilarity> {

    public abstract double similarity(
            HesitantValuation<Term, Double> hesitantProfile1,
            HesitantValuation<Term, Double> hesitantProfile2);

    public final String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj instanceof HesitantSimilarity) {
            HesitantSimilarity hesitantSimilarity = (HesitantSimilarity) obj;
            return this.getName().equals(hesitantSimilarity.getName());
        } else {
            return super.equals(obj); //To change body of generated methods, choose Tools | Templates.
        }
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public final String toString() {
        return getName();
    }

    @Override
    public int compareTo(HesitantSimilarity o) {
        return this.getName().compareTo(o.getName());
    }

    protected static <Term> Set<Term> getTerms(
            HesitantValuation<Term, Double> hesitantProfile1,
            HesitantValuation<Term, Double> hesitantProfile2) {
        Set<Term> terms = new TreeSet<>();
        terms.addAll(hesitantProfile1.getTerms());
        terms.addAll(hesitantProfile2.getTerms());
        return terms;
    }
}
