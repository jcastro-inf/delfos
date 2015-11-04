package delfos.utils.hesitant.similarity.basic;

import delfos.utils.hesitant.HesitantValuation;
import delfos.utils.hesitant.similarity.HesitantSimilarity;
import delfos.utils.hesitant.similarity.PearsonCorrelationCoefficient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HesitantMeanAggregation<Term extends Comparable<Term>> extends HesitantSimilarity<Term> {

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

            double mean1 = 0;
            for (Double value : values1) {
                mean1 += value;
            }
            mean1 /= values1.size();

            double mean2 = 0;
            for (Double value : values2) {
                mean2 += value;
            }
            mean2 /= values2.size();

            profile1agg.add(mean1);
            profile2agg.add(mean2);
        }

        return pcc.pearsonCorrelationCoefficient(profile1agg, profile2agg);
    }

}
