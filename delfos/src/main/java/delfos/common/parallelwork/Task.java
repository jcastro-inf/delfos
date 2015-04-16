package delfos.common.parallelwork;

import java.io.Serializable;

/**
 *
 * @version 20-may-2014
* @author Jorge Castro Gallardo
 */
public abstract class Task implements Serializable {

    private static final long serialVersionUID = 42L;

    @Override
    public abstract String toString();
}
