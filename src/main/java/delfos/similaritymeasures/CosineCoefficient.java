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

import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.rs.collaborativefiltering.knn.CommonRating;
import delfos.similaritymeasures.useruser.UserUserSimilarity;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Clase que implementa la medida del coseno para realizar una medida de similitud de dos vectores
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class CosineCoefficient extends WeightedSimilarityMeasureAdapter implements UserUserSimilarity {

    private static final long serialVersionUID = 1L;

    @Override
    public double weightedSimilarity(List<Double> v1, List<Double> v2, List<Double> weights) {
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
            return Double.NaN;
        }

        if (sumPesos > 1 + 1e08) {
            throw new IllegalArgumentException("Sum of weights is greater than one: " + sumPesos);
        }

        if (denominator1 == 0 || denominator2 == 0) {
            return 0;
        } else {
            double coseno = (double) (numerator / (Math.sqrt(denominator1) * Math.sqrt(denominator2)));
            return coseno;
        }
    }

    public static double cosineCoefficient(List<Double> v1, List<Double> v2) {
        if (v1.size() != v2.size()) {
            throw new IllegalArgumentException("The vector lengths are different");
        }

        double numerator = 0;
        double denominator1 = 0, denominator2 = 0;

        for (int i = 0; i < v1.size(); i++) {
            double r1 = v1.get(i);
            double r2 = v2.get(i);

            numerator = numerator + r1 * r2;
            denominator1 = denominator1 + r1 * r1;
            denominator2 = denominator2 + r2 * r2;

        }

        if (denominator1 == 0 || denominator2 == 0) {
            return 0;
        } else {
            double coseno = (double) (numerator / (Math.sqrt(denominator1) * Math.sqrt(denominator2)));
            return coseno;
        }
    }

    public static double cosineCoefficient(double[] v1, double[] v2) {
        if (v1.length != v2.length) {
            throw new IllegalArgumentException("The vector lengths are different");
        }

        double numerator = 0;
        double denominator1 = 0, denominator2 = 0;

        for (int i = 0; i < v1.length; i++) {
            double r1 = v1[i];
            double r2 = v2[i];

            numerator = numerator + r1 * r2;
            denominator1 = denominator1 + r1 * r1;
            denominator2 = denominator2 + r2 * r2;

        }

        if (denominator1 == 0 || denominator2 == 0) {
            return 0;
        } else {
            double coseno = (double) (numerator / (Math.sqrt(denominator1) * Math.sqrt(denominator2)));
            return coseno;
        }
    }

    @Override
    public double similarity(DatasetLoader<? extends Rating> datasetLoader, int idUser1, int idUser2) {
        User user1 = datasetLoader.getUsersDataset().get(idUser1);
        User user2 = datasetLoader.getUsersDataset().get(idUser2);

        return similarity(datasetLoader, user1, user2);
    }

    @Override
    public double similarity(DatasetLoader<? extends Rating> datasetLoader, User user1, User user2) {

        List<CommonRating> intersection = CommonRating.intersection(datasetLoader, user1, user2).stream().collect(Collectors.toList());

        List<Double> l1 = intersection.stream().map(commonRating -> commonRating.getRating1()).collect(Collectors.toList());
        List<Double> l2 = intersection.stream().map(commonRating -> commonRating.getRating2()).collect(Collectors.toList());

        return similarity(l1, l2);

    }
}
