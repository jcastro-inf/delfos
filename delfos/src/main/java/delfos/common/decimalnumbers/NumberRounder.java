/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package delfos.common.decimalnumbers;

import java.text.DecimalFormat;

/**
 * Redondea números decimales.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 24-Apr-2013
 */
public class NumberRounder {

    /**
     * Redondea el valor indicado al número de decimales indicado.
     *
     * @param value Valor que se quiere redondear.
     * @param numDecimals Número de decimales que se consideran.
     * @return Valor redondeado
     */
    public static double round(Number value, int numDecimals) {
        double doubleValue = value.doubleValue();
        if (Double.isInfinite(doubleValue) || Double.isNaN(doubleValue)) {
            return doubleValue;
        }

        if (numDecimals < 0) {
            throw new IllegalArgumentException("Number of decimals can't be negative.");
        }

        double pow = Math.pow(10, numDecimals);

        int intValue = (int) (value.doubleValue() * pow);

        double ret = intValue / pow;
        return ret;
    }

    public static String round_str(Number value) {

        double doubleValue = value.doubleValue();
        if (Double.isInfinite(doubleValue)) {
            return "Inf";
        }

        if (Double.isNaN(doubleValue)) {
            return "NaN";
        }

        DecimalFormat df = new DecimalFormat();

        df.setMinimumFractionDigits(8);
        df.setMaximumFractionDigits(8);

        String format = df.format(value);
        return format;
    }
}
