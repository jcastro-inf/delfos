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
package delfos.rs.collaborativefiltering.als;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.experiment.SeedHolder;
import delfos.rs.collaborativefiltering.CollaborativeRecommender;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVDModel;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 *
 * @author jcastro
 */
public class ALSRecommender extends CollaborativeRecommender<TryThisAtHomeSVDModel>
        implements SeedHolder {

    public ALSRecommender() {
        super();
        addParameter(SeedHolder.SEED);
    }

    @Override
    public TryThisAtHomeSVDModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset {

        int numIter = 20;
        int dimension = 20;
        long seed = getSeedValue();

        Map<User, List<Double>> randomUserVectors = datasetLoader.getUsersDataset().parallelStream().collect(Collectors.toMap(user -> user, user -> {
            Random random = new Random(seed + user.getId());
            List<Double> vector = random.doubles(-10, 10).limit(dimension).boxed().collect(Collectors.toList());
            return vector;
        }));

        Map<Item, List<Double>> randomItemVectors = datasetLoader.getContentDataset().parallelStream().collect(Collectors.toMap(item -> item, item -> {
            Random random = new Random(seed + item.getId());
            List<Double> vector = random.doubles(-10, 10).limit(dimension).boxed().collect(Collectors.toList());
            return vector;
        }));

        TryThisAtHomeSVDModel model = new TryThisAtHomeSVDModel(randomUserVectors, randomItemVectors);

        for (int i = 0; i < numIter; i++) {

            final TryThisAtHomeSVDModel initialModel = model;

            double error = getModelError(datasetLoader, initialModel);

            System.out.println("Error in iteration " + i + " is " + error);

            Map<User, List<Double>> trainedUserVectors = datasetLoader.getUsersDataset().parallelStream().collect(Collectors.toMap(user -> user,
                    user -> {
                        Map<Integer, ? extends Rating> userRatings = datasetLoader.getRatingsDataset().getUserRatingsRated(user.getId());

                        List<Integer> itemsSorted = userRatings.keySet().stream().sorted().collect(Collectors.toList());

                        RealMatrix coefficients = new Array2DRowRealMatrix(itemsSorted.size(), dimension);
                        IntStream.range(0, itemsSorted.size()).parallel().boxed()
                        .forEach(index
                                -> {
                            double[] vector = initialModel.getItemFeatures(itemsSorted.get(index))
                                    .stream().mapToDouble(value -> value).toArray();
                            coefficients.setRow(index, vector);
                        });

                        DecompositionSolver solver = new QRDecomposition(coefficients).getSolver();

                        RealVector realVector = new ArrayRealVector(itemsSorted.size());
                        IntStream.range(0, itemsSorted.size()).parallel().boxed()
                        .forEach(index -> {

                            Integer idItem = itemsSorted.get(index);
                            double ratingValue = userRatings.get(idItem).getRatingValue().doubleValue();
                            realVector.setEntry(index, ratingValue);
                        });

                        RealVector solution = solver.solve(realVector);

                        List<Double> solutionList = IntStream.range(0, solution.getDimension()).boxed()
                        .map(index -> solution.getEntry(index))
                        .collect(Collectors.toList());
                        return solutionList;
                    }));

            Map<Item, List<Double>> trainedItemVectors = datasetLoader.getContentDataset().parallelStream().collect(Collectors.toMap(item -> item,
                    item -> {
                        Map<Integer, ? extends Rating> itemRatings = datasetLoader.getRatingsDataset().getItemRatingsRated(item.getId());

                        List<Integer> usersSorted = itemRatings.keySet().stream().sorted().collect(Collectors.toList());

                        RealMatrix coefficients = new Array2DRowRealMatrix(usersSorted.size(), dimension);
                        IntStream.range(0, usersSorted.size()).parallel().boxed()
                        .forEach(index
                                -> {
                            final Integer idUser = usersSorted.get(index);
                            double[] vector = initialModel.getUserFeatures(idUser)
                                    .stream().mapToDouble(value -> value).toArray();
                            coefficients.setRow(index, vector);
                        });

                        DecompositionSolver solver = new QRDecomposition(coefficients).getSolver();

                        RealVector realVector = new ArrayRealVector(usersSorted.size());
                        IntStream.range(0, usersSorted.size()).parallel().boxed()
                        .forEach(index -> {
                            Integer idUser = usersSorted.get(index);
                            double ratingValue = itemRatings.get(idUser).getRatingValue().doubleValue();
                            realVector.setEntry(index, ratingValue);
                        });

                        RealVector solution = solver.solve(realVector);

                        List<Double> solutionList = IntStream.range(0, solution.getDimension()).boxed()
                        .map(index -> solution.getEntry(index))
                        .collect(Collectors.toList());
                        return solutionList;
                    }));

            model = new TryThisAtHomeSVDModel(trainedUserVectors, trainedItemVectors);

        }
        return model;

    }

    @Override
    public void setSeedValue(long seedValue) {
        setParameterValue(SEED, seedValue);
    }

    @Override
    public long getSeedValue() {
        return (Long) getParameterValue(SEED);
    }

    private double getModelError(DatasetLoader<? extends Rating> datasetLoader, TryThisAtHomeSVDModel initialModel) {
        return datasetLoader.getUsersDataset().parallelStream().flatMap(user -> {
            return datasetLoader.getRatingsDataset().getUserRatingsRated(user.getId())
                    .values().parallelStream().map(rating -> {
                        int idItem = rating.getIdItem();
                        int idUser = rating.getIdUser();
                        double ratingValue = rating.getRatingValue().doubleValue();
                        double predictedValue = initialModel.predict(idUser, idItem);
                        return Math.abs(ratingValue - predictedValue);
                    });
        }).mapToDouble(value -> value).average().orElse(Double.NaN);

    }

}
