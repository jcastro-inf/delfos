package delfos.rs.collaborativefiltering.svd.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.ERROR_CODES;
import delfos.common.Chronometer;
import delfos.common.DateCollapse;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtItemInformation;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parallelwork.notblocking.MultiThreadExecutionManager_NotBlocking;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.BooleanParameter;
import delfos.common.parameters.restriction.FloatParameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.experiment.SeedHolder;
import delfos.rs.collaborativefiltering.CollaborativeRecommender;
import delfos.rs.recommendation.Recommendation;

/**
 * Sistema de recomendación descrito en
 * http://sifter.org/~simon/journal/20061211.html que utiliza la descomposición
 * en valores singulares de las valoraciones.
 *
 * Calcula los valores singulares mediante el descenso de gradiente (aprendizaje
 * reduciendo el error en múltiples iteraciones.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * @version 24-julio-2014
 */
public class ParallelSVD
        extends CollaborativeRecommender<ParallelSVDModel>
        implements SeedHolder {

    private static final long serialVersionUID = 1L;
    /**
     * Parámetro para mejorar la inicializadion de los valores. True para usar
     * la inicialización inteligente (a veces falla).
     */
    public static final Parameter SMART_INITIALISATION = new Parameter("smartInit", new BooleanParameter(Boolean.FALSE));
    /**
     * Parámetro que indica el número de características que el sistema de
     * recomendación utiliza para la construcción del modelo, es decir, el
     * número de valores singulares que se calculan.
     */
    public static final Parameter NUM_FEATURES = new Parameter("features", new IntegerParameter(1, 9000, 10));
    /**
     * Como la descomposición en valores si
     */
    public static final Parameter NUM_ITER_PER_FEATURE = new Parameter("iterPerFeature", new IntegerParameter(1, 9000000, 10));
    /**
     * Parámetro que controla el learning rate, es decir, la velocidad con la
     * que se modifican los valores para minimizar el error de predicción.
     */
    public static final Parameter LEARNING_RATE = new Parameter("lRate", new FloatParameter(0.001f, 500f, 0.01f));

    /**
     * Parámetro para penalizar valores grandes de las características.
     */
    public static final Parameter K = new Parameter("K", new FloatParameter(0.0001f, 1f, 0.02f), "Parámetro para penalizar valores grandes de las características.");

    /**
     * Cojnstructor por defecto, que añade los parámetros del sistema de
     * recomendación.
     */
    public ParallelSVD() {
        super();
        addParameter(K);
        addParameter(SEED);
        addParameter(NUM_FEATURES);
        addParameter(LEARNING_RATE);
        addParameter(NUM_ITER_PER_FEATURE);
        addParameter(SMART_INITIALISATION);
    }

    /**
     * Constructor que asigna los valores indicados como número de
     * características y número de iteraciones del sistema.
     *
     * @param featuresValue Número de características que se calculan
     * @param iterationsValue Número de iteraciones para cada característica
     */
    public ParallelSVD(int featuresValue, int iterationsValue) {
        this();
        setParameterValue(ParallelSVD.NUM_FEATURES, featuresValue);
        setParameterValue(NUM_ITER_PER_FEATURE, iterationsValue);
    }

    protected static final double getInitialisation(double maxInitialisation, double minInitialisation, long seed) {
        Random random = new Random(seed);

        double ret = 0;
        while (ret == 0) {

            ret = random.nextFloat();
            ret = (ret * (maxInitialisation - minInitialisation)) + minInitialisation;
        }

        return ret;
    }

    @Override
    public ParallelSVDModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadRatingsDataset, CannotLoadContentDataset {
        final RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

        ParallelSVD_AlgorithmParameters parameters = extractAlgorithmParameters(ratingsDataset);

        Map<Integer, ArrayList<Double>> usersFeatures = new TreeMap<>();
        Map<Integer, ArrayList<Double>> itemsFeatures = new TreeMap<>();

        initFeatureMatrices(
                ratingsDataset,
                usersFeatures,
                itemsFeatures,
                parameters,
                getSeedValue()
        );

        ParallelSVDModel parallelSVDModel = new ParallelSVDModel(usersFeatures, itemsFeatures);

        final int numSplits = 16;
        ArrayList<Set<Integer>> usersSplit = splitSet(ratingsDataset.allUsers(), numSplits);
        ArrayList<Set<Integer>> itemsSplit = splitSet(ratingsDataset.allRatedItems(), numSplits);

        {

            double maeAnterior = 0;

            Chronometer chronometerBetweenIterations = new Chronometer();
            MeanIterative meanTimeToIterate = new MeanIterative();

            Global.showInfoMessage("Feature\tIteration\tMAE\tdiffMae\titerTime\ttotalTime\tremainingTime\n");
            for (int feature = 0; feature < parameters.numFeatures; feature++) {
                for (int iteration = 0; iteration < parameters.numIterationsPerFeature; iteration++) {

                    trainFeatureParallel(numSplits, usersSplit, itemsSplit, ratingsDataset, parameters, parallelSVDModel, feature);

                    int percentCompleted = computeTaskCompletionPercent(parameters, feature, iteration);
                    long remainingTime = computeTaskRemainingTime(parameters, feature, iteration, meanTimeToIterate.getMean());

                    if (Global.isInfoPrinted()) {
                        double mae = computeMAEofModelInTraining(datasetLoader, parallelSVDModel, feature);
                        Global.showInfoMessage(""
                                + feature + "\t"
                                + iteration + "\t"
                                + String.format("%.8f", mae) + "\t"
                                + String.format("%.8f", maeAnterior - mae) + "\t"
                                + DateCollapse.collapse(chronometerBetweenIterations.getPartialElapsed()) + "\t"
                                + DateCollapse.collapse(chronometerBetweenIterations.getTotalElapsed()) + "\t"
                                + DateCollapse.collapse(remainingTime) + "\t"
                                + "\n");
                        maeAnterior = mae;
                    }

                    meanTimeToIterate.addValue(chronometerBetweenIterations.getPartialElapsed());

                    fireBuildingProgressChangedEvent("Training model Feature: " + feature + " iter: " + iteration, percentCompleted, remainingTime);
                    chronometerBetweenIterations.setPartialEllapsedCheckpoint();

                }
            }

        }

        ParallelSVDModel model = new ParallelSVDModel(usersFeatures, itemsFeatures);

        if (Global.isVerboseAnnoying()) {

            Global.showInfoMessage("=======================================\n");
            Global.showInfoMessage("User features:\n");
            ratingsDataset.allUsers().stream().forEach((idUser) -> {
                Global.showInfoMessage("User " + idUser + " \t" + model.getUserFeatures(idUser).toString() + "\n");
            });
            Global.showInfoMessage("---------------------------------------\n");
            Global.showInfoMessage("Item features:\n");

            ratingsDataset.allRatedItems().stream().forEach((idItem) -> {
                Global.showInfoMessage("Item " + idItem + " \t" + model.getItemFeatures(idItem).toString() + "\n");
            });
            Global.showInfoMessage("=======================================\n");
        }

        return model;
    }

    public int computeTaskCompletionPercent(ParallelSVD_AlgorithmParameters parameters, int feature, int iteration) {

        int numTotalIterations = (parameters.numFeatures * parameters.numIterationsPerFeature);
        int numIterationsDone = (feature * parameters.numIterationsPerFeature + (iteration + 1));

        int iterationPercent = (numIterationsDone * 90 / numTotalIterations) + 10;
        return iterationPercent;
    }

    private long computeTaskRemainingTime(ParallelSVD_AlgorithmParameters parameters, int feature, int iteration, double iterationTime) {

        int numTotalIterations = (parameters.numFeatures * parameters.numIterationsPerFeature);
        int numIterationsDone = (feature * parameters.numIterationsPerFeature + (iteration + 1));

        long remainingTime = (long) (iterationTime * (numTotalIterations - numIterationsDone));

        return remainingTime;
    }

    public ParallelSVD_AlgorithmParameters extractAlgorithmParameters(final RatingsDataset<? extends Rating> ratingsDataset) {
        ParallelSVD_AlgorithmParameters parameters = new ParallelSVD_AlgorithmParameters();
        parameters.kVvalue = (Float) getParameterValue(K);
        parameters.lrate = getLearningRate();
        parameters.numFeatures = (Integer) getParameterValue(NUM_FEATURES);
        parameters.numIterationsPerFeature = (Integer) getParameterValue(NUM_ITER_PER_FEATURE);
        parameters.smartInit = (Boolean) getParameterValue(SMART_INITIALISATION);
        parameters.maxInitialisation = (float) Math.sqrt(ratingsDataset.getRatingsDomain().max().doubleValue() / parameters.numFeatures);
        parameters.minInitialisation = (float) Math.sqrt(ratingsDataset.getRatingsDomain().min().doubleValue() / parameters.numFeatures);
        return parameters;
    }

    public void trainFeatureParallel(final int numSplits, ArrayList<Set<Integer>> usersSplit, ArrayList<Set<Integer>> itemsSplit, final RatingsDataset<? extends Rating> ratingsDataset, ParallelSVD_AlgorithmParameters parameters, ParallelSVDModel parallelSVDModel, int feature) {
        MultiThreadExecutionManager_NotBlocking<ParallelSVD_TrainSector_Task> multiThread
                = new MultiThreadExecutionManager_NotBlocking<>("",
                        ParallelSVD_TrainSector_SingleTaskExecutor.class);
        multiThread.runInBackground();

        Random random = new Random(getSeedValue());

        for (int split = 0; split < numSplits; split++) {
            for (int offset = 0; offset < numSplits; offset++) {
                int i = split % numSplits;
                int j = (split + offset) % numSplits;

                Set<Integer> usersSet = usersSplit.get(i);
                Set<Integer> itemsSet = itemsSplit.get(j);

                long seed = random.nextLong();

                ParallelSVD_TrainSector_Task parallelSVD_TrainSectorTask
                        = new ParallelSVD_TrainSector_Task(
                                ratingsDataset,
                                parameters,
                                parallelSVDModel,
                                feature,
                                usersSet, itemsSet, seed);

                multiThread.addTask(parallelSVD_TrainSectorTask);
            }
        }

        try {
            multiThread.waitUntilFinished();
        } catch (InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static double privatePredictRating(
            RatingsDataset<? extends Rating> ratingsDataset,
            ParallelSVD_AlgorithmParameters parameters,
            ParallelSVDModel model,
            int idUser,
            int idItem
    ) throws NotEnoughtUserInformation, NotEnoughtItemInformation, UserNotFound, ItemNotFound {
        double prediction = 0;

        if (!model.containsUser(idUser)) {
            throw new NotEnoughtUserInformation("SVD recommendation model does not contains the user.");
        }
        if (!model.containsItem(idItem)) {
            throw new NotEnoughtItemInformation("SVD recommendation model does not contains the item.");
        }

        ArrayList<Double> userFeatures = model.getUserFeatures(idUser);
        ArrayList<Double> itemFeatures = model.getItemFeatures(idItem);

        for (int i = 0; i < parameters.numFeatures; i++) {
            prediction += userFeatures.get(i) * itemFeatures.get(i);
        }

        if (Double.isInfinite(prediction) || Double.isNaN(prediction)) {
            Global.showWarning("Rating prediction overflow occured. Please revise learning rate parammeter");
        }

        return prediction;
    }

    @Override
    public Collection<Recommendation> recommendToUser(DatasetLoader<? extends Rating> datasetLoader, ParallelSVDModel model, Integer idUser, java.util.Set<Integer> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        if (model == null) {
            throw new IllegalArgumentException("SVD recommendation model is null.");
        }

        if (!model.containsUser(idUser)) {
            Global.showWarning("SVD recommendation model does not contains the user (" + idUser + "): Returning empty list.");
            return new ArrayList<>();
        }

        ParallelSVD_AlgorithmParameters parameters = extractAlgorithmParameters(datasetLoader.getRatingsDataset());

        final RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

        ArrayList<Recommendation> ret = new ArrayList<>(candidateItems.size());
        for (int idItem : candidateItems) {
            try {
                Number prediction = privatePredictRating(ratingsDataset, parameters, model, idUser, idItem);

                ret.add(new Recommendation(idItem, prediction));
            } catch (NotEnoughtItemInformation ex) {
                //Fallo de cobertura, no habia ratings del producto en la fase de entrenamiento.
                model.warningItemNotInModel(
                        idItem,
                        "SVD recommendation model does not contains the item (" + idItem + ").",
                        ex);
            } catch (NotEnoughtUserInformation ex) {
                //Fallo de cobertura, no habia ratings del usuario en la fase de entrenamiento.
                model.warningUserNotInModel(idUser,
                        "SVD recommendation model does not contains the user (" + idUser + ").",
                        ex);
            }
        }

        Collections.sort(ret);
        return ret;
    }

    @Override
    public final void setSeedValue(long seedValue) {
        setParameterValue(SEED, seedValue);
    }

    @Override
    public final long getSeedValue() {
        return (Long) getParameterValue(SEED);
    }

    protected final int getNumFeatures() {
        return (Integer) getParameterValue(NUM_FEATURES);
    }

    protected final double getLearningRate() {
        return ((Number) getParameterValue(LEARNING_RATE)).doubleValue();
    }

    protected final int getNumIterPerFeature() {
        return (Integer) getParameterValue(NUM_ITER_PER_FEATURE);
    }

    public void initFeatureMatrices(
            RatingsDataset<? extends Rating> ratingsDataset,
            Map<Integer, ArrayList<Double>> usersFeatures,
            Map<Integer, ArrayList<Double>> itemsFeatures,
            ParallelSVD_AlgorithmParameters parameters,
            long seed
    ) {
        Random randomInit = new Random(seed);

        for (int idUser : ratingsDataset.allUsers()) {
            ArrayList<Double> userFeatures = new ArrayList<>(parameters.numFeatures);

            if (parameters.smartInit) {
                for (int j = 0; j < parameters.numFeatures; j++) {
                    double initValue = getInitialisation(parameters.maxInitialisation, parameters.minInitialisation, randomInit.nextLong());
                    userFeatures.add(initValue);
                }
            } else {
                for (int j = 0; j < parameters.numFeatures; j++) {
                    userFeatures.add(0.01);
                }
            }
            usersFeatures.put(idUser, userFeatures);
        }

        for (int idItem : ratingsDataset.allRatedItems()) {
            ArrayList<Double> itemFeatures = new ArrayList<>(parameters.numFeatures);

            if (parameters.smartInit) {
                for (int j = 0; j < parameters.numFeatures; j++) {
                    double initValue = getInitialisation(parameters.maxInitialisation, parameters.minInitialisation, randomInit.nextLong());
                    itemFeatures.add(initValue);
                }
            } else {
                for (int j = 0; j < parameters.numFeatures; j++) {
                    itemFeatures.add(0.01);
                }
            }
            itemsFeatures.put(idItem, itemFeatures);
        }
    }

    private static ArrayList<Set<Integer>> splitSet(Collection<Integer> elementsToSplit, int numSplits) {
        ArrayList<Set<Integer>> ret = new ArrayList<>();
        for (int i = 0; i < numSplits; i++) {
            ret.add(new TreeSet<>());
        }

        int i = 0;
        for (Iterator<Integer> it = elementsToSplit.iterator(); it.hasNext();) {
            int element = it.next();
            int index = i % numSplits;

            ret.get(index).add(element);

            i++;
        }
        return ret;
    }

    public static double computeMAEofModelInTraining(DatasetLoader<? extends Rating> datasetLoader, ParallelSVDModel parallelSVDModel, int feature) {
        final MeanIterative mae = new MeanIterative();

        for (Rating rating : datasetLoader.getRatingsDataset()) {
            try {
                double predictRating = privatePredictRating_forTraining(parallelSVDModel, rating.idUser, rating.idItem, feature);
                double originalRating = rating.ratingValue.doubleValue();
                mae.addValue(Math.abs(predictRating - originalRating));

            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            } catch (ItemNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            } catch (CannotLoadRatingsDataset ex) {
                ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
            } catch (CannotLoadContentDataset ex) {
                ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
            } catch (NotEnoughtUserInformation | NotEnoughtItemInformation ex) {

            }
        }

        return mae.getMean();
    }

    public static void trainModelWithThisRating(ParallelSVD_AlgorithmParameters algorithmParameters, ParallelSVDModel parallelSVDModel, Rating rating, int feature) {
        double predicted;
        double ratingValue = rating.ratingValue.doubleValue();
        double error = 0;

        try {
            predicted = ParallelSVD.privatePredictRating_forTraining(parallelSVDModel, rating.idUser, rating.idItem, feature);
            error = (ratingValue - predicted);
        } catch (NotEnoughtUserInformation | NotEnoughtItemInformation | UserNotFound | ItemNotFound ex) {
            throw new IllegalStateException(ex);
        }

        double userFeatureValue = parallelSVDModel.getUserFeatures(rating.idUser).get(feature);
        double itemFeatureValue = parallelSVDModel.getItemFeatures(rating.idItem).get(feature);

        double updateUserValue = (error * itemFeatureValue - algorithmParameters.kVvalue * userFeatureValue);
        double updateItemValue = (error * userFeatureValue - algorithmParameters.kVvalue * itemFeatureValue);

        double newUserValue = userFeatureValue + algorithmParameters.lrate * updateUserValue;
        double newItemValue = itemFeatureValue + algorithmParameters.lrate * updateItemValue;

        if (Double.isInfinite(newUserValue) || Double.isInfinite(newItemValue)) {
            throw new IllegalStateException("Los valores nuevos son erroneos");
        } else {
            //compruebo que los valores convergen a un valor bajo
            if (!(newUserValue > 10E20 || newUserValue < -10E20)) {
                parallelSVDModel.setUserFeatureValue(rating.idUser, feature, newUserValue);
            }

            //compruebo que los valores convergen a un valor bajo
            if (!(newItemValue > 10E20 || newItemValue < -10E20)) {
                parallelSVDModel.setItemFeatureValue(rating.idItem, feature, newItemValue);
            }
        }
    }

    public static double privatePredictRating_forTraining(
            ParallelSVDModel model,
            int idUser,
            int idItem,
            int feature
    ) throws NotEnoughtUserInformation, NotEnoughtItemInformation, UserNotFound, ItemNotFound {
        double prediction = 0;

        if (!model.containsUser(idUser)) {
            throw new NotEnoughtUserInformation("SVD recommendation model does not contains the user.");
        }
        if (!model.containsItem(idItem)) {
            throw new NotEnoughtItemInformation("SVD recommendation model does not contains the item.");
        }

        ArrayList<Double> userFeatures = model.getUserFeatures(idUser);
        ArrayList<Double> itemFeatures = model.getItemFeatures(idItem);

        for (int i = 0; i <= feature; i++) {
            prediction += userFeatures.get(i) * itemFeatures.get(i);
        }

        if (Double.isInfinite(prediction) || Double.isNaN(prediction)) {
            Global.showWarning("Rating prediction overflow occured. Please revise learning rate parammeter");
        }
        return prediction;
    }

}
