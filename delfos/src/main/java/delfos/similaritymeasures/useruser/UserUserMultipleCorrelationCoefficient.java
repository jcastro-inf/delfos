package delfos.similaritymeasures.useruser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.common.Global;
import delfos.common.datastructures.histograms.HistogramNumbersSmart;
import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.loaders.given.DatasetLoaderGiven;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.similaritymeasures.BasicSimilarityMeasure;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import delfos.similaritymeasures.SimilarityMeasure;
import delfos.similaritymeasures.SimilarityMeasureAdapter;

/**
 *
 *
 * @version 08-may-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class UserUserMultipleCorrelationCoefficient extends SimilarityMeasureAdapter implements UserUserSimilarity {

    public static final Parameter WRAPPED_SIMILARITY = new Parameter(
            "WrappedSimilarity",
            new ParameterOwnerRestriction(
                    SimilarityMeasure.class,
                    new PearsonCorrelationCoefficient()));

    private BasicSimilarityMeasure basicSimilarityMeasure;
    private UserUserSimilarityWrapper wrappedSimilarity;

    public UserUserMultipleCorrelationCoefficient() {
        super();
        addParameter(WRAPPED_SIMILARITY);

        addParammeterListener(() -> {
            basicSimilarityMeasure = (BasicSimilarityMeasure) getParameterValue(WRAPPED_SIMILARITY);
            wrappedSimilarity = new UserUserSimilarityWrapper(basicSimilarityMeasure);
        });
    }

    public UserUserMultipleCorrelationCoefficient(BasicSimilarityMeasure basicSimilarityMeasure) {
        this();
        setParameterValue(WRAPPED_SIMILARITY, basicSimilarityMeasure);
    }

    @Override
    public double similarity(DatasetLoader<? extends Rating> datasetLoader, int idUser1, int idUser2) throws UserNotFound, CouldNotComputeSimilarity {

        Map<Integer, Double> values = new TreeMap<>();
        MeanIterative mean = new MeanIterative();
        double rangeWidth = 0.05;
        HistogramNumbersSmart histogram = new HistogramNumbersSmart(rangeWidth);

        for (int idNeighbor : datasetLoader.getRatingsDataset().allUsers()) {
            if (idNeighbor == idUser1 || idNeighbor == idUser2) {
                continue;
            }
            double r;
            try {
                r = computeRValue(datasetLoader, idUser1, idUser2, idNeighbor);

                values.put(idNeighbor, r);
                mean.addValue(r);
                if (Global.isVerboseAnnoying()) {
                    histogram.addValue(r);
                }
            } catch (CouldNotComputeSimilarity ex) {

            }
        }
        if (mean.isEmpty()) {
            throw new CouldNotComputeSimilarity("No r similarities computed.");
        }

        if (Global.isVerboseAnnoying()) {
            histogram.printHistogram(System.out);
        }
        return mean.getMean();
    }

    double min = -1;
    double max = 1;
    double delta = 0.00001;

    public double toRange(double value) {
        if (value - delta > max || value + delta < min) {
            throw new IllegalArgumentException("The value '" + value + "' is out of range [" + min + "," + max + "]");
        }

        if (value > max) {
            return max;
        }

        if (value < min) {
            return min;
        }
        return value;
    }

    protected double computeRValue(DatasetLoader<? extends Rating> datasetLoader, int a, int b, int c_i) throws UserNotFound, CouldNotComputeSimilarity {

        //Para que esta medida funcione correctamente, tengo que rellenar los vectores de valoraciones.
        Set<Integer> itemsRated = new TreeSet<>();
        itemsRated.addAll(datasetLoader.getRatingsDataset().getUserRated(a));
        itemsRated.addAll(datasetLoader.getRatingsDataset().getUserRated(b));
        itemsRated.addAll(datasetLoader.getRatingsDataset().getUserRated(c_i));

        Collection<Rating> completedRatings = new ArrayList<>(itemsRated.size() * 3);

        Collection<Integer> users = new ArrayList<>();
        users.add(a);
        users.add(b);
        users.add(c_i);

        for (int idUser : users) {

            Map<Integer, ? extends Rating> userRatings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser);
            double meanUserRating = datasetLoader.getRatingsDataset().getMeanRatingUser(idUser);

            itemsRated.stream().forEach((idItem) -> {
                if (userRatings.containsKey(idItem)) {
                    completedRatings.add(new Rating(idUser, idItem, userRatings.get(idItem).ratingValue));
                } else {
                    completedRatings.add(new Rating(idUser, idItem, meanUserRating));
                }
            });

        }

        RatingsDataset<Rating> ratingsDataset_completed = new BothIndexRatingsDataset<>(completedRatings);

        DatasetLoader<Rating> datasetLoader_completed = new DatasetLoaderGiven<>(datasetLoader, ratingsDataset_completed);

        double simAB = toRange(wrappedSimilarity.similarity(datasetLoader_completed, a, b));

        double simAC_I = toRange(wrappedSimilarity.similarity(datasetLoader_completed, a, c_i));
        double simBC_I = toRange(wrappedSimilarity.similarity(datasetLoader_completed, b, c_i));

        if (simBC_I == 1) {
            throw new CouldNotComputeSimilarity("sim(" + b + "," + c_i + ") = 1: Division by zero");
        }

        return computeRValueFromSimilarities(simAB, simAC_I, simBC_I);
    }

    protected double simAB(DatasetLoader<? extends Rating> datasetLoader, int a, int b, int c_i) throws UserNotFound, CouldNotComputeSimilarity {

        //Para que esta medida funcione correctamente, tengo que rellenar los vectores de valoraciones.
        Set<Integer> itemsRated = new TreeSet<>();
        itemsRated.addAll(datasetLoader.getRatingsDataset().getUserRated(a));
        itemsRated.addAll(datasetLoader.getRatingsDataset().getUserRated(b));
        itemsRated.addAll(datasetLoader.getRatingsDataset().getUserRated(c_i));

        Collection<Rating> completedRatings = new ArrayList<>(itemsRated.size() * 3);

        Collection<Integer> users = new ArrayList<>();
        users.add(a);
        users.add(b);
        users.add(c_i);

        for (int idUser : users) {

            Map<Integer, ? extends Rating> userRatings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser);
            double meanUserRating = datasetLoader.getRatingsDataset().getMeanRatingUser(idUser);

            itemsRated.stream().forEach((idItem) -> {
                if (userRatings.containsKey(idItem)) {
                    completedRatings.add(new Rating(idUser, idItem, userRatings.get(idItem).ratingValue));
                } else {
                    completedRatings.add(new Rating(idUser, idItem, meanUserRating));
                }
            });

        }

        RatingsDataset<Rating> ratingsDataset_completed = new BothIndexRatingsDataset<>(completedRatings);

        DatasetLoader<Rating> datasetLoader_completed = new DatasetLoaderGiven<>(datasetLoader, ratingsDataset_completed);

        double simAB = toRange(wrappedSimilarity.similarity(datasetLoader_completed, a, b));
        return simAB;
    }

    protected double simAC_i(DatasetLoader<? extends Rating> datasetLoader, int a, int b, int c_i) throws UserNotFound, CouldNotComputeSimilarity {

        //Para que esta medida funcione correctamente, tengo que rellenar los vectores de valoraciones.
        Set<Integer> itemsRated = new TreeSet<>();
        itemsRated.addAll(datasetLoader.getRatingsDataset().getUserRated(a));
        itemsRated.addAll(datasetLoader.getRatingsDataset().getUserRated(b));
        itemsRated.addAll(datasetLoader.getRatingsDataset().getUserRated(c_i));

        Collection<Rating> completedRatings = new ArrayList<>(itemsRated.size() * 3);

        Collection<Integer> users = new ArrayList<>();
        users.add(a);
        users.add(b);
        users.add(c_i);

        for (int idUser : users) {

            Map<Integer, ? extends Rating> userRatings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser);
            double meanUserRating = datasetLoader.getRatingsDataset().getMeanRatingUser(idUser);

            itemsRated.stream().forEach((idItem) -> {
                if (userRatings.containsKey(idItem)) {
                    completedRatings.add(new Rating(idUser, idItem, userRatings.get(idItem).ratingValue));
                } else {
                    completedRatings.add(new Rating(idUser, idItem, meanUserRating));
                }
            });

        }

        RatingsDataset<Rating> ratingsDataset_completed = new BothIndexRatingsDataset<>(completedRatings);

        DatasetLoader<Rating> datasetLoader_completed = new DatasetLoaderGiven<>(datasetLoader, ratingsDataset_completed);

        double simAC_I = toRange(wrappedSimilarity.similarity(datasetLoader_completed, a, c_i));
        return simAC_I;
    }

    protected double simBC_i(DatasetLoader<? extends Rating> datasetLoader, int a, int b, int c_i) throws UserNotFound, CouldNotComputeSimilarity {

        //Para que esta medida funcione correctamente, tengo que rellenar los vectores de valoraciones.
        Set<Integer> itemsRated = new TreeSet<>();
        itemsRated.addAll(datasetLoader.getRatingsDataset().getUserRated(a));
        itemsRated.addAll(datasetLoader.getRatingsDataset().getUserRated(b));
        itemsRated.addAll(datasetLoader.getRatingsDataset().getUserRated(c_i));

        Collection<Rating> completedRatings = new ArrayList<>(itemsRated.size() * 3);

        Collection<Integer> users = new ArrayList<>();
        users.add(a);
        users.add(b);
        users.add(c_i);

        for (int idUser : users) {

            Map<Integer, ? extends Rating> userRatings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser);
            double meanUserRating = datasetLoader.getRatingsDataset().getMeanRatingUser(idUser);

            itemsRated.stream().forEach((idItem) -> {
                if (userRatings.containsKey(idItem)) {
                    completedRatings.add(new Rating(idUser, idItem, userRatings.get(idItem).ratingValue));
                } else {
                    completedRatings.add(new Rating(idUser, idItem, meanUserRating));
                }
            });

        }

        RatingsDataset<Rating> ratingsDataset_completed = new BothIndexRatingsDataset<>(completedRatings);

        DatasetLoader<Rating> datasetLoader_completed = new DatasetLoaderGiven<>(datasetLoader, ratingsDataset_completed);

        double simBC_I = toRange(wrappedSimilarity.similarity(datasetLoader_completed, b, c_i));
        return simBC_I;
    }

    protected double computeRValueFromSimilarities(double simAB, double simAC_i, double simBC_i) throws CouldNotComputeSimilarity {

        double positivePart = (1 + 2 * simAB * simAC_i * simBC_i);
        double negativePart = (simAB * simAC_i * simBC_i + Math.pow(simAB, 2) + Math.pow(simBC_i, 2));

        double determinantOfR = positivePart - negativePart;
        double adjointOfR = 1 - simBC_i * simBC_i;

        if (adjointOfR == 0) {
            throw new CouldNotComputeSimilarity("sim( b , c_i ) = 1: Division by zero");
        }

        double pSquare = 1 - determinantOfR / adjointOfR;
        if (Double.isNaN(pSquare)) {
            throw new IllegalStateException("Cannot be NaN");
        }
        if (pSquare < 0) {
            throw new CouldNotComputeSimilarity("Determinant/cofactor is negative, cannot compute similarity.");
        }
        double p = Math.sqrt(pSquare);

        if (Double.isNaN(p)) {
            throw new IllegalStateException("Cannot be NaN");
        }

        return p;
    }
}
