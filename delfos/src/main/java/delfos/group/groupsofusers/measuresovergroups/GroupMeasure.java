package delfos.group.groupsofusers.measuresovergroups;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.parameters.ParameterOwner;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Define los métodos para solicitar una medida que se aplica sobre un grupo de
 * usuarios.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 03-Jun-2013
 */
public interface GroupMeasure extends ParameterOwner {

    /**
     * Devuelve una medida asociada al grupo, que sirve para destacar una
     * característica concreta del mismo.
     *
     * @param datasetLoader 
     * @param group
     * @return
     * @throws CannotLoadRatingsDataset
     */
    public double getMeasure(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers group) throws CannotLoadRatingsDataset;
}
