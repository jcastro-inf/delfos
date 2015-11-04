package delfos.utils.hesitant.similarity.basic;

import delfos.utils.hesitant.HesitantValuation;
import delfos.utils.hesitant.similarity.HesitantSimilarity;
import delfos.utils.hesitant.similarity.PearsonCorrelationCoefficient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HesitantMinAggregation<Term extends Comparable<Term>> extends HesitantSimilarity<Term> {

    public double aggregate(Iterable<? extends Number> values) {
        double min = Double.MAX_VALUE;
        for (Number value : values) {
            if (value.doubleValue() < min) {
                min = value.doubleValue();
            }
        }
        return min;
    }

    @Override
    public double similarity(
            HesitantValuation<Term, Double> hesitantProfile1,
            HesitantValuation<Term, Double> hesitantProfile2) {

        if (!hesitantProfile1.getTerms().equals(hesitantProfile2.getTerms())) {
            throw new IllegalArgumentException("Cannot compare the hesitant valuations, the terms set are different!");
        }

        ArrayList<Term> terms = new ArrayList<>(getTerms(hesitantProfile1, hesitantProfile2));
        PearsonCorrelationCoefficient pcc = new PearsonCorrelationCoefficient();

        List<Double> profile1agg = new ArrayList<>();
        List<Double> profile2agg = new ArrayList<>();

        for (Term term : terms) {
            Collection<Double> values1 = hesitantProfile1.getTermsValues(term);
            Collection<Double> values2 = hesitantProfile2.getTermsValues(term);

            profile1agg.add(aggregate(values1));
            profile2agg.add(aggregate(values2));
        }

        return pcc.pearsonCorrelationCoefficient(profile1agg, profile2agg);
    }
}
