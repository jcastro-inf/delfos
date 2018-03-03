package delfos.experiment;

import delfos.common.parameters.ParameterOwner;

public interface Experiment extends ParameterOwner{

    public void execute();
}
