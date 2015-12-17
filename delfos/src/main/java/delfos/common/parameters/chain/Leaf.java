package delfos.common.parameters.chain;

import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import java.util.Objects;

/**
 *
 * @author jcastro
 */
class Leaf {

    private final Parameter parameter;
    private final ParameterOwner parameterOwner;
    private final Object parameterValue;

    public Leaf(ParameterOwner parameterOwner, Parameter parameter, Object parameterValue) {
        this.parameter = parameter;
        this.parameterOwner = parameterOwner;
        this.parameterValue = parameterValue;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public Object getParameterValue() {
        return parameterValue;
    }

    public ParameterOwner getParameterOwner() {
        return parameterOwner;
    }

    boolean isCompatibleWith(Leaf leaf) {
        boolean parameterAreSame = parameter.equals(leaf.parameter);

        if (!parameterAreSame) {
            return false;
        }

        boolean parameterOwnerAreSameClass = parameterOwner.getClass().equals(leaf.parameterOwner.getClass());

        return parameterOwnerAreSameClass;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Leaf) {
            Leaf leaf = (Leaf) obj;

            if (!this.parameterOwner.getClass().equals(leaf.parameterOwner.getClass())) {
                return false;
            }

            if (this.parameterValue instanceof ParameterOwner && leaf.parameterValue instanceof ParameterOwner) {
                ParameterOwner parameterValueParameterOwner = (ParameterOwner) this.parameterValue;
                ParameterOwner parameterValueParameterOwner2 = (ParameterOwner) leaf.parameterValue;

                return parameterValueParameterOwner.equals(parameterValueParameterOwner2);
            } else {
                boolean valuesAreSame = this.parameterValue.equals(leaf.parameterValue);
                return valuesAreSame;
            }
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
