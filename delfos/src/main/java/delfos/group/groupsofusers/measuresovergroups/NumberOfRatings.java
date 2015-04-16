package delfos.group.groupsofusers.measuresovergroups;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Clase para calcular número de ratings del grupo.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 03-Junio-2013
 */
public class NumberOfRatings extends GroupMeasureAdapter {


    public NumberOfRatings() {
        super();
    }


    /**
     * Devuelve el grado con el que el grupo indicado es un clique.
     *
     * @param trustNetwork Red de confianza.
     * @param group Grupo a comprobar.
     * @return Valor difuso con el que un grupo es un clique.
     */
    @Override
    public double getMeasure(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers group) throws CannotLoadRatingsDataset {
        int numRatings = 0;
        for(int idUser:group){
            try {
                numRatings += datasetLoader.getRatingsDataset().getUserRated(idUser).size();
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }
        return numRatings;
    }
}
