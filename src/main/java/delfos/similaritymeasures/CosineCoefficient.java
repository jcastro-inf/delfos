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

import delfos.common.exceptions.CouldNotComputeSimilarity;
import java.util.List;

/**
 * Clase que implementa la medida del coseno para realizar una medida de
 * similitud de dos vectores
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class CosineCoefficient extends WeightedSimilarityMeasureAdapter {

    private static final long serialVersionUID = 1L;

    @Override
    public double weightedSimilarity(List<Double> v1, List<Double> v2, List<Double> weights) throws CouldNotComputeSimilarity {
        if (v1.size() != v2.size() || v1.size() != weights.size()) {
            throw new IllegalArgumentException("The vector lengths are different");
        }

        double numerator = 0;
        double denominator1 = 0, denominator2 = 0;
        double sumPesos = 0;
        for (int i = 0; i < v1.size(); i++) {
            double r1 = v1.get(i);
            double r2 = v2.get(i);
            double w = weights.get(i);

            numerator = numerator + r1 * r2 * w;
            denominator1 = denominator1 + r1 * r1 * w;
            denominator2 = denominator2 + r2 * r2 * w;

            sumPesos += w;
        }

        if (sumPesos == 0) {
            throw new CouldNotComputeSimilarity("Sum of weights is zero");
        }
        if (sumPesos > 1.01) {
            throw new CouldNotComputeSimilarity("Sum of weights is greater than 1: '" + sumPesos + "'.");
        }

        if (denominator1 == 0 || denominator2 == 0) {
            return 0;
        } else {
            double coseno = (double) (numerator / (Math.sqrt(denominator1) * Math.sqrt(denominator2)));
            return coseno;
        }
    }
}
