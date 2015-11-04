package delfos.utils.hesitant.similarity;

import delfos.utils.hesitant.HesitantValuation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class HesitantPearson<Term extends Comparable<Term>> extends HesitantSimilarity<Term> {

    public static <Term> Collection<Pair<Double>> getValuesCartesianProduct(
            HesitantValuation<Term, Double> hesitantProfile1,
            HesitantValuation<Term, Double> hesitantProfile2,
            Term term) {

        Collection<Double> termsValues1 = hesitantProfile1.getTermsValues(term);
        Collection<Double> termsValues2 = hesitantProfile2.getTermsValues(term);

        Collection<Pair<Double>> cartesianProduct = new ArrayList<>(termsValues1.size() * termsValues2.size());

        for (Double value1 : termsValues1) {
            for (Double value2 : termsValues2) {
                cartesianProduct.add(new Pair<>(value1, value2));
            }
        }

        return cartesianProduct;
    }

    public static <Term> int getSizeR_HFS(HesitantValuation<Term, Double> hesitantProfile1, HesitantValuation<Term, Double> hesitantProfile2) {

        int sizeR_HFS = 0;
        for (Term term : getTerms(hesitantProfile1, hesitantProfile2)) {
            sizeR_HFS += hesitantProfile1.getTermsValues(term).size() * hesitantProfile2.getTermsValues(term).size();
        }
        return sizeR_HFS;
    }

    public static class Pair<Type> {

        public Type key1;
        public Type key2;

        public Pair(Type key1, Type key2) {
            this.key1 = key1;
            this.key2 = key2;
        }

        @Override
        public String toString() {
            return "(" + key1 + "," + key2 + ")";
        }

    }

    @Override
    public double similarity(
            HesitantValuation<Term, Double> hesitantProfile1,
            HesitantValuation<Term, Double> hesitantProfile2) {

        if (!hesitantProfile1.getTerms().equals(hesitantProfile2.getTerms())) {
            throw new IllegalArgumentException("Cannot compare the hesitant valuations, the terms set are different!");
        }

        double SCC_XY = getSCC(hesitantProfile1, hesitantProfile2);
        double SSH_X = getSSHX(hesitantProfile1, hesitantProfile2);
        double SSH_Y = getSSHY(hesitantProfile1, hesitantProfile2);

        if (SSH_X == 0) {
            return Double.NEGATIVE_INFINITY;
        } else if (SSH_Y == 0) {
            return Double.NEGATIVE_INFINITY;
        } else {

            double denominator = Math.sqrt(SSH_X) * Math.sqrt(SSH_Y);
            double pearson = SCC_XY / denominator;

            return pearson;
        }
    }

    public double getMeanHX(
            HesitantValuation<Term, Double> hesitantProfile1,
            HesitantValuation<Term, Double> hesitantProfile2) {
        double sum = 0;
        int num = 0;

        Set<Term> terms = getTerms(hesitantProfile1, hesitantProfile2);

        for (Term term : terms) {
            Collection<Pair<Double>> valuesCartesianProduct = getValuesCartesianProduct(hesitantProfile1, hesitantProfile2, term);
            for (Pair<Double> pair : valuesCartesianProduct) {
                sum += pair.key1;
                num++;
            }
        }
        return sum / num;
    }

    public double getMeanHY(
            HesitantValuation<Term, Double> hesitantProfile1,
            HesitantValuation<Term, Double> hesitantProfile2) {
        double meanHY = getMeanHX(hesitantProfile2, hesitantProfile1);
        return meanHY;
    }

    double getSCC(
            HesitantValuation<Term, Double> hesitantProfile1,
            HesitantValuation<Term, Double> hesitantProfile2) {

        double meanHX = getMeanHX(hesitantProfile1, hesitantProfile2);
        double meanHY = getMeanHX(hesitantProfile2, hesitantProfile1);
        Set<Term> terms = getTerms(hesitantProfile1, hesitantProfile2);

        double SCC = 0;

        for (Term term : terms) {
            Collection<Double> xValues = hesitantProfile1.getTermsValues(term);
            Collection<Double> yValues = hesitantProfile2.getTermsValues(term);
            for (Double xValue : xValues) {
                for (Double yValue : yValues) {
                    double xPart = (xValue - meanHX);
                    double yPart = (yValue - meanHY);
                    double product = xPart * yPart;

                    SCC += product;
                }
            }
        }

        return SCC;
    }

    double getSSHX(HesitantValuation<Term, Double> hesitantProfile1, HesitantValuation<Term, Double> hesitantProfile2) {

        final double meanHX = getMeanHX(hesitantProfile1, hesitantProfile2);
        final Set<Term> terms = getTerms(hesitantProfile1, hesitantProfile2);

        double SSHX = 0;
        for (Term term : terms) {
            double innerSum = 0;

            Collection<Double> xValues = hesitantProfile1.getTermsValues(term);
            for (Double xValue : xValues) {
                double xPart = (xValue - meanHX);
                double xPartSquare = Math.pow(xPart, 2);
                innerSum += xPartSquare;
            }

            final int numYValues = hesitantProfile2.getTermsValues(term).size();
            innerSum *= numYValues;

            SSHX += innerSum;
        }
        return SSHX;

    }

    double getSSHY(HesitantValuation<Term, Double> hesitantProfile1, HesitantValuation<Term, Double> hesitantProfile2) {
        double SSHY = getSSHX(hesitantProfile2, hesitantProfile1);
        return SSHY;
    }
}
