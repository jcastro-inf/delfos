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
package delfos.similaritymeasures;

import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;

/**
 * Implementación por defecto de una medida de similitud.
 *
 * <p>
 * <p>
 * La similitud es un valor entre 0 y 1, 0 cuando los vectores son completamente
 * distintos y 1 cuando son completamente iguales.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 2.0 (Unknow date) Se cambia el funcionamiento de las medidas de
 * similitud, ahora se hace mediante interfaces y adaptadores.
 * @version 1.0 (Unknow date)
 */
public abstract class SimilarityMeasureAdapter extends ParameterOwnerAdapter implements SimilarityMeasure {

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SimilarityMeasure) {
            SimilarityMeasure similarityMeasure = (SimilarityMeasure) obj;
            return getName().equals(similarityMeasure.getName());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = getName().hashCode();
        return hash;
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.SIMILARITY_MEASURE;
    }

}
