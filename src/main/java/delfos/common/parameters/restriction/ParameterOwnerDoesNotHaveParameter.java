package delfos.common.parameters.restriction;

import delfos.common.parameters.ParameterOwner;

/**
 * Created by jcastro on 28/06/17.
 */
public class ParameterOwnerDoesNotHaveParameter extends RuntimeException{
    private ParameterOwner parameterOwner;
    private String parameterName;

    public ParameterOwnerDoesNotHaveParameter(ParameterOwner parameterOwner, String parameterName){
        super("ParameterOwner '"+parameterOwner.getName()+"' does not have the parameter named '"+parameterName+"'.");

        this.parameterOwner = parameterOwner;
        this.parameterName = parameterName;
    }
}
