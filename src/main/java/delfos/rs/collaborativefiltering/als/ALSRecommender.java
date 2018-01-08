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
import delfos.rs.collaborativefiltering.factorization.MatrixFactorizationModel;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationsToUser;
import delfos.utils.algorithm.progress.ProgressChangedController;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.math4.optim.InitialGuess;
import org.apache.commons.math4.optim.MaxEval;
import org.apache.commons.math4.optim.MaxIter;
import org.apache.commons.math4.optim.PointValuePair;
import org.apache.commons.math4.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math4.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math4.optim.nonlinear.scalar.noderiv.MultiDirectionalSimplex;
import org.apache.commons.math4.optim.nonlinear.scalar.noderiv.SimplexOptimizer;

/**
 *
 * @author jcastro
 */
public class ALSRecommender extends CollaborativeRecommender<MatrixFactorizationModel>
        implements SeedHolder {

    public ALSRecommender() {
        super();
        addParameter(SeedHolder.SEED);
    }

    @Override
    public MatrixFactorizationModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset {

        int numIter = 1;
        int dimension = 5;
        long seed = getSeedValue();

        final double lambda = 0.1;

        Bias bias = new Bias(datasetLoader);

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

        MatrixFactorizationModel model = new MatrixFactorizationModel(randomUserVectors, randomItemVectors, bias);

        for (int iterationIndex = 0; iterationIndex < numIter; iterationIndex++) {

            final int iteration = iterationIndex;
            final MatrixFactorizationModel initialModel = model;

            double error = getModelError(bias, datasetLoader, initialModel);

            System.out.println("Error in iteration " + iterationIndex + " is " + error);

            ProgressChangedController userProgress = new ProgressChangedController(
                    getAlias() + " for dataset " + datasetLoader.getAlias() + " userOptimization iteration " + iteration,
                    datasetLoader.getUsersDataset().size(),
                    this::fireBuildingProgressChangedEvent
            );

            Map<User, List<Double>> trainedUserVectors = datasetLoader.getUsersDataset().parallelStream().collect(Collectors.toMap(user -> user,
                    (User user) -> {
                        Map<Long, ? extends Rating> userRatings = datasetLoader.getRatingsDataset().getUserRatingsRated(user.getId());

                        ObjectiveFunction objectiveFunction = new ObjectiveFunction((double[] pu) -> {
                            List<Double> userVector = Arrays.stream(pu).boxed().collect(Collectors.toList());
                            double predictionError = userRatings.values().parallelStream()
                                    .map(bias.getBiasApplier())
                                    .map(rating -> {
                                        List<Double> itemVector = initialModel.getItemFeatures(rating.getItem());
                                        double prediction = IntStream.range(0, userVector.size())
                                                .mapToDouble(index -> userVector.get(index) * itemVector.get(index))
                                                .sum();

                                        double value = rating.getRatingValue().doubleValue();

                                        double errorThisRating = prediction - value;

                                        return errorThisRating;
                                    })
                                    .map(value -> Math.pow(value, 2))
                                    .mapToDouble(value -> value)
                                    .sum();

                            double penalty = Arrays.stream(pu)
                                    .map(value -> Math.pow(value, 2))
                                    .sum();
                            double objectiveFunctionValue = predictionError + lambda * penalty;
                            return objectiveFunctionValue;
                        });

                        SimplexOptimizer simplexOptimizer = new SimplexOptimizer(0, 0);

                        double[] initialGuess = new Random(seed + user.getId()).doubles(-10, 10).limit(dimension).toArray();

                        List<Double> initialGuessList = Arrays.stream(initialGuess).boxed().collect(Collectors.toList());

                        double initialGuessPenalty = objectiveFunction.getObjectiveFunction().value(initialGuess);

                        try {
                            PointValuePair optimize = simplexOptimizer.optimize(new MultiDirectionalSimplex(dimension),
                                    new InitialGuess(initialGuess),
                                    objectiveFunction,
                                    GoalType.MINIMIZE,
                                    MAX_EVAL,
                                    MAX_ITER
                            );
                            double optimizedPenalty = optimize.getValue();
                            userProgress.setTaskFinished();

                            List<Double> optimizedUserVector = Arrays.stream(optimize.getPoint()).boxed().collect(Collectors.toList());
                            return optimizedUserVector;
                        } catch (Exception ex) {
                            System.out.println("Vector cannot be optimized for user " + user + " (numRatings=" + userRatings.size() + ")");
                            return initialModel.getUserFeatures(user);
                        }
                    }));

            ProgressChangedController itemProgress = new ProgressChangedController(
                    getAlias() + " for dataset " + datasetLoader.getAlias() + " item optimization iteration " + iteration,
                    datasetLoader.getContentDataset().size(),
                    this::fireBuildingProgressChangedEvent
            );

            Collection<Item> itemsWithRatings = datasetLoader.getContentDataset().parallelStream()
                    .filter(item -> {
                        try {
                            datasetLoader
                                    .getRatingsDataset().getItemRated(item.getId());
                            return true;
                        }catch(Exception e){
                            return false;
                        }
                    })
                    .collect(Collectors.toList());

            Map<Item, List<Double>> trainedItemVectors =
                    itemsWithRatings.parallelStream().collect(Collectors.toMap(item -> item,
                    item -> {
                        Map<Long, ? extends Rating> itemRatings = datasetLoader.getRatingsDataset().getItemRatingsRated(item.getId());

                        ObjectiveFunction objectiveFunction = new ObjectiveFunction((double[] pu) -> {
                            List<Double> itemVector = Arrays.stream(pu).boxed().collect(Collectors.toList());
                            double predictionError = itemRatings.values().parallelStream()
                                    .map(bias.getBiasApplier())
                                    .map(rating -> {
                                        List<Double> userVector = initialModel.getUserFeatures(rating.getUser());
                                        double prediction = IntStream.range(0, userVector.size())
                                                .mapToDouble(index -> userVector.get(index) * itemVector.get(index))
                                                .sum();

                                        double value = rating.getRatingValue().doubleValue();

                                        double errorThisRating = prediction - value;

                                        return errorThisRating;
                                    })
                                    .map(value -> Math.pow(value, 2))
                                    .mapToDouble(value -> value)
                                    .sum();

                            double penalty = Arrays.stream(pu)
                                    .map(value -> Math.pow(value, 2))
                                    .sum();
                            double objectiveFunctionValue = predictionError + lambda * penalty;
                            return objectiveFunctionValue;
                        });

                        SimplexOptimizer simplexOptimizer = new SimplexOptimizer(0, 0);

                        double[] initialGuess = new Random(seed + item.getId()).doubles(-10, 10).limit(dimension).toArray();

                        List<Double> initialGuessList = Arrays.stream(initialGuess).boxed().collect(Collectors.toList());

                        double initialGuessPenalty = objectiveFunction.getObjectiveFunction().value(initialGuess);

                        try {
                            PointValuePair optimize = simplexOptimizer.optimize(new MultiDirectionalSimplex(dimension),
                                    new InitialGuess(initialGuess),
                                    objectiveFunction,
                                    GoalType.MINIMIZE,
                                    MAX_EVAL,
                                    MAX_ITER);
                            double optimizedPenalty = optimize.getValue();
                            itemProgress.setTaskFinished();

                            List<Double> optimizedVector = Arrays.stream(optimize.getPoint()).boxed().collect(Collectors.toList());

                            return optimizedVector;
                        } catch (Exception ex) {
                            System.out.println("Vector cannot be optimized " + item + " cannot be optimized (numRatings=" + itemRatings.size() + ")");
                            return initialModel.getItemFeatures(item);
                        }
                    }));

            model = new MatrixFactorizationModel(trainedUserVectors, trainedItemVectors, bias);

        }
        return model;

    }
    private final MaxIter MAX_ITER = new MaxIter(1000000);
    private final MaxEval MAX_EVAL = new MaxEval(1000000);

    @Override
    public void setSeedValue(long seedValue) {
        setParameterValue(SEED, seedValue);
    }

    @Override
    public long getSeedValue() {
        return (Long) getParameterValue(SEED);
    }

    private double getModelError(Bias bias, DatasetLoader<? extends Rating> datasetLoader, MatrixFactorizationModel initialModel) {
        return datasetLoader.getUsersDataset().parallelStream().flatMap(user -> {
            return datasetLoader.getRatingsDataset().getUserRatingsRated(user.getId())
                    .values()
                    .parallelStream()
                    .map(bias.getBiasApplier())
                    .map(rating -> {
                        Item item = rating.getItem();
                        double ratingValue = rating.getRatingValue().doubleValue();
                        double predictedValue = initialModel.predict(user, item);
                        return Math.abs(ratingValue - predictedValue);
                    });
        }).mapToDouble(value -> value).average().orElse(Double.NaN);

    }

    @Override
    public RecommendationsToUser recommendToUser(DatasetLoader<? extends Rating> dataset, MatrixFactorizationModel recommendationModel, User user, Set<Item> candidateItems) {

        List<Recommendation> recommendations = candidateItems.parallelStream().map(item -> {
            double predictRating = recommendationModel.predictRating(user, item);
            return new Recommendation(item, predictRating);
        }).collect(Collectors.toList());

        return new RecommendationsToUser(user, recommendations);
    }

}
