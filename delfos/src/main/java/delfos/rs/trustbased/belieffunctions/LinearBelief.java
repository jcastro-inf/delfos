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

/**
 *
 * @version 14-abr-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class LinearBelief extends BeliefFunction {

    @Override
    public double beliefFromCorrelation(double correlation) {
        if (correlation > 1 || correlation < -1) {
            throw new IllegalArgumentException("The correlation must be given in [-1,1] ( value " + correlation + ")");
        }

        return (correlation + 1) / (2.0);
    }

}
