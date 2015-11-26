package delfos.common.parameters.chain;

import delfos.common.parameters.Parameter;
import java.util.Objects;

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

    boolean isCompatibleWith(Leaf leaf) {
        boolean parameterAreSame = parameter.equals(leaf.parameter);

        return parameterAreSame;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Leaf) {
            Leaf leaf = (Leaf) obj;

            boolean valuesAreSame = this.parameterValue.equals(leaf.parameterValue);

            return valuesAreSame;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.parameter);
        hash = 67 * hash + Objects.hashCode(this.parameterValue);
        return hash;
    }

}
