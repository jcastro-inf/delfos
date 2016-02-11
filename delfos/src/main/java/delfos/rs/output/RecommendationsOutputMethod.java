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
package delfos.rs.output;

import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.rs.recommendation.Recommendations;

/**
 * Interfaz que sirve para definir la semántica de un método de salida de las
 * recomendaciones.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 28-oct-2013
 * @version 1.1 15-Noviembre-2013
 */
public abstract class RecommendationsOutputMethod extends ParameterOwnerAdapter {

    /**
     * Regula el tamaño de la lista de recomendaciones
     */
    public static final Parameter NUMBER_OF_RECOMMENDATIONS = new Parameter("NUMBER_OF_RECOMMENDATIONS", new IntegerParameter(-1, 1000000, -1), "Size of recommendation list returned by this library");

    /**
     * Escribe las recomendaciones indicadas por parámetro.
     *
     * @param recommendations Recomendaciones a escribir
     */
    public abstract void writeRecommendations(Recommendations recommendations);

    public RecommendationsOutputMethod() {
        super();
        addParameter(NUMBER_OF_RECOMMENDATIONS);
    }

    public int getNumberOfRecommendations() {
        Integer numberOfRecommendations = (Integer) getParameterValue(NUMBER_OF_RECOMMENDATIONS);
        return numberOfRecommendations;
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.RECOMMENDATIONS_OUTPUT_METHOD;
    }
}
