package delfos.group.casestudy.definedcases.jrs2014;

/**
* @author Jorge Castro Gallardo
 *
 */
public class CouldNotComputeTrust extends Exception {

    private static final long serialVersionUID = 1L;

    public CouldNotComputeTrust(String msg) {
        super(msg);
    }

    public CouldNotComputeTrust(Throwable cause) {
        super(cause);
    }
}
