package delfos.rs.collaborativefiltering.svd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import delfos.common.Chronometer;
import delfos.common.DateCollapse;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtItemInformation;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.BooleanParameter;
import delfos.common.parameters.restriction.FloatParameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.experiment.SeedHolder;
import delfos.rs.collaborativefiltering.CollaborativeRecommender;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.persistence.database.DAOTryThisAtHomeDatabaseModel;
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
 *
 * @version 1.0 Unknown date
 * @version 1.1 (Unknown date) Añadido parámetro para controlar la
 * inicialización inteligente.
 * @version 1.2 (28 de Febrero de 2013)
 */
public class TryThisAtHomeSVD
        extends CollaborativeRecommender<TryThisAtHomeSVDModel>
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
     * Parámetro para indicar si se realiza una normalización de las
     * valoraciones utilizando la valoración media del usuario. Por defecto esta
     * mejora está activa.
     */
    public static final Parameter NORMALIZE_WITH_USER_MEAN = new Parameter("Normalize_with_mean", new BooleanParameter(Boolean.FALSE));
    /**
     * Parámetro para indicar si las valoraciones predichas se truncan para
     * estar dentro del rango de valoraciones.
     */
    public static final Parameter PREDICT_IN_RATING_RANGE = new Parameter("Predict_in_range", new BooleanParameter(Boolean.FALSE));
    /**
     * Parámetro para penalizar valores grandes de las características.
     */
    public static final Parameter K = new Parameter("K", new FloatParameter(0.0001f, 1f, 0.02f), "Parámetro para penalizar valores grandes de las características.");

    /**
     * Constructor por defecto, que añade los parámetros del sistema de
     * recomendación.
     */
    public TryThisAtHomeSVD() {
        super();
        addParameter(K);
        addParameter(SEED);
        addParameter(NUM_FEATURES);
        addParameter(LEARNING_RATE);
        addParameter(NUM_ITER_PER_FEATURE);
        addParameter(NORMALIZE_WITH_USER_MEAN);
        addParameter(SMART_INITIALISATION);
        addParameter(PREDICT_IN_RATING_RANGE);
    }

    /**
     * Constructor que asigna los valores indicados como número de
     * características y número de iteraciones del sistema.
     *
     * @param featuresValue Número de características que se calculan
     * @param iterationsValue Número de iteraciones para cada característica
     */
    public TryThisAtHomeSVD(int featuresValue, int iterationsValue) {
        this();
        setParameterValue(TryThisAtHomeSVD.NUM_FEATURES, featuresValue);
        setParameterValue(NUM_ITER_PER_FEATURE, iterationsValue);
    }

    /**
     * Devuelve un valor aleatorio (diferente en cada llamada y distinto de
     * cero) que se utiliza para la inicialización de las matrices.
     * <p>
     * NOTA: Se utiliza este método para devolver una inicialización lo más
     * variada posible, teniendo en cuenta la configuración establecida para que
     * los valores no se vayan a infinito durante las iteraciones.
     *
     * @param maxInitialisation Para que la inicialización tenga la máxima
     * variedad de valores posible, sin que en el proceso de aprendizaje los
     * valores excedan un rango considerado como seguro. Amacena el valor
     * máximo.
     *
     * @param minInitialisation Para que la inicialización tenga la máxima
     * variedad de valores posible, sin que en el proceso de aprendizaje los
     * valores excedan un rango considerado como seguro. Amacena el valor
     * mínimo.
     *
     * @return Valor aleatorio de inicialización.
     */
    protected final double getInitialisation(double maxInitialisation, double minInitialisation, long seed) {
        double ret = 0;

        Random random = new Random(seed);
        while (ret == 0) {
            ret = random.nextFloat();
            ret = (ret * (maxInitialisation - minInitialisation)) + minInitialisation;
        }

        return ret;
    }

    @Override
    public TryThisAtHomeSVDModel build(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadRatingsDataset, CannotLoadContentDataset {

        boolean normalise = (Boolean) getParameterValue(NORMALIZE_WITH_USER_MEAN);
        final double lrate = getLearningRate();
        final int numFeatures = (Integer) getParameterValue(NUM_FEATURES);
        final int numIterationsPerFeature = (Integer) getParameterValue(NUM_ITER_PER_FEATURE);
        final RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        final float Kvalue = getK();

        final double maxInitialisation = (float) Math.sqrt(ratingsDataset.getRatingsDomain().max().doubleValue() / numFeatures);
        final double minInitialisation = (float) Math.sqrt(ratingsDataset.getRatingsDomain().min().doubleValue() / numFeatures);

        final TreeMap<Integer, Integer> itemsIndex = new TreeMap<>();
        final TreeMap<Integer, Integer> usersIndex = new TreeMap<>();
        int index = 0;
        for (int idItem : ratingsDataset.allRatedItems()) {
            itemsIndex.put(idItem, index);
            index++;
        }

        index = 0;
        for (int idUser : ratingsDataset.allUsers()) {
            usersIndex.put(idUser, index);
            index++;
        }

        final int users = usersIndex.size();
        final int items = itemsIndex.size();

        ArrayList<ArrayList<Double>> usersFeatures = new ArrayList<>(users);
        ArrayList<ArrayList<Double>> itemsFeatures = new ArrayList<>(items);

        for (int i = 0; i < users; i++) {
            usersFeatures.add(new ArrayList<>(numFeatures));
            for (int j = 0; j < numFeatures; j++) {
                usersFeatures.get(i).add(null);
            }
        }
        for (int i = 0; i < items; i++) {
            itemsFeatures.add(new ArrayList<>(numFeatures));
            for (int j = 0; j < numFeatures; j++) {
                itemsFeatures.get(i).add(null);
            }
        }

        if ((Boolean) getParameterValue(SMART_INITIALISATION)) {
            Random random = new Random(getSeedValue());
            for (int i = 0; i < users; i++) {
                for (int j = 0; j < numFeatures; j++) {
                    long seed = random.nextLong();
                    double initValue = getInitialisation(maxInitialisation, minInitialisation, seed);
                    usersFeatures.get(i).set(j, initValue);
                }
            }
            for (int i = 0; i < items; i++) {
                for (int j = 0; j < numFeatures; j++) {
                    long seed = random.nextLong();
                    double initValue = getInitialisation(maxInitialisation, minInitialisation, seed);
                    itemsFeatures.get(i).set(j, initValue);
                }
            }
        } else {
            for (int i = 0; i < users; i++) {
                for (int j = 0; j < numFeatures; j++) {
                    usersFeatures.get(i).set(j, 0.01);
                }
            }
            for (int i = 0; i < items; i++) {
                for (int j = 0; j < numFeatures; j++) {
                    itemsFeatures.get(i).set(j, 0.01);
                }
            }
        }

        MeanIterative tiempoCiclo = new MeanIterative(20);
        fireBuildingProgressChangedEvent("training values", 0, -1);

        Global.showMessage("Feature\tIteration\tMAE\tThe error has improved" + "\n");

        /**
         * Modelo que se entrena a continuación.
         */
        TryThisAtHomeSVDModel model = new TryThisAtHomeSVDModel(usersFeatures, usersIndex, itemsFeatures, itemsIndex);

        double maeAnterior = 0;
        for (int indexFeature = 0; indexFeature < numFeatures; indexFeature++) {

            Chronometer c = new Chronometer();
            for (int iteration = 0; iteration < numIterationsPerFeature; iteration++) {

                c.reset();
                MeanIterative meanAbsoluteError = new MeanIterative();
                for (Rating rating : ratingsDataset) {
                    int idUser = rating.idUser;
                    int idItem = rating.idItem;
                    Integer indexUser = usersIndex.get(rating.idUser);
                    Integer indexItem = itemsIndex.get(rating.idItem);

                    double predicted;
                    double ratingValue = rating.ratingValue.doubleValue();
                    double error = 0;
                    try {
                        predicted = privatePredictRating(datasetLoader.getRatingsDataset(), model, idUser, idItem);
                        error = (ratingValue - predicted);
                    } catch (NotEnoughtUserInformation | NotEnoughtItemInformation | UserNotFound | ItemNotFound ex) {
                        throw new IllegalStateException(ex);
                    }
                    meanAbsoluteError.addValue(Math.abs(error));

                    double getUser = usersFeatures.get(indexUser).get(indexFeature);
                    double getItem = itemsFeatures.get(indexItem).get(indexFeature);

                    double updateUserValue = (error * getItem - Kvalue * getUser);
                    double updateItemValue = (error * getUser - Kvalue * getItem);

                    double newUserValue = getUser + lrate * updateUserValue;
                    double newItemValue = getItem + lrate * updateItemValue;
                    if (Double.isInfinite(newUserValue) || Double.isInfinite(newItemValue)) {
                        throw new IllegalStateException("Los valores nuevos son erroneos");
                    } else {
                        //compruebo que los valores convergen a un valor bajo
                        if (!(newUserValue > 10E20 || newUserValue < -10E20)) {
                            usersFeatures.get(indexUser).set(indexFeature, newUserValue);
                        }

                        //compruebo que los valores convergen a un valor bajo
                        if (!(newItemValue > 10E20 || newItemValue < -10E20)) {
                            itemsFeatures.get(indexItem).set(indexFeature, newItemValue);
                        }
                    }
                }

                long diff = c.getPartialElapsed();

                tiempoCiclo.addValue(diff);

                Global.showMessage(indexFeature + "\t" + iteration + "\t" + String.format("%.8f", meanAbsoluteError.getMean()) + "\t" + String.format("%.8f", (maeAnterior - meanAbsoluteError.getMean())) + "\t time: " + DateCollapse.collapse(diff) + "\n");
                maeAnterior = meanAbsoluteError.getMean();
                int totalIteraciones = numFeatures * numIterationsPerFeature;
                int iterActual = indexFeature * numIterationsPerFeature + iteration + 1;

                long tiempoRestante = (long) (tiempoCiclo.getMean() * (totalIteraciones - iterActual));
                fireBuildingProgressChangedEvent("Training features values", ((indexFeature * numIterationsPerFeature + (iteration + 1)) * 90 / (numFeatures * numIterationsPerFeature)) + 10, tiempoRestante);
            }
        }

        if (Global.isVerboseAnnoying()) {

            Global.showMessage("=======================================\n");
            Global.showMessage("User features:\n");
            for (int idUser : ratingsDataset.allUsers()) {
                Global.showMessage("User " + idUser + " \t" + model.getUserFeatures(idUser).toString() + "\n");
            }
            Global.showMessage("---------------------------------------\n");
            Global.showMessage("Item features:\n");

            for (int idItem : ratingsDataset.allRatedItems()) {
                Global.showMessage("Item " + idItem + " \t" + model.getItemFeatures(idItem).toString() + "\n");
            }
            Global.showMessage("=======================================\n");
        }

        return model;
    }

    /**
     * Predice la valoración que el usuario daría sobre el producto utilizando
     * el modelo indicado.
     *
     * @param ratingsDataset Dataset de valoraciones.
     * @param model Modelo de recomendación.
     * @param idUser Usuario para el que se predice la valoración
     * @param idItem Producto para el que se predice la valoración
     * @return Valoración predicha del usuario indicado sobre el producto
     * indicado
     * @throws NotEnoughtUserInformation Si el usuario no se encuentra en el
     * dataset de ratings.
     * @throws NotEnoughtItemInformation Si el producto no se encuentra en el
     * dataset de ratings.
     * @throws delfos.common.exceptions.dataset.users.UserNotFound
     * @throws delfos.common.exceptions.dataset.items.ItemNotFound
     */
    protected final double privatePredictRating(
            RatingsDataset<? extends Rating> ratingsDataset,
            TryThisAtHomeSVDModel model,
            int idUser,
            int idItem) throws NotEnoughtUserInformation, NotEnoughtItemInformation, UserNotFound, ItemNotFound {
        double prediction = 0;

        if (!model.getUsersIndex().containsKey(idUser)) {
            throw new NotEnoughtUserInformation("SVD recommendation model does not contains the user.");
        }
        if (!model.getItemsIndex().containsKey(idItem)) {
            throw new NotEnoughtItemInformation("SVD recommendation model does not contains the item.");
        }

        int numFeatures = getNumFeatures();

        ArrayList<Double> user = model.getAllUserFeatures().get(model.getUsersIndex().get(idUser));
        ArrayList<Double> item = model.getAllItemFeatures().get(model.getItemsIndex().get(idItem));

        if (user.size() != item.size()) {
            throw new IllegalArgumentException("Users and item models does not have the same number of features!");
        }

        for (int i = 0; i < user.size() && i <= numFeatures; i++) {
            prediction += user.get(i) * item.get(i);
        }

        if (Double.isInfinite(prediction) || Double.isNaN(prediction)) {
            Global.showWarning("Rating prediction overflow occured. Please revise learning rate parammeter");
        }

        if (isNormalised()) {
            float meanRating = ratingsDataset.getMeanRating();
            float meanRatingUser = meanRating - ratingsDataset.getMeanRatingUser(idUser);
            float meanRatingItem = meanRating - ratingsDataset.getMeanRatingItem(idItem);

            prediction = prediction + meanRating + meanRatingUser + meanRatingItem;
        }
        return prediction;
    }

    @Override
    public Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, TryThisAtHomeSVDModel model, Integer idUser, java.util.Set<Integer> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        if (model == null) {
            throw new IllegalArgumentException("SVD recommendation model is null.");
        }

        if (!model.getUsersIndex().containsKey(idUser)) {
            Global.showWarning("SVD recommendation model does not contains the user (" + idUser + "): Returning empty list.");

            if (datasetLoader instanceof UsersDatasetLoader) {
                UsersDatasetLoader usersDatasetLoader = (UsersDatasetLoader) datasetLoader;

                User user = usersDatasetLoader.getUsersDataset().getUser(idUser);
            }

            return new ArrayList<>();
        }

        final RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        boolean toRatingRange = (Boolean) getParameterValue(PREDICT_IN_RATING_RANGE);

        ArrayList<Recommendation> ret = new ArrayList<>(candidateItems.size());
        for (int idItem : candidateItems) {
            try {
                Number prediction = privatePredictRating(ratingsDataset, model, idUser, idItem);
                if (toRatingRange) {
                    prediction = toRatingRange(datasetLoader, prediction);
                }
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

    protected final boolean isNormalised() {
        return (Boolean) getParameterValue(NORMALIZE_WITH_USER_MEAN);
    }

    protected final double getLearningRate() {
        return ((Number) getParameterValue(LEARNING_RATE)).doubleValue();
    }

    protected final int getNumIterPerFeature() {
        return (Integer) getParameterValue(NUM_ITER_PER_FEATURE);
    }

    @Override
    public void saveModel(DatabasePersistence databasePersistence, TryThisAtHomeSVDModel model) throws FailureInPersistence {
        new DAOTryThisAtHomeDatabaseModel().saveModel(databasePersistence, model);
    }

    @Override
    public TryThisAtHomeSVDModel loadModel(DatabasePersistence databasePersistence, Collection<Integer> users, Collection<Integer> items) throws FailureInPersistence {
        return new DAOTryThisAtHomeDatabaseModel().loadModel(databasePersistence, users, items);
    }

    /**
     * Devuelve el valor de penalización de valores grandes para las
     * características.
     *
     * @return
     */
    protected final float getK() {
        return (Float) getParameterValue(K);
    }
}
