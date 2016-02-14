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
package delfos.utils.hesitant.similarity.basic;

import delfos.utils.hesitant.HesitantValuation;
import delfos.utils.hesitant.similarity.HesitantSimilarity;
import delfos.utils.hesitant.similarity.PearsonCorrelationCoefficient;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HesitantRMSMeanAggregation<Term extends Comparable<Term>> extends HesitantSimilarity<Term> {

    public double rmsMean(Iterable<? extends Number> values) {
        MathContext mathContext = new MathContext(32, RoundingMode.HALF_UP);
        BigDecimal sum = new BigDecimal(BigInteger.ZERO, mathContext);
        int n = 0;

        for (Number value : values) {
            BigDecimal valueB = new BigDecimal(value.doubleValue(), mathContext);
            sum = sum.add(valueB.multiply(valueB, mathContext), mathContext);
            n++;
        }

        BigDecimal meanSquare = sum.multiply(BigDecimal.ONE.divide(new BigDecimal(n), mathContext));

        double rootMeanSquare = Math.sqrt(meanSquare.doubleValue());
        return rootMeanSquare;
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

            profile1agg.add(rmsMean(values1));
            profile2agg.add(rmsMean(values2));
        }

        return pcc.pearsonCorrelationCoefficient(profile1agg, profile2agg);
    }
}
