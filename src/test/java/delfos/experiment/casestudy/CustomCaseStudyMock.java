package delfos.experiment.casestudy;

import delfos.common.parameters.ParameterOwnerType;
import delfos.experiment.ExperimentAdapter;
import delfos.experiment.SeedHolder;

public class CustomCaseStudyMock extends ExperimentAdapter{

    public CustomCaseStudyMock(){
        addParameter(SEED);
        addParameter(CaseStudy.NUM_EXECUTIONS);
        addParameter(ExperimentAdapter.RESULTS_DIRECTORY);
    }
    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public boolean hasErrors() {
        return false;
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.CASE_STUDY;
    }

    @Override
    public void execute() {

    }

    @Override
    public void setSeedValue(long seedValue) {
        setParameterValue(SEED,seedValue);
    }

    @Override
    public long getSeedValue() {
        return ((Number) getParameterValue(SEED)).longValue();
    }
}
