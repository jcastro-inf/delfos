package delfos.main.managers.experiment.join.xml;

import delfos.group.casestudy.GroupCaseStudy;
import delfos.group.io.excel.casestudy.GroupCaseStudyExcel;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class GroupCaseStudyResult {

    public static final Comparator<GroupCaseStudyResult> dataValidationComparator = (GroupCaseStudyResult o1, GroupCaseStudyResult o2) -> {

        int datasetCompare = o1.groupCaseStudy.getDatasetLoader().compareTo(o2.groupCaseStudy.getDatasetLoader());
        if (datasetCompare != 0) {
            return datasetCompare;
        }

        int groupFormationCompare = o1.groupCaseStudy.getGroupFormationTechnique().compareTo(o2.groupCaseStudy.getGroupFormationTechnique());
        if (groupFormationCompare != 0) {
            return groupFormationCompare;
        }

        int groupValidationTechniqueCompare = o1.groupCaseStudy.getGroupValidationTechnique().compareTo(o2.groupCaseStudy.getGroupValidationTechnique());
        if (groupValidationTechniqueCompare != 0) {
            return groupValidationTechniqueCompare;
        }

        int groupPredictionProtocolCompare = o1.groupCaseStudy.getGroupPredictionProtocol().compareTo(o2.groupCaseStudy.getGroupPredictionProtocol());
        if (groupPredictionProtocolCompare != 0) {
            return groupPredictionProtocolCompare;
        }
        return 0;
    };

    public static final Comparator<GroupCaseStudyResult> techniqueComparator = (GroupCaseStudyResult o1, GroupCaseStudyResult o2) -> {

        int groupRecommenderSystemCompare = o1.groupCaseStudy.getGroupRecommenderSystem().compareTo(o2.groupCaseStudy.getGroupRecommenderSystem());
        return groupRecommenderSystemCompare;

    };

    private final int caseStudyHash;
    private final int techniqueHash;
    private final int dataValidationHash;
    private final int numExecutions;
    private final String alias;

    private final Map<String, Object> dataValidationParameters;
    private final Map<String, Object> techniqueParameters;
    private final Map<String, Number> evaluationMeasuresValues;
    private final long seed;
    private final GroupCaseStudy groupCaseStudy;

    /**
     *
     * @param groupCaseStudy
     */
    public GroupCaseStudyResult(GroupCaseStudy groupCaseStudy) {

        this.groupCaseStudy = groupCaseStudy;

        caseStudyHash = groupCaseStudy.hashCode();
        techniqueHash = groupCaseStudy.hashTechnique();
        dataValidationHash = groupCaseStudy.hashDataValidation();
        numExecutions = groupCaseStudy.getNumExecutions();
        alias = groupCaseStudy.getAlias();
        seed = groupCaseStudy.getSeedValue();

        dataValidationParameters = GroupCaseStudyExcel.extractDataValidationParameters(groupCaseStudy);

        techniqueParameters = GroupCaseStudyExcel.extractTechniqueParameters(groupCaseStudy);

        evaluationMeasuresValues = GroupCaseStudyExcel.extractEvaluationMeasuresValues(groupCaseStudy);
    }

    public Set<String> getDefinedDataValidationParameters() {
        return new TreeSet<>(dataValidationParameters.keySet());
    }

    public Set<String> getDefinedTechniqueParameters() {
        return new TreeSet<>(techniqueParameters.keySet());
    }

    public Set<String> getDefinedEvaluationMeasures() {
        return new TreeSet<>(evaluationMeasuresValues.keySet());
    }

    public String getGroupCaseStudyAlias() {
        return alias;
    }

    public int getNumExecutions() {
        return numExecutions;
    }

    public long getGroupCaseStudySeed() {
        return seed;
    }

    public GroupCaseStudy getGroupCaseStudy() {
        return groupCaseStudy;
    }

    public Object getDataValidationParameterValue(String dataValidationParameter) {
        return dataValidationParameters.get(dataValidationParameter);
    }

    public Object getTechniqueParameterValue(String techniqueParameter) {
        return techniqueParameters.get(techniqueParameter);
    }

    public Object getEvaluationMeasureValue(String evaluationMeasure) {
        return evaluationMeasuresValues.get(evaluationMeasure);
    }

}
