package delfos.common.parameters.chain;

import delfos.common.parameters.Parameter;

/**
 *
 * @author jcastro
 */
class Leaf {

    private final Parameter parameter;
    private final Object parameterValue;

    public Leaf(Parameter parameter, Object parameterValue) {
        this.parameter = parameter;
        this.parameterValue = parameterValue;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public Object getParameterValue() {
        return parameterValue;
    }

}
