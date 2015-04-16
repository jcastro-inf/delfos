package delfos.experiment.validation.predictionprotocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import delfos.common.Global;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;

/**
 * Implementa la validación de predicción que realiza una validación que predice
 * N valoraciones de cada usuario. Por lo tanto, en cada perfil de usuario
 * quedarán X-n valoraciones, donde X es el número d evaloraciones original que
 * el usuario había hecho.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.1 21-Jan-2013
 * @version 1.0 Unknow date
 */
public class PredictN extends PredictionProtocol {

    public static final long serialVersionUID = 1L;
    /**
     * Cantidad de valoraciones que se predicen para un usuario
     */
    public static Parameter n = new Parameter("n", new IntegerParameter(1, 10000, 4));

    /**
     * Constructor por defecto que deja el numero de valoraciones a 4
     */
    public PredictN() {
        super();
        addParameter(n);
    }

    /**
     * Constructor que asigna el n especificado en el parámetro <code>n</code>
     * para que se prediga dicho número de valoraciones de cada usuario
     *
     * @param nValue Cantidad de valoraciones que se predicen para un usuario
     */
    public PredictN(int nValue) {
        addParameter(n);
        setParameterValue(n, nValue);
    }

    @Override
    public Collection<Collection<Integer>> getRecommendationRequests(RatingsDataset<? extends Rating> testRatingsDataset, int idUser) throws UserNotFound {
        Random random = new Random(getSeedValue());
        Collection<Integer> userRated = new TreeSet<>(testRatingsDataset.getUserRated(idUser));
        Set<Integer> extraidos = new TreeSet<>();
        Number extraer = (Number) getParameterValue(PredictN.n);

        if (extraer.intValue() > userRated.size()) {
            //no se pueden extraer el número que se solicita, qué hacer?
            Global.showWarning("User " + idUser + " has not enough test rating to extract " + extraer + "\n");

            Collection<Collection<Integer>> ret = new ArrayList<>(extraidos.size());
            List<Integer> l = new ArrayList<>(userRated);
            ret.add(l);
            return ret;
        } else {
            while (!userRated.isEmpty() && extraidos.size() != extraer.doubleValue()) {
                int index = random.nextInt(userRated.size());
                int idItem = userRated.toArray(new Integer[1])[index];
                userRated.remove(idItem);
                extraidos.add(idItem);
            }

            Collection<Collection<Integer>> ret = new ArrayList<>(extraidos.size());
            ret.add(extraidos);
            return ret;
        }
    }
}
