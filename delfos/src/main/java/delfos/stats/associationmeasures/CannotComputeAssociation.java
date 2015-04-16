package delfos.stats.associationmeasures;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 15-oct-2013
 */
public class CannotComputeAssociation extends Exception {

    private final static long serialVersionUID = 1L;

    public CannotComputeAssociation(String message) {
        super(message);
    }
}
