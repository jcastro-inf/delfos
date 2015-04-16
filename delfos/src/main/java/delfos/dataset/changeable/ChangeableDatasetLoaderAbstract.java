package delfos.dataset.changeable;

import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.UsersDataset;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.CannotSaveContentDataset;
import delfos.common.exceptions.dataset.CannotSaveRatingsDataset;
import delfos.common.exceptions.dataset.CannotSaveUsersDataset;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;

/**
 * Métodos por defecto que un cargador dataset modificable debe implementar.
 *
* @author Jorge Castro Gallardo
 *
 * @version 17-sep-2013
 */
public abstract class ChangeableDatasetLoaderAbstract extends ParameterOwnerAdapter implements ChangeableDatasetLoader {

    @Override
    public final RatingsDataset<Rating> getRatingsDataset() throws CannotLoadRatingsDataset {
        return getChangeableRatingsDataset();
    }

    @Override
    public final UsersDataset getUsersDataset() throws CannotLoadUsersDataset {
        return getChangeableUsersDataset();
    }

    @Override
    public final ContentDataset getContentDataset() throws CannotLoadContentDataset {
        return getChangeableContentDataset();
    }

    /**
     * Ordena que los datos sean guardados en el método persistente
     * correspondiente del dataset.
     */
    @Override
    public void commitChangesInPersistence() throws CannotSaveContentDataset, CannotSaveUsersDataset, CannotSaveRatingsDataset {
        try {
            ChangeableRatingsDataset changeableRatingsDataset = getChangeableRatingsDataset();
            changeableRatingsDataset.commitChangesInPersistence();
        } catch (CannotLoadRatingsDataset ex) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
        }
        try {
            ChangeableContentDataset changeableContentDataset = getChangeableContentDataset();
            changeableContentDataset.commitChangesInPersistence();
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
        }
        try {
            ChangeableUsersDataset changeableUsersDataset = getChangeableUsersDataset();
            if (changeableUsersDataset != null) {
                changeableUsersDataset.commitChangesInPersistence();
            }
        } catch (CannotLoadUsersDataset ex) {
            ERROR_CODES.CANNOT_LOAD_USERS_DATASET.exit(ex);
        }
    }

    @Override
    public final ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.DATASET_LOADER;
    }
}
