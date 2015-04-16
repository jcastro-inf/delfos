package delfos.dataset.basic.trust;

/**
 *
 *
* @author Jorge Castro Gallardo
 *
 * @version 18-dic-2013
 */
public class TrustStatement {

    int idUserSource;
    int idUserDestiny;
    double trustValue;

    public TrustStatement(int idUserSource, int idUserDestiny, double trustValue) {
        this.idUserSource = idUserSource;
        this.idUserDestiny = idUserDestiny;
        this.trustValue = trustValue;
    }

    public int getIdUserDestiny() {
        return idUserDestiny;
    }

    public int getIdUserSource() {
        return idUserSource;
    }

    public double getTrustValue() {
        return trustValue;
    }
}
