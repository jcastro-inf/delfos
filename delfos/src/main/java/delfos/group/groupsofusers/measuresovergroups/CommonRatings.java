package delfos.group.groupsofusers.measuresovergroups;

import java.util.Map;
import java.util.TreeMap;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Clase para calcular número de ratings del grupo.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 03-Junio-2013
 */
public class CommonRatings extends GroupMeasureAdapter {

    public static final Parameter numCommon = new Parameter("numCommon", new IntegerParameter(1, Integer.MAX_VALUE, 2));

    public CommonRatings() {
        super();

        addParameter(numCommon);
    }

    public CommonRatings(int numCommon) {
        this();
        setParameterValue(CommonRatings.numCommon, numCommon);
    }

    public int getNumCommon() {
        return (Integer) getParameterValue(numCommon);
    }

    @Override
    public String getNameWithParameters() {
        return getName() + "_" + getNumCommon() + "_members";
    }

    /**
     * Devuelve el grado con el que el grupo indicado es un clique.
     *
     * @param datasetLoader
     * @param group Grupo a comprobar.
     * @return Valor difuso con el que un grupo es un clique.
     */
    @Override
    public double getMeasure(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers group) throws CannotLoadRatingsDataset {

        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        Map<Integer, Integer> numMembersRated = new TreeMap<Integer, Integer>();
        for (int idUser : group) {
            try {
                for (int idItem : ratingsDataset.getUserRated(idUser)) {
                    if (!numMembersRated.containsKey(idItem)) {
                        numMembersRated.put(idItem, 0);
                    }
                    numMembersRated.put(idItem, numMembersRated.get(idItem) + 1);
                }
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }

        double value = 0;
        for (int idItem : numMembersRated.keySet()) {
            int numMembers = numMembersRated.get(idItem);
            if (numMembers >= getNumCommon()) {
                value++;
            }
        }

        value = value / numMembersRated.size();

        return value;
    }
}
