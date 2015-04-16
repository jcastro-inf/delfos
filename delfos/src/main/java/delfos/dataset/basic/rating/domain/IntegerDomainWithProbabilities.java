package delfos.dataset.basic.rating.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import delfos.common.Global;

/**
 * Clase que se utiliza para describir en qué dominio discreto se da una
 * variable y con qué distribución de probabilidad.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class IntegerDomainWithProbabilities extends IntegerDomain {

    static final String NOT_UNIQUES = "The values are not uniques!";
    static final String NOT_CORRELATIVES = "the values are not correlatives";
    static final String MINIMUM_EQUALS_MAXIMUM = "The minimum should be different to the maximum value.";

    private static final long serialVersionUID = 5119898L;

    List<ValueWithProbability> valuesWithProbability;

    public static class ValueWithProbability {

        long value;
        double probability;
        double acum;

        public ValueWithProbability(long value, double probability) {
            this.value = value;
            this.probability = probability;
            acum = Double.NaN;
        }

        public ValueWithProbability(long value, double probability, double acum) {
            this.value = value;
            this.probability = probability;
            this.acum = acum;
        }

        private static double norm(List<ValueWithProbability> valuesWithProbability) {
            double probabilitiesNorm = 0;
            probabilitiesNorm = valuesWithProbability.stream()
                    .map((valueWithProbability) -> valueWithProbability.probability)
                    .reduce(probabilitiesNorm, (accumulator, _item) -> accumulator + _item);

            return probabilitiesNorm;
        }

        private static void checkCorrelativesAndUnique(List<ValueWithProbability> valuesWithProbability) {
            List<Long> values = new ArrayList<>(valuesWithProbability.size());

            valuesWithProbability.stream().forEach((valueWithProbability) -> {
                values.add(valueWithProbability.value);
            });

            Collections.sort(values);

            if (values.size() != new TreeSet<>(values).size()) {
                Global.showWarning(NOT_UNIQUES + " " + values.toString());
                throw new IllegalArgumentException(NOT_UNIQUES);
            }

            long firstValue = values.get(0);
            for (int i = 0; i < values.size(); i++) {
                if (values.get(i) != firstValue + i) {
                    throw new IllegalArgumentException(NOT_CORRELATIVES);
                }
            }

        }

        private static long getMinValue(List<ValueWithProbability> valuesWithProbability) {
            List<Long> values = new ArrayList<>(valuesWithProbability.size());

            valuesWithProbability.stream().forEach((valueWithProbability) -> {
                values.add(valueWithProbability.value);
            });
            Collections.sort(values);

            long firstValue = values.get(0);

            return firstValue;
        }

        private static long getMaxValue(List<ValueWithProbability> valuesWithProbability) {
            List<Long> values = new ArrayList<>(valuesWithProbability.size());

            valuesWithProbability.stream().forEach((valueWithProbability) -> {
                values.add(valueWithProbability.value);
            });
            Collections.sort(values);

            long lastValue = values.get(values.size() - 1);

            return lastValue;
        }
    }

    public IntegerDomainWithProbabilities(List<ValueWithProbability> valuesWithProbability) {
        super();

        ValueWithProbability.checkCorrelativesAndUnique(valuesWithProbability);

        minValue = ValueWithProbability.getMinValue(valuesWithProbability);
        maxValue = ValueWithProbability.getMaxValue(valuesWithProbability);

        if (minValue.equals(maxValue)) {
            throw new IllegalArgumentException(MINIMUM_EQUALS_MAXIMUM);
        }

        double norm = ValueWithProbability.norm(valuesWithProbability);

        this.valuesWithProbability = new ArrayList<>();
        double acum = 0;
        for (ValueWithProbability valueWithProbability : valuesWithProbability) {
            double probabilityNormalised = valueWithProbability.probability / norm;
            acum = acum + probabilityNormalised;
            this.valuesWithProbability.add(new ValueWithProbability(valueWithProbability.value, probabilityNormalised, acum));
        }
    }

    @Override
    public Long getValueAssociatedToProbability(Number probability) {
        DecimalDomain.ZERO_TO_ONE.checkValueIsInDomain(probability);

        double probabilityValue = probability.doubleValue();

        for (ValueWithProbability valueWithProbability : valuesWithProbability) {
            if (probabilityValue < valueWithProbability.acum) {
                return valueWithProbability.value;
            }
        }

        if (probabilityValue <= 1 && probabilityValue > 0.9999) {
            return valuesWithProbability.get(valuesWithProbability.size() - 1).value;
        }

        throw new IllegalStateException("Cannot reach this point, a value should have been selected");
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + minValue + "," + maxValue + "]";
    }

}
