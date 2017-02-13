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
package delfos.experiment.casestudy;

import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.experiment.validation.predictionprotocol.PredictionProtocol;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.factories.EvaluationMeasuresFactory;
import delfos.io.excel.casestudy.CaseStudyExcel;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.GenericRecommenderSystem;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Almacena los valores y resultados de un caso de estudio de sistemas de recomendación tradicionales ya ejecutado.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 19-Noviembre-2013
 */
public class CaseStudyResults {

    public static final Comparator<CaseStudyResults> dataValidationComparator = (CaseStudyResults o1, CaseStudyResults o2) -> {

        int datasetCompare = o1.caseStudy.getDatasetLoader().compareTo(o2.caseStudy.getDatasetLoader());
        if (datasetCompare != 0) {
            return datasetCompare;
        }

        int validationTechniqueCompare = o1.caseStudy.getValidationTechnique().compareTo(o2.caseStudy.getValidationTechnique());
        if (validationTechniqueCompare != 0) {
            return validationTechniqueCompare;
        }

        return 0;
    };

    public static final Comparator<CaseStudyResults> techniqueComparator = (CaseStudyResults o1, CaseStudyResults o2) -> {

        int groupRecommenderSystemCompare = o1.caseStudy.getRecommenderSystem().compareTo(o2.caseStudy.getRecommenderSystem());
        return groupRecommenderSystemCompare;

    };

    private final int caseStudyHash;
    private final int techniqueHash;
    private final int dataValidationHash;
    private final int numExecutions;

    private final Map<String, Object> dataValidationParameters;
    private final Map<String, Object> techniqueParameters;
    private final Map<String, Number> evaluationMeasuresValues;
    private final long seed;
    private final CaseStudy caseStudy;

    public String caseStudyAlias;

    /**
     *
     * @param caseStudy
     */
    public CaseStudyResults(CaseStudy caseStudy) {

        this.caseStudy = caseStudy;

        caseStudyHash = caseStudy.hashCode();
        techniqueHash = caseStudy.hashTechnique();
        dataValidationHash = caseStudy.hashDataValidation();
        numExecutions = caseStudy.getNumExecutions();
        caseStudyAlias = caseStudy.getAlias();
        seed = caseStudy.getSeedValue();

        dataValidationParameters = CaseStudyExcel.extractDataValidationParameters(caseStudy);

        techniqueParameters = CaseStudyExcel.extractTechniqueParameters(caseStudy);

        evaluationMeasuresValues = CaseStudyExcel.extractEvaluationMeasuresValues(caseStudy);
    }

    public GenericRecommenderSystem<Object> getRecommenderSystem() {
        return (GenericRecommenderSystem<Object>) caseStudy.getRecommenderSystem();
    }

    public ValidationTechnique getValidationTechnique() {
        return caseStudy.getValidationTechnique();
    }

    public PredictionProtocol getPredictionProtocol() {
        return caseStudy.getPredictionProtocol();
    }

    public Collection<EvaluationMeasure> getEvaluationMeasures() {
        return EvaluationMeasuresFactory.getInstance().getAllClasses();
    }

    public DatasetLoader<? extends Rating> getDatasetLoader() {
        return caseStudy.getDatasetLoader();
    }

    public Map<EvaluationMeasure, Double> getEvaluationMeasuresResults() {

        return caseStudy.getEvaluationMeasures().parallelStream().collect(Collectors.toMap(
                Function.identity(),
                evaluationMeasure -> caseStudy.getMeasureResult(evaluationMeasure).getValue()
        ));

    }

    public long getBuildTime() {
        return 0;
    }

    public long getRecommendationTime() {
        return 0;
    }

    public String getCaseStudyAlias() {
        return caseStudyAlias;
    }

    public void setCaseStudyAlias(String caseStudyAlias) {
        this.caseStudyAlias = caseStudyAlias;
    }

    public CaseStudy getCaseStudy() {
        return caseStudy;
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

    public Object getDataValidationParameterValue(String dataValidationParameter) {
        return dataValidationParameters.get(dataValidationParameter);
    }

    public Object getTechniqueParameterValue(String techniqueParameter) {
        return techniqueParameters.get(techniqueParameter);
    }

    public Number getEvaluationMeasureValue(String evaluationMeasure) {
        return evaluationMeasuresValues.get(evaluationMeasure);
    }

    public int getNumExecutions() {
        return caseStudy.getNumExecutions();
    }
}
