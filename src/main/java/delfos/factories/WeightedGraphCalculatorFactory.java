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
package delfos.factories;

import delfos.rs.trustbased.JaccardGraph;
import delfos.rs.trustbased.PearsonCorrelationWithPenalty;
import delfos.rs.trustbased.WeightedGraphCalculation;
import delfos.rs.trustbased.implicittrustcomputation.ShambourLu_UserBasedImplicitTrustComputation;

/**
 * Factoría que conoce los algoritmos de generación de grafos ponderados que la
 * biblioteca de recomendación incorppora.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 11-Septiembre-2013
 *
 * @see WeightedGraphCalculation
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class WeightedGraphCalculatorFactory extends Factory<WeightedGraphCalculation> {

    private static final WeightedGraphCalculatorFactory instance;

    public static WeightedGraphCalculatorFactory getInstance() {
        return instance;
    }

    static {
        instance = new WeightedGraphCalculatorFactory();
        instance.addClass(JaccardGraph.class);
        instance.addClass(ShambourLu_UserBasedImplicitTrustComputation.class);

        instance.addClass(PearsonCorrelationWithPenalty.class);
    }
}
