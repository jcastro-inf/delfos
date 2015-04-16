package delfos.group.groupsofusers.measuresovergroups;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Clase para calcular número de miembros del grupo.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 03-Junio-2013
 */
public class SizeOfGroup extends GroupMeasureAdapter {
    public SizeOfGroup() {
        super();
    }
    
    /**
     * Devuelve el número de miembros del grupo.
     *
     * @param group Grupo a comprobar.
     * @return número de miembros del grupo.
     */
    @Override
    public double getMeasure(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers group) throws CannotLoadRatingsDataset {
        return group.size();
    }
}
