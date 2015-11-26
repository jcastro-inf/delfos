package delfos.common.parameters.chain;

import delfos.common.parameters.ParameterOwner;

/**
 *
 * @author jcastro
 */
public class Root {

    private final ParameterOwner parameterOwner;

    public Root(ParameterOwner parameterOwner) {
        this.parameterOwner = parameterOwner;
    }

    public ParameterOwner getParameterOwner() {
        return parameterOwner;
    }

    public boolean isCompatibleWith(Root root) {
        return parameterOwner.getClass().equals(root.getParameterOwner().getClass());
    }
}
