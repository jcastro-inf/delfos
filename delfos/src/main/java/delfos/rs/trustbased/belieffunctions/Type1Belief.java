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
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class Type1Belief extends BeliefFunction {

    @Override
    public double beliefFromCorrelation(double correlation) {

        double ret;

        final double radians = correlation * (Math.PI / 2);

        double sin = Math.sin(radians);

        sin++;

        ret = 0.5 * sin;

        return ret;
    }

}
