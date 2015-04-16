package delfos.dataset.loaders.epinions;

import delfos.dataset.basic.trust.TrustStatement;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 18-dic-2013
 */
class EPinionsTrustStatement extends TrustStatement {

    long timestamp;

    public EPinionsTrustStatement(int idUserSource, int idUserDestiny, float trustValue, long timestamp) {
        super(idUserSource, idUserDestiny, trustValue);
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
