package delfos.common.parameters;

/**
 * Created by jcastro on 28/06/17.
 */
public class ParameterOwnerNotFoundException extends RuntimeException {

    private String parameterOwnerName;

    public ParameterOwnerNotFoundException(String parameterOwnerName){
        super("ParameterOwner named '"+parameterOwnerName+"' not found.");

        this.parameterOwnerName = parameterOwnerName;
    }

    public ParameterOwnerNotFoundException(String message, Exception ex){
        super(message,ex);
    }
}
