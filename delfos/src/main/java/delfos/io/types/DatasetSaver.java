package delfos.io.types;

import java.io.FileNotFoundException;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.CannotSaveUsersDataset;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.UsersDataset;

/**
 * Clase abstracta que especifica los métodos que debe implementar una clase
 * encargada de guardar en cualquier tipo de persistencia un dataset cualquiera
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 15-Noviembre-2013
 */
public abstract class DatasetSaver extends ParameterOwnerAdapter {

    /**
     * Método que almacena el dataset de valoraciones.
     *
     * @param rd Dataset de valoraciones a almacenar en persistencia.
     */
    public abstract void saveRatingsDataset(RatingsDataset<? extends Rating> rd);

    /**
     * Método que almacena el dataset de contenido.
     *
     * @param cd Dataset de contenido de los productos a almacenar en
     * persistencia.
     */
    public abstract void saveContentDataset(ContentDataset cd);

    /**
     * Guarda un dataset de usuarios en el archivo indicado en el parámetro de
     * la clase, en formato CSV.
     *
     * @param usersDataset Dataset de usuarios que se desea almacenar.
     */
    public abstract void saveUsersDataset(UsersDataset usersDataset) throws CannotSaveUsersDataset;

    public abstract ContentDataset loadContentDataset() throws CannotLoadContentDataset, FileNotFoundException;

    public abstract RatingsDataset<? extends Rating> loadRatingsDataset() throws CannotLoadRatingsDataset, FileNotFoundException;

    public abstract UsersDataset loadUsersDataset() throws CannotLoadUsersDataset, FileNotFoundException;

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.DATASET_SAVER;
    }
}
