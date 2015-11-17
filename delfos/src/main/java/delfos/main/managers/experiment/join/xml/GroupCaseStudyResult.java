package delfos.main.managers.experiment.join.xml;

import delfos.common.decimalnumbers.NumberRounder;
import delfos.group.casestudy.GroupCaseStudy;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupMeasureResult;
import delfos.group.results.groupevaluationmeasures.precisionrecall.PRSpaceGroups;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import jxl.write.WritableSheet;
import jxl.write.WriteException;

public class GroupCaseStudyResult {

    int caseStudyHash;
    int techniqueHash;
    int dataValidationHash;

    List<String> dataValidationParametersOrder;
    List<String> techniqueParametersOrder;
    List<String> evaluationMeasuresOrder;

    Map<String, String> dataValidationParameters;
    Map<String, String> techniqueParameters;
    Map<String, Number> evaluationMeasuresValues;

    public GroupCaseStudyResult(GroupCaseStudy groupCaseStudy) {

        dataValidationParametersOrder = extractDataValidationParametersOrder(groupCaseStudy);
        dataValidationParameters = extractDataValidationParameters(groupCaseStudy);

        techniqueParametersOrder = extractTechniqueParametersOrder(groupCaseStudy);
        techniqueParameters = extractTechniqueParameters(groupCaseStudy);

        evaluationMeasuresOrder = extractEvaluationMeasuresOrder(groupCaseStudy);
        evaluationMeasuresValues = extractEvaluationMeasuresValues(groupCaseStudy);

    }

    public void setDataValidationParametersOrder(List<String> orderedDataValidationParameters) {
        this.dataValidationParametersOrder = orderedDataValidationParameters;
    }

    public void setOrderedDataValidationParameters(List<String> orderedDataValidationParameters) {
        this.dataValidationParametersOrder = orderedDataValidationParameters;
    }

    public void setOrderedEvaluationMeasures(List<String> orderedEvaluationMeasures) {
        this.evaluationMeasuresOrder = orderedEvaluationMeasures;
    }

    public void setOrderedTechniqueParameters(List<String> orderedTechniqueParameters) {
        this.techniqueParametersOrder = orderedTechniqueParameters;
    }

    public Set<String> getDefinedDataValidationParameters() {
        return new TreeSet<>(dataValidationParameters.keySet());
    }

    public Set<String> getDefinedTechniqueParameters() {
        return new TreeSet<>(techniqueParameters.keySet());
    }

    public Set<String> getDefinedEvaluationMeasuresValues() {
        return new TreeSet<>(evaluationMeasuresValues.keySet());
    }

    private static void createAggregateResultsSheet(GroupCaseStudy caseStudyGroup, WritableSheet sheet) throws WriteException {
        int row = 0;

        PRSpaceGroups pRSpaceGroups = null;
        Map<String, Integer> indexOfMeasures = new TreeMap<>();
        Map<String, GroupEvaluationMeasure> metricsByName = new TreeMap<>();
        {
            int i = 0;
            for (GroupEvaluationMeasure groupEvaluationMeasure : caseStudyGroup.getEvaluationMeasures()) {
                indexOfMeasures.put(groupEvaluationMeasure.getName(), i++);

                metricsByName.put(groupEvaluationMeasure.getName(), groupEvaluationMeasure);

                if (groupEvaluationMeasure instanceof PRSpaceGroups) {
                    pRSpaceGroups = (PRSpaceGroups) groupEvaluationMeasure;
                    for (int listSize = 1; listSize <= maxListSize; listSize++) {
                        indexOfMeasures.put("Precision@" + listSize, i++);
                    }
                }
            }
            indexOfMeasures.put("BuildTime", i++);
            indexOfMeasures.put("GroupModelBuildTime", i++);
            indexOfMeasures.put("RecommendationTime", i++);
        }

        for (Map.Entry<String, Integer> entry : indexOfMeasures.entrySet()) {
            String name = entry.getKey();
            int column = entry.getValue();
            addTitleText(sheet, column, row, name);
        }

        row++;

        //Ahora los valores agregados de cada metrica.
        for (Map.Entry<String, Integer> entry : indexOfMeasures.entrySet()) {
            String name = entry.getKey();
            int column = entry.getValue();

            final double value;

            if (name.equals("BuildTime")) {
                value = caseStudyGroup.getAggregateBuildTime();
            } else {
                if (name.equals("GroupModelBuildTime")) {
                    value = caseStudyGroup.getAggregateGroupBuildTime();
                } else {
                    if (name.equals("RecommendationTime")) {
                        value = caseStudyGroup.getAggregateRecommendationTime();
                    } else {
                        if (name.startsWith("Precision@")) {
                            GroupMeasureResult measureResult = caseStudyGroup.getAggregateMeasureResult(pRSpaceGroups);
                            Map<String, Double> detailedResult = (Map<String, Double>) measureResult.getDetailedResult();

                            Double get = detailedResult.get(name);

                            if (get == null) {
                                //No se llegan a recomendar tantos productos.
                                value = Double.NaN;
                            } else {
                                value = get;
                            }
                        } else {
                            //Es una medida cualquiera.
                            GroupEvaluationMeasure groupEvaluationMeasure = metricsByName.get(name);
                            value = caseStudyGroup.getAggregateMeasureResult(groupEvaluationMeasure).getValue();
                        }
                    }
                }
            }

            if (!Double.isNaN(value)) {
                double decimalTrimmedValue = NumberRounder.round(value, 5);
                addNumber(sheet, column, row, decimalTrimmedValue);
            } else {
                addText(sheet, column, row, "");
            }
            double decimalTrimmedValue = NumberRounder.round(value, 5);

            addNumber(sheet, column, row, decimalTrimmedValue);
        }

    }
}
