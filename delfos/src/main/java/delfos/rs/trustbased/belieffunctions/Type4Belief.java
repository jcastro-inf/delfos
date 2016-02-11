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
package delfos.rs.trustbased.belieffunctions;

import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;

/**
 *
 * @version 14-abr-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class Type4Belief extends BeliefFunction {

    public static final Parameter K_PARAMETER = new Parameter("K", new IntegerParameter(1, 100, 3));

    public Type4Belief() {
        super();
        addParameter(K_PARAMETER);
    }

    public Type4Belief(int k) {
        this();

        setParameterValue(K_PARAMETER, k);
    }

    @Override
    public double beliefFromCorrelation(double correlation) {

        double k = ((Number) getParameterValue(K_PARAMETER)).doubleValue();

        double ret = 0.5 * (1 + Math.pow(correlation, k));

        return ret;
    }

}
