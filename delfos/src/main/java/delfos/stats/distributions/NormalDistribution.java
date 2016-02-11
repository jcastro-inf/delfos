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
package delfos.stats.distributions;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 07-ene-2014
 */
public final class NormalDistribution {

    public static final double z(final double mean, final double variance, final double x) {
        final double standardDeviation = Math.sqrt(variance);

        double firstPart = 1 / (standardDeviation * Math.sqrt(2 * Math.PI));

        double secondPart = Math.pow(Math.E, -(Math.pow(x - mean, 2) / (2 * variance)));

        double result = firstPart * secondPart;
        return result;
    }

    public static final double z(double confidence) {
        if (confidence > 1) {
            throw new IllegalArgumentException("Confidence must be <= 1");
        }
        if (confidence < 0) {
            throw new IllegalArgumentException("Confidence must be >= 0");
        }

        return pnormaldist(1 - (1 - confidence) / 2);
    }

    private static double pnormaldist(double qn) {

        double[] b = {1.570796288, 0.03706987906, -0.8364353589e-3,
            -0.2250947176e-3, 0.6841218299e-5, 0.5824238515e-5,
            -0.104527497e-5, 0.8360937017e-7, -0.3231081277e-8,
            0.3657763036e-10, 0.6936233982e-12};

        if (qn < 0.0 || 1.0 < qn) {
            return 0.0;
        }
        if (qn == 0.5) {
            return 0.0;
        }

        double w1 = qn;
        if (qn > 0.5) {
            w1 = 1.0 - w1;
        }

        double w3 = -Math.log(4.0 * w1 * (1.0 - w1));

        w1 = b[0];
        for (int i = 1; i <= 10; i++) {
            w1 += b[i] * Math.pow(w3, i);

        }

        if (qn > 0.5) {
            return Math.sqrt(w1 * w3);
        } else {
            return -Math.sqrt(w1 * w3);
        }
    }
}
