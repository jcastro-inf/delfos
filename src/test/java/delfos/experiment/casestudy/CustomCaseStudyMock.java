package delfos.experiment.casestudy;

import delfos.common.parameters.ParameterOwnerType;
import delfos.experiment.ExperimentAdapter;
import delfos.experiment.SeedHolder;
import org.jdom2.Element;

public class CustomCaseStudyMock extends ExperimentAdapter{

    boolean finished = false;
    double result = Double.NaN;

    public CustomCaseStudyMock(){
        addParameter(SEED);
        addParameter(CaseStudy.NUM_EXECUTIONS);
        addParameter(ExperimentAdapter.RESULTS_DIRECTORY);
    }
    @Override
    public boolean isFinished() {
        return finished;
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
        finished = true;
        result = 1.0;
    }

    @Override
    public void setSeedValue(long seedValue) {
        setParameterValue(SEED,seedValue);
    }

    @Override
    public long getSeedValue() {
        return ((Number) getParameterValue(SEED)).longValue();
    }

    @Override
    public void addResultsToElement(Element experimentElement) {
        if(!isFinished()) {
            return;
        }

        Element resultsElement = new Element("Results");
        resultsElement.setAttribute("value", Double.toString(result));

        experimentElement.addContent(resultsElement);
    }

    @Override
    public void setResultsFromElement(Element experimentElement) {

        if(experimentElement.getChild("Results") == null){
            return;
        }
        Element resultsElement = experimentElement.getChild("Results");

        String valueString = resultsElement.getAttributeValue("value");
        result = Double.parseDouble(valueString);
    }

}
