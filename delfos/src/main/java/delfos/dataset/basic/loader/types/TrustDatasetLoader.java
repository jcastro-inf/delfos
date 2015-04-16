package delfos.dataset.basic.loader.types;

import delfos.dataset.basic.trust.TrustDataset;
import delfos.dataset.basic.trust.TrustStatement;
import delfos.common.exceptions.dataset.CannotLoadTrustDataset;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 26-Noviembre-2013
 * @param <TrustStatementType>
 */
public interface TrustDatasetLoader<TrustStatementType extends TrustStatement> {

    public TrustDataset<TrustStatementType> getTrustDataset() throws CannotLoadTrustDataset;
}
