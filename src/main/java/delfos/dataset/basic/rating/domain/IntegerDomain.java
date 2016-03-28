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
 * Clase que se utiliza para describir en qué dominio de valores enteros se da
 * una variable
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class IntegerDomain extends Domain {

    private static final Long serialVersionUID = 1L;

    protected Long minValue;
    protected Long maxValue;

    protected IntegerDomain() {

    }

    /**
     * Constructor de un dominio que establece el valor mínimo y máximo.
     *
     * @param minValue Valor de valoración mínimo.
     * @param maxValue Valor de valoración máximo.
     *
     * @throws IllegalArgumentException Si el valor mínimo es mayor que el
     * máximo.
     */
    public IntegerDomain(Long minValue, Long maxValue) {
        if (minValue > maxValue) {
            throw new IllegalArgumentException("The minimum value must be lower than maximum value.");
        }

        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    /**
     * Constructor de un dominio que establece el valor mínimo y máximo.
     *
     * @param minValue Valor de valoración mínimo.
     * @param maxValue Valor de valoración máximo.
     *
     * @throws IllegalArgumentException Si el valor mínimo es mayor que el
     * máximo.
     */
    public IntegerDomain(Integer minValue, Integer maxValue) {
        if (minValue > maxValue) {
            throw new IllegalArgumentException("The minimum value must be lower than maximum value.");
        }

        this.minValue = minValue.longValue();
        this.maxValue = maxValue.longValue();
    }

    /**
     * Devuelve el valor medio del dominio de valoracion.
     *
     * @return Valor medio.
     */
    public Long mean() {
        return maxValue - minValue;
    }

    /**
     * Devuelve el valor máximo del dominio de valoración.
     *
     * @return Valor máximo.
     */
    @Override
    public Long max() {
        return maxValue;
    }

    /**
     * Devuelve el valor máximo del dominio de valoración.
     *
     * @return Valor máximo.
     */
    @Override
    public Long min() {
        return minValue;
    }

    @Override
    public Long width() {
        Long width = maxValue - minValue;
        return width;
    }

    public Long numValues() {
        return width() + 1;
    }

    @Override
    public Long trimValueToDomain(Number preference) {
        Long preferenceDouble = Math.round(preference.doubleValue());

        if (minValue > preferenceDouble) {
            return minValue;
        } else if (maxValue < preferenceDouble) {
            return maxValue;
        } else {
            return preferenceDouble;
        }

    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + minValue + "," + maxValue + "]";
    }

    @Override
    public Long getValueAssociatedToProbability(Number value) {
        DecimalDomain.ZERO_TO_ONE.checkValueIsInDomain(value);

        return DecimalDomain.ZERO_TO_ONE.convertToIntegerDomain(value, this);
    }

    @Override
    public Number convertToDecimalDomain(Number valueInThisDomain, DecimalDomain destinyDomain) {
        Long value = valueInThisDomain.longValue();

        //From original to [0,1]
        Number ret = (value - this.min()) / this.width();

        //From [0,1] to [-1,1]
        ret = ret.doubleValue() * (destinyDomain.width()) + destinyDomain.min();

        return ret;
    }

    @Override
    public Long convertToIntegerDomain(Number valueInThisDomain, IntegerDomain destinyDomain) {
        Long value = valueInThisDomain.longValue();

        //From original to [0,1]
        Long ret = (value - this.min()) / this.width();

        //From [0,1] to [-1,1]
        ret = ret * (destinyDomain.width()) + destinyDomain.min();

        return ret;

    }

    public boolean isInside(int value) {
        return minValue <= value && value <= maxValue;
    }

}
