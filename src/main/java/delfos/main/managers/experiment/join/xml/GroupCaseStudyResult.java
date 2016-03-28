/*
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package delfos.main.managers.experiment.join.xml;

import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.io.excel.casestudy.GroupCaseStudyExcel;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

        int validationTechniqueCompare = o1.groupCaseStudy.getValidationTechnique().compareTo(o2.groupCaseStudy.getValidationTechnique());
        if (validationTechniqueCompare != 0) {
            return validationTechniqueCompare;
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
        return dataValidationParameters.keySet().parallelStream().collect(Collectors.toSet());
    }

    public Set<String> getDefinedTechniqueParameters() {
        return techniqueParameters.keySet().parallelStream().collect(Collectors.toSet());
    }

    public Set<String> getDefinedEvaluationMeasures() {
        return evaluationMeasuresValues.keySet().parallelStream().collect(Collectors.toSet());
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

    public Number getEvaluationMeasureValue(String evaluationMeasure) {
        return evaluationMeasuresValues.get(evaluationMeasure);
    }

}
