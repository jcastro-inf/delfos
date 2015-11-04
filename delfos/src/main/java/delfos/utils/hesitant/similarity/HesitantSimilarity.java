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
