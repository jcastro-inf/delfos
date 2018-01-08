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

import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.rs.collaborativefiltering.knn.CommonRating;
import delfos.similaritymeasures.useruser.UserUserSimilarity;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * Medida de similitud que utiliza el coeficiente de correlación de pearson para
 * comparar la relación de variación de dos vectores. El valor devuelto está en
 * el intervalo [0,1] ya que una relación de variación inversa perfecta
 * (coeficiente de pearson = -1) indicaría que son completamente distintos
 * (similitud = 0)
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class PearsonCorrelationCoefficient extends WeightedSimilarityMeasureAdapter implements UserUserSimilarity {

    @Override
    public double similarity(List<Double> v1, List<Double> v2) {
        double pcc = pearsonCorrelationCoefficient(v1, v2);

        return (double) pcc;
    }

    @Override
    public double weightedSimilarity(List<Double> v1, List<Double> v2, List<Double> weights) {
        double pcc = pearsonCorrelationCoefficient_weighted(v1, v2, weights);

        return (double) pcc;
    }

    /**
     * Devuelve el PCC de las listas de valores. El valor está entre -1 y 1. Si
     * las listas están vacías, lanza una excepción
     *
     * @param v1
     * @param v2
     * @return
     */
    public double pearsonCorrelationCoefficient(List<? extends Number> v1, List<? extends Number> v2) {

        validateInputParameters(v1, v2);

        double avg1 = 0;
        double avg2 = 0;

        double similarity;

        if (v1.isEmpty()) {
            similarity = Double.NaN;
        } else {
            avg1 = v1.stream().mapToDouble(number -> number.doubleValue()).average().getAsDouble();
            avg2 = v2.stream().mapToDouble(number -> number.doubleValue()).average().getAsDouble();

            double numerador = 0;
            double denominador1 = 0;
            double denominador2 = 0;
            ListIterator<? extends Number> i1 = v1.listIterator();
            ListIterator<? extends Number> i2 = v2.listIterator();
            do {
                double value1 = i1.next().doubleValue();
                double value2 = i2.next().doubleValue();

                numerador += (value1 - avg1) * (value2 - avg2);
                denominador1 += (value1 - avg1) * (value1 - avg1);
                denominador2 += (value2 - avg2) * (value2 - avg2);
            } while (i1.hasNext());

            //Cálculo del denominador
            double denominador = Math.sqrt(denominador1 * denominador2);

            if (denominador == 0) {
                return Double.NaN;
            } else {
                similarity = numerador / denominador;
            }
        }
        return similarity;
    }

    public double weightedMean(List<? extends Number> v, List<? extends Number> weights) {
        double numerator = 0;
        double denominator = 0;

        for (int i = 0; i < v.size(); i++) {
            numerator += v.get(i).doubleValue() * weights.get(i).doubleValue();
            denominator += weights.get(i).doubleValue();
        }

        return numerator / denominator;
    }

    public double weightedCovariance(List<? extends Number> v1, List<? extends Number> v2, List<? extends Number> weights) {

        if (v1.isEmpty()) {
            return Double.NaN;
        }
        double weightedMean_v1 = weightedMean(v1, weights);
        double weightedMean_v2 = weightedMean(v2, weights);

        double numerator = 0;
        double denominator = 0;

        ListIterator<? extends Number> i1 = v1.listIterator(), i2 = v2.listIterator(), iw = weights.listIterator();

        do {
            double value1 = i1.next().doubleValue();
            double value2 = i2.next().doubleValue();
            double weight = iw.next().doubleValue();

            numerator += weight * (value1 - weightedMean_v1) * (value2 - weightedMean_v2);
            denominator += weight;
        } while (i1.hasNext());

        return numerator / denominator;
    }

    public double pearsonCorrelationCoefficient_weighted(List<? extends Number> v1, List<? extends Number> v2, List<? extends Number> weights) throws IllegalArgumentException {
        validateInputParameters(v1, v2, weights);

        if (v1.isEmpty()) {
            return Double.NaN;
        }

        double numerator = weightedCovariance(v1, v2, weights);

        double denominator1 = weightedCovariance(v1, v1, weights);
        double denominator2 = weightedCovariance(v2, v2, weights);

        double denominator = Math.sqrt(denominator1 * denominator2);

        if (denominator == 0) {
            return Double.NaN;
        }
        double pcc_weighted = numerator / denominator;

        return pcc_weighted;
    }

    private void validateInputParameters(List<? extends Number> v1, List<? extends Number> v2) throws IllegalArgumentException {
        if (v1 == null) {
            throw new IllegalArgumentException("The list v1 cannot be null.");
        }
        if (v2 == null) {
            throw new IllegalArgumentException("The list v2 cannot be null.");
        }
        if (v1.size() != v2.size()) {
            throw new IllegalArgumentException("The lists have different size: " + v1.size() + " != " + v2.size());
        }
    }

    private void validateInputParameters(List<? extends Number> v1, List<? extends Number> v2, List<? extends Number> weights) throws IllegalArgumentException {
        if (v1 == null) {
            throw new IllegalArgumentException("The list v1 cannot be null.");
        }
        if (v2 == null) {
            throw new IllegalArgumentException("The list v2 cannot be null.");
        }
        if (weights == null) {
            throw new IllegalArgumentException("The weights list cannot be null.");
        }
        if (v1.size() != v2.size()
                || v1.size() != weights.size()
                || v2.size() != weights.size()) {
            throw new IllegalArgumentException("The lists have different size: " + v1.size() + " != " + v2.size() + " != " + weights.size());
        }
    }

    @Override
    public double similarity(DatasetLoader<? extends Rating> datasetLoader, long idUser1, long idUser2) {
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

    public double similarity(DatasetLoader<? extends Rating> datasetLoader, Item item1, Item item2) {
        List<CommonRating> intersection = CommonRating.intersection(datasetLoader, item1, item2).stream().collect(Collectors.toList());

        List<Double> l1 = intersection.stream().map(commonRating -> commonRating.getRating1()).collect(Collectors.toList());
        List<Double> l2 = intersection.stream().map(commonRating -> commonRating.getRating2()).collect(Collectors.toList());

        return similarity(l1, l2);
    }
}
