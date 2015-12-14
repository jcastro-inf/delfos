package delfos.common.parallelwork;

import java.io.Serializable;

/**
 *
 * @version 20-may-2014
 * @author Jorge Castro Gallardo
 *
 * @deprecated The parallel execution should be done using
 * {@link java.util.function.Function}, by iterating over the list of the
 * objects with the data of the task. Also the objects that perform the
 * execution should be refactored to implement
 * {@link java.util.function.Function} and execute the code over the data
 * object.
 */
public abstract class Task implements Serializable {

    private static final long serialVersionUID = 42L;

    @Override
    public abstract String toString();
}
