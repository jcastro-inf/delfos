package delfos.common.parameters.chain;

import delfos.common.parameters.ParameterOwner;

/**
 *
 * @author jcastro
 */
public class Root {

    private final String rootName;
    private final ParameterOwner parameterOwner;

    public Root(String rootName, ParameterOwner parameterOwner) {
        this.rootName = rootName;
        this.parameterOwner = parameterOwner;
    }

    public String getRootName() {
        return rootName;
    }

    public ParameterOwner getParameterOwner() {
        return parameterOwner;
    }

}
