package delfos.dataset.basic.loader.types;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.trust.TrustDataset;
import delfos.dataset.basic.user.UsersDataset;

/**
 * DatasetLoader<? extends Rating> con todos los métodos posibles, dejando que
 * ocurran en tiempo de ejecución los fallos derivados de la no implementación
 * de alguna de las interfaces de los dataset loader.
 *
* @author Jorge Castro Gallardo
 *
 * @version 26-Noviembre-2013
 * @param <RatingType>
 */
public class CompleteDatasetLoaderAbstract_withTrust<RatingType extends Rating>
        extends DatasetLoaderAbstract<RatingType>
        implements CompleteDatasetLoader<RatingType> {

    private static final long serialVersionUID = 1L;

    @Override
    public RelevanceCriteria getDefaultRelevanceCriteria() {
        throw new IllegalStateException("Not implemented yet.");
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {
        throw new CannotLoadUsersDataset("Not implemented yet.");
    }

    @Override
    public UsersDataset getUsersDataset() throws CannotLoadUsersDataset {
        throw new CannotLoadUsersDataset("Not implemented yet.");
    }

    @Override
    public RatingsDataset<RatingType> getRatingsDataset() throws CannotLoadRatingsDataset {
        throw new CannotLoadRatingsDataset("Not implemented yet.");
    }

    @Override
    public TrustDataset getTrustDataset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
