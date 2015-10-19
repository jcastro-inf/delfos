package delfos.common.decimalnumbers;

import static delfos.Constants.COMPARE_NUM_DECIMALS;

/**
 *
 * @author Jorge Castro Gallardo
 * @version 1.0 22-Mar-2013
 */
public class NumberCompare {

    /**
     * Compara dos valores, teniendo en cuenta sólo un determinado número de
     * decimales.
     *
     * @param n1 Número a comparar.
     * @param n2 Número a comparar.
     * @return true, si los valores son iguales hasta el decimal indicado, false
     * en otro caso.
     *
     * @throws IllegalArgumentException Si el número de decimales es negativo.
     */
    public static boolean equals(Number n1, Number n2) {

        if (COMPARE_NUM_DECIMALS < 0) {
            throw new IllegalArgumentException("Number of decimals can't be negative.");
        }

        double p1 = n1.doubleValue();
        double p2 = n2.doubleValue();

        double diff = p1 - p2;

        if (diff == 0) {
            return true;
        } else if (diff < Math.pow(10, -COMPARE_NUM_DECIMALS)) {
            return true;
        } else if (diff > Math.pow(10, -COMPARE_NUM_DECIMALS)) {
            final int value = (int) Math.pow(10, COMPARE_NUM_DECIMALS);

            p1 = (int) (p1 * value);
            p1 = p1 / value;

            p2 = (int) (p2 * value);
            p2 = p2 / value;

            return p1 == p2;
        } else if (Double.isNaN(n1.doubleValue()) && Double.isNaN(n2.doubleValue())) {
            return true;
        } else {
            throw new IllegalStateException("asdf");
        }
    }
}
