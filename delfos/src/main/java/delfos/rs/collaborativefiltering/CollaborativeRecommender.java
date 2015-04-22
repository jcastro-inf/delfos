package delfos.rs.collaborativefiltering;

import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.rs.RecommenderSystemAdapter;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.TreeSet;

/**
 * Clase de la que deben heredar todos los sistemas de recomendación que estén
 * basados en la predicción de las valoraciones.
 *
 * <p>
 * <p>
 * <b>IMPORTANTE</b>Las clases que hereden de {@link CollaborativeRecommender}
 * deben llamar siempre al constructor por defecto de esta clase en sus
 * constructores, utilizando la sentencia super();. En caso de que no se
 * implemente de esta manera, el correcto comportamiento del sistema no está
 * garantizado.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 2.0 Unknown date
 * @version 2.1 (18-Feb-2013)
 * @param <RecommenderSystemModel> Modelo de recomendación del sistema.
 */
public abstract class CollaborativeRecommender<RecommenderSystemModel>
        extends RecommenderSystemAdapter<RecommenderSystemModel> {

    /**
     * Constructor por defecto de un sistema de recomendación colaborativo.
     */
    public CollaborativeRecommender() {
        super();
    }

    /**
     * Los sistemas de recomendación colaborativos funcionan prediciendo la
     * valoración para los productos no valorados, por lo que siempre devuelve
     * true;
     *
     * {@inheritDoc }
     *
     * @return
     */
    @Override
    public final boolean isRatingPredictorRS() {
        return true;
    }

    /**
     * Transforma una predicción del sistema de recomendación en una predicción
     * dentro del rango de valoraciones. De esta manera se mejora el error de
     * predicción.
     *
     * @param datasetLoader Conjunto de datos.
     * @param rating Predicción en bruto.
     * @return Predicción dentro del rango de valoración.
     */
    public Number toRatingRange(DatasetLoader<? extends Rating> datasetLoader, Number rating) {
        try {
            if (rating.floatValue() < datasetLoader.getRatingsDataset().getRatingsDomain().min().doubleValue()) {
                return datasetLoader.getRatingsDataset().getRatingsDomain().min();
            }
            if (rating.floatValue() > datasetLoader.getRatingsDataset().getRatingsDomain().max().doubleValue()) {
                return datasetLoader.getRatingsDataset().getRatingsDomain().max();
            }
            return rating;
        } catch (CannotLoadRatingsDataset ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Predice la valoración que un usuario daría a un producto, utilizando el
     * sistema de recomendación.
     *
     * @param datasetLoader Conjunto de datos.
     * @param model Modelo de recomendación.
     * @param idUser Usuario para el que se realiza la predicción.
     * @param idItem Producto para el que se realiza la predicción.
     * @return Valoración predicha del usuario sobre el producto. Si no se puede
     * calcular, devuelve null (indica un fallo de cobertura)
     * @throws UserNotFound Si no se encuentra el usuario en los datasets.
     * @throws ItemNotFound Si no se encuenta el producto en los datasets.
     * @throws delfos.common.exceptions.ratings.NotEnoughtUserInformation
     */
    public Number predictRating(DatasetLoader<? extends Rating> datasetLoader, RecommenderSystemModel model, int idUser, int idItem)
            throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {
        if (Global.isVerboseAnnoying()) {
            Global.showMessage("Predicting rating of user " + idUser + " over item " + idItem + "\n");
            try {
                Global.showMessage("Valoración real " + datasetLoader.getRatingsDataset().getRating(idUser, idItem) + "\n");
                Global.showMessage("Media user " + datasetLoader.getRatingsDataset().getMeanRatingUser(idUser) + "\n");
                Global.showMessage("Media item " + datasetLoader.getRatingsDataset().getMeanRatingItem(idItem) + "\n");
            } catch (CannotLoadRatingsDataset ex) {
                throw new IllegalStateException(ex);
            }
        }

        TreeSet<Integer> items = new TreeSet<>();
        items.add(idItem);

        Collection<Recommendation> recommendOnly = recommendOnly(datasetLoader, model, idUser, items);
        if (recommendOnly.isEmpty()) {
            if (Global.isVerboseAnnoying()) {
                Global.showMessage("Prediction of rating of user " + idUser + " over item " + idItem + " can't be predicted\n");
            }
            return null;
        } else {
            double prediction = recommendOnly.iterator().next().getPreference().doubleValue();
            if (Global.isVerboseAnnoying()) {
                Global.showMessage("Prediction of rating of user " + idUser + " over item " + idItem + " ---> " + prediction + "\n");
            }
            return prediction;
        }
    }

}
