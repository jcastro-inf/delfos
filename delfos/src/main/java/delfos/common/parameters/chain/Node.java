package delfos.common.parameters.chain;

import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;

/**
 *
 * @author jcastro
 */
class Node {

    private final Parameter parameter;
    private final ParameterOwner parameterOwner;

    public Node(Parameter parameter, ParameterOwner parameterOwner) {
        this.parameter = parameter;
        this.parameterOwner = parameterOwner;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public ParameterOwner getParameterOwner() {
        return parameterOwner;
    }

    boolean isCompatibleWith(Node firstChainNode) {
        boolean parameterOwnerAreSameClass = parameterOwner.getClass().equals(firstChainNode.parameterOwner.getClass());

        if (!parameterOwnerAreSameClass) {
            return false;
        }

        boolean nodeParameterAreSame = parameter.equals(firstChainNode.parameter);

        return nodeParameterAreSame;
    }

}
