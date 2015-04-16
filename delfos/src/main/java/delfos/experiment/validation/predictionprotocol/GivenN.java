package delfos.experiment.validation.predictionprotocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import delfos.common.Global;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.RecommenderSystemAdapter;

/**
 * Implementa la validación de predicción que realiza una validación dadas N
 * valoraciones para el usuario que se predice
 *
* @author Jorge Castro Gallardo
 */
public class GivenN extends PredictionProtocol {

    private static final long serialVersionUID = 1L;
    /**
     * Cantidad de valoraciones que se usan en la predicción para un usuario
     */
    public static Parameter n = new Parameter("n", new IntegerParameter(1, 10000, 4));

    /**
     * Constructor por defecto que deja el numero de valoraciones a 4
     */
    public GivenN() {
        super();
        addParameter(n);
    }

    /**
     * Constructor que asigna el n especificado en el parámetro <code>n</code>
     * para que se use dicho número de valoraciones de cada usuario
     *
     * @param n Cantidad de valoraciones que se usan en la predicción para un
     * usuario
     */
    public GivenN(int n) {
        addParameter(GivenN.n);
        setParameterValue(GivenN.n, n);
    }

    /**
     * Devuelve las valoraciones que se han de predecir en una lista
     * independiente. Para un correcto uso de los resultados, se deben extraer
     * del dataset de valoraciones que el sistema de recomendación usa todas las
     * valoraciones que se van a predecir.
     *
     * @param idUser
     * @return Lista de listas con los elementos que se predicen. Cada lista
     * representa una petición de recomendaciones, es decir, una llamada a
     * {@link RecommenderSystemAdapter#recommendOnly(java.lang.Integer, java.util.Collection) }
     * en la que la colección que se pasan son los elementos de la lista.
     * @throws UserNotFound
     */
    @Override
    public Collection<Collection<Integer>> getRecommendationRequests(RatingsDataset<? extends Rating> testRatingsDataset, int idUser) throws UserNotFound {
        Random random = new Random(getSeedValue());
        Integer[] itemsRated = testRatingsDataset.getUserRatingsRated(idUser).keySet().toArray(new Integer[0]);
        int nValue = (Integer) getParameterValue(n);
        Set<Integer> dadosN = new TreeSet<>();

        if (nValue > itemsRated.length) {
            Global.showWarning("Cannot apply " + GivenN.class.getName() + "\n");
            Global.showWarning("cause: user '" + idUser + "' have less than " + nValue + " ratings");
        }

        while (dadosN.size() < nValue) {
            dadosN.add(itemsRated[random.nextInt(itemsRated.length)]);
        }

        Set<Integer> predecir = new TreeSet<>();
        for (int idItem : itemsRated) {
            if (!dadosN.contains(idItem)) {
                predecir.add(idItem);
            }
        }

        Collection<Collection<Integer>> ret = new ArrayList<>(predecir.size());
        ret.add(predecir);
        return ret;
    }
}
