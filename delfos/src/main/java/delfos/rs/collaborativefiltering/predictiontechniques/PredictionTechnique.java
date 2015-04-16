package delfos.rs.collaborativefiltering.predictiontechniques;

import java.util.Collection;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.collaborativefiltering.knn.MatchRating;
import delfos.common.exceptions.CouldNotPredictRating;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;

/**
 * Métodos que una clase que implemente una técnica de predicción de ratings
 * debe implementar.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 (18-Feb-2013)
 */
public abstract class PredictionTechnique extends ParameterOwnerAdapter {

    /**
     * Predice una valoración a partir de las valoraciones en común que se dan
     * en el parámetro ratings. Se dan adicionalmente los id de usuario y
     * producto, además del dataset para que sea posible solicitar más datos,
     * como la valoración media de un usuario.
     *
     * @param idUser Usuario para el que se predice la valoración.
     * @param idItem Producto para el que se predice la valoración.
     * @param ratings Colección de valoraciones en común, es decir, valoraciones
     * hechas sobre el producto que se recomienda o sobre el usuario para el que
     * se predice.
     * @param ratingsDataset Conjunto de valoraciones para obtener datos
     * adicionales, en caso de que se necesiten.
     * @return Predicción de la valoración. Devuelve null si no se puede
     * predecir.
     * @throws CouldNotPredictRating Si no se puede predecir la valoración.
     * @throws UserNotFound Si el usuario no existe.
     * @throws ItemNotFound Si el producto no existe.
     */
    public abstract float predictRating(int idUser, int idItem, Collection<MatchRating> ratings, RatingsDataset<? extends Rating> ratingsDataset) throws CouldNotPredictRating, UserNotFound, ItemNotFound;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PredictionTechnique) {
            PredictionTechnique predictionTechnique = (PredictionTechnique) obj;
            return this.getName().equals(predictionTechnique.getName());
        } else {
            return false;
        }

    }

    @Override
    public int hashCode() {
        int hash = getName().hashCode();
        return hash;
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.PREDICTION_TECHNIQUE;
    }

}
