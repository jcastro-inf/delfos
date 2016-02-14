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
package delfos.dataset.basic.rating.domain;

/**
 * Clase que se utiliza para describir en qué dominio decimal se da una variable
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class DecimalDomain extends Domain {

    private static final long serialVersionUID = 1L;

    public static final DecimalDomain ZERO_TO_ONE = new DecimalDomain(0, 1);

    private final Double minValue;
    private final Double maxValue;

    /**
     * Constructor de un dominio que establece el valor mínimo y máximo.
     *
     * @param minValue Valor de valoración mínimo.
     * @param maxValue Valor de valoración máximo.
     *
     * @throws IllegalArgumentException Si el valor mínimo es mayor que el
     * máximo.
     */
    public DecimalDomain(Number minValue, Number maxValue) {
        this.minValue = minValue.doubleValue();
        this.maxValue = maxValue.doubleValue();

        if (this.minValue >= this.maxValue) {
            throw new IllegalArgumentException("The minimum value must be lower than maximum value.");
        }
    }

    /**
     * Devuelve el valor medio del dominio de valoracion.
     *
     * @return Valor medio.
     */
    @Override
    public Double mean() {
        return (maxValue + minValue) / 2;
    }

    /**
     * Devuelve el valor máximo del dominio de valoración.
     *
     * @return Valor máximo.
     */
    @Override
    public Double max() {
        return maxValue;
    }

    /**
     * Devuelve el valor máximo del dominio de valoración.
     *
     * @return Valor máximo.
     */
    @Override
    public Double min() {
        return minValue;
    }

    @Override
    public Double convertToDecimalDomain(Number valueInThisDomain, DecimalDomain destinyDomain) {
        checkValueIsInDomain(valueInThisDomain);

        //From original to [0,1]
        Double ret = (valueInThisDomain.doubleValue() - this.min()) / this.width();

        //From [0,1] to destiny
        Double valueInDestinyDomain = ret * (destinyDomain.width()) + destinyDomain.min();

        return valueInDestinyDomain;
    }

    @Override
    public Long convertToIntegerDomain(Number valueInThisDomain, IntegerDomain destinyDomain) {
        checkValueIsInDomain(valueInThisDomain);

        //From original to [0,1]
        Double valueInzeroToOne = (valueInThisDomain.doubleValue() - this.min()) / (this.width());

        //From [0,1] to [-1,1]
        long valueInDestinyDomain = (long) ((valueInzeroToOne * destinyDomain.numValues()) + destinyDomain.min());

        return valueInDestinyDomain;
    }

    @Override
    public Double width() {
        return maxValue - minValue;
    }

    @Override
    public Number trimValueToDomain(Number preference) {
        Double preferenceDouble = preference.doubleValue();

        if (minValue > preferenceDouble) {
            return minValue;
        } else if (maxValue < preferenceDouble) {
            return maxValue;
        } else {
            return preference;
        }

    }

    public boolean isValueInDomain(Number value) {
        if (value.doubleValue() < minValue) {
            return false;
        } else if (value.doubleValue() > maxValue) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + minValue + "," + maxValue + "]";
    }

    public void checkValueIsInDomain(Number value) {
        if (!isValueInDomain(value)) {
//            throw new IllegalArgumentException("The value '" + value + "' is not in this domain " + this.toString());
        }
    }

    @Override
    public Double getValueAssociatedToProbability(Number probability) {
        ZERO_TO_ONE.checkValueIsInDomain(probability);

        double valueInThisDomain = ZERO_TO_ONE.convertToDecimalDomain(probability, this);
        return valueInThisDomain;
    }

}
