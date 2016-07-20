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

import java.io.Serializable;

public abstract class Domain implements Serializable {

    public abstract Number getValueAssociatedToProbability(Number value);

    public abstract Number max();

    public abstract Number min();

    public abstract Number trimValueToDomain(Number preference);

    public Number convertToDomain(Number valueInThisDomain, Domain destinyDomain) {
        if (destinyDomain instanceof IntegerDomain) {
            IntegerDomain integerDomain = (IntegerDomain) destinyDomain;
            return convertToIntegerDomain(valueInThisDomain, integerDomain);
        }

        if (destinyDomain instanceof DecimalDomain) {
            DecimalDomain decimalDomain = (DecimalDomain) destinyDomain;
            return convertToDecimalDomain(valueInThisDomain, decimalDomain);
        }

        throw new IllegalStateException("Unknown destiny domain type '" + destinyDomain.getClass() + "'");
    }

    public abstract Number convertToDecimalDomain(Number valueInThisDomain, DecimalDomain destinyDomain);

    public abstract Number convertToIntegerDomain(Number valueInThisDomain, IntegerDomain destinyDomain);

    public static long drawnInteger(double value, IntegerDomainWithProbabilities domain) {
        return domain.getValueAssociatedToProbability(value);
    }

    public abstract Number width();

    public abstract Number mean();

    public abstract boolean isValueInDomain(Number value);

}
