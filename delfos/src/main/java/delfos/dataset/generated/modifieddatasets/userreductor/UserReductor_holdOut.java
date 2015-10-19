package delfos.dataset.generated.modifieddatasets.userreductor;

import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.FloatParameter;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.generated.modifieddatasets.DatasetSampler;
import delfos.dataset.loaders.csv.CSVfileDatasetLoader;
import java.util.Random;
import java.util.TreeSet;

/**
 * Envoltura de los datasets que se encarga de realizar una selección aleatoria
 * de entre todos los usuarios para trabajar con un dataset más pequeño
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.1 (21-01-2013) Ahora implementa de {@link RatingsDatasetAdapter}
 * @version 21-Enero-2014 Clase renombrada para claridad de su funcionamiento.
 */
public class UserReductor_holdOut extends DatasetSampler {

    private static final long serialVersionUID = 1L;
    /**
     * Parámetro para especificar el porcentaje de usuarios que se conservará en
     * el dataset reducido.
     */
    public static final Parameter usersPercentage = new Parameter("usersPercentage", new FloatParameter(0.0f, 1.0f, 0.1f));
    private UserReductor_allowedUsers<? extends Rating> ratingsDataset = null;

    public UserReductor_holdOut() {
        this(new CSVfileDatasetLoader());
    }

    public UserReductor_holdOut(DatasetLoader<? extends Rating> defaultValue) {
        super(defaultValue);

        addParameter(usersPercentage);
    }

    @Override
    public void shuffle() throws CannotLoadRatingsDataset {
        Random randomGenerator = new Random(getSeedValue());

        DatasetLoader<? extends Rating> loader = (DatasetLoader) getParameterValue(ORIGINAL_DATASET_PARAMETER);
        RatingsDataset<? extends Rating> _ratingsDataset = loader.getRatingsDataset();

        Integer[] users = _ratingsDataset.allUsers().toArray(new Integer[1]);
        TreeSet<Integer> allowedUsers = new TreeSet<>();
        float percentage = (Float) getParameterValue(usersPercentage);
        int numUsuarios = (int) (users.length * percentage);
        Global.showInfoMessage("Using " + numUsuarios + " users: " + "\n");
        for (int i = 0; i < numUsuarios; i++) {
            int indexUser = randomGenerator.nextInt(users.length);
            while (users[indexUser] == null) {
                indexUser = randomGenerator.nextInt(users.length);
            }
            allowedUsers.add(users[indexUser]);
            users[indexUser] = null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int idUser : allowedUsers) {
            sb.append(idUser).append(",");
        }
        sb.setCharAt(sb.length() - 1, ']');
        Global.showInfoMessage(sb.toString() + "\n");

        ratingsDataset = new UserReductor_allowedUsers(loader.getRatingsDataset(), allowedUsers);
    }

    @Override
    public RatingsDataset<? extends Rating> getRatingsDataset() throws CannotLoadRatingsDataset {
        if (ratingsDataset == null) {
            throw new CannotLoadRatingsDataset("must call shuffle before using this method");
        } else {
            return ratingsDataset;
        }
    }

    @Override
    public RelevanceCriteria getDefaultRelevanceCriteria() {
        return new RelevanceCriteria(4);
    }
}
