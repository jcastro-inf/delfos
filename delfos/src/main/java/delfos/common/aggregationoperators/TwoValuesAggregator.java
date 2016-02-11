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
package delfos.common.aggregationoperators;

import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;

/**
 * Clase que provee los métodos para un operador de agregación que agrega dos
 * valores.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 01-Mar-2013
 */
public abstract class TwoValuesAggregator extends ParameterOwnerAdapter {

    /**
     * Método que agrega los dos valores especificados en un único valor.
     *
     * @param v1 Valor a agregar.
     * @param v2 Valor a agregar.
     * @return valor agregado de los valores especificados
     *
     * @throws IllegalArgumentException Si alguno de los parametros es null.
     */
    public abstract float aggregateTwoValues(Number v1, Number v2);

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TwoValuesAggregator) {

            Class<? extends TwoValuesAggregator> thisName = this.getClass();
            TwoValuesAggregator twoValuesAggregator = (TwoValuesAggregator) obj;
            Class<? extends TwoValuesAggregator> compareName = twoValuesAggregator.getClass();
            if (thisName.equals(compareName)) {
                return true;
            }
        }
        return false;

    }

    @Override
    public int hashCode() {
        int hash = getName().hashCode();
        return hash;
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.AGGREGATION_OPERATOR;

    }

}
