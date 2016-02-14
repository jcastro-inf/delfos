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
package delfos.rs.contentbased.vsm;

import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.similaritymeasures.CosineCoefficient;
import delfos.similaritymeasures.WeightedSimilarityMeasure;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 5-marzo-2014
 */
public class ContentBasedVSMRS {

    /**
     * Parámetro que almacena la medida de similitud que el sistema utiliza.
     */
    public static final Parameter SIMILARITY_MEASURE = new Parameter(
            "Similarity_measure",
            new ParameterOwnerRestriction(WeightedSimilarityMeasure.class, new CosineCoefficient()),
            "Parámetro que almacena la medida de similitud que el sistema utiliza."
    );

}
