package delfos.view.resultanalysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;
import org.jdom2.JDOMException;
import delfos.experiment.casestudy.CaseStudyResults;
import delfos.io.xml.casestudy.CaseStudyXML;
import delfos.io.xml.casestudy.ParameterOwnerParameterNamesXML;
import delfos.io.xml.dataset.DatasetLoaderXML;
import delfos.io.xml.predictionprotocol.PredictionProtocolXML;
import delfos.io.xml.rs.RecommenderSystemXML;
import delfos.io.xml.validationtechnique.ValidationTechniqueXML;
import delfos.results.evaluationmeasures.EvaluationMeasure;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 19-Noviembre-2013
 */
public class OutputTableModel extends AbstractTableModel {

    public void updateOutput(File[] files) {
        List<String> newColumnNames = new ArrayList<>();

        Set<EvaluationMeasure> evaluationMeasures = new TreeSet<>();

        List<CaseStudyResults> results = new ArrayList<>();

        for (File file : files) {
            try {
                CaseStudyResults loadCaseResults = CaseStudyXML.loadCaseResults(file);
                results.add(loadCaseResults);
            } catch (JDOMException | IOException ex) {
                Logger.getLogger(OutputTableModel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //Columnas
        {
            //Parametros del SR
            int i = 0;
            for (CaseStudyResults caseResults : results) {
                List<String> parameterNames = ParameterOwnerParameterNamesXML.getParameterNames(caseResults.getRecommenderSystem());
                for (String parameterName : parameterNames) {
                    String parameterNameExt = RecommenderSystemXML.ELEMENT_NAME + "." + parameterName;
                    if (!newColumnNames.contains(parameterNameExt)) {
                        newColumnNames.add(parameterNameExt);
                    }
                }

                i++;
            }
            //DatasetLoader
            i = 0;
            for (CaseStudyResults caseResults : results) {
                List<String> parameterNames = ParameterOwnerParameterNamesXML.getParameterNames(caseResults.getDatasetLoader());
                for (String parameterName : parameterNames) {
                    String parameterNameExt = DatasetLoaderXML.ELEMENT_NAME + "." + parameterName;
                    if (!newColumnNames.contains(parameterNameExt)) {
                        newColumnNames.add(parameterNameExt);
                    }
                }

                i++;
            }

            //ValidationTechnique
            i = 0;
            for (CaseStudyResults caseResults : results) {
                List<String> parameterNames = ParameterOwnerParameterNamesXML.getParameterNames(caseResults.getValidationTechnique());
                for (String parameterName : parameterNames) {
                    String parameterNameExt = ValidationTechniqueXML.ELEMENT_NAME + "." + parameterName;
                    if (!newColumnNames.contains(parameterNameExt)) {
                        newColumnNames.add(parameterNameExt);
                    }
                }

                i++;
            }
            //PredictionProtocol
            i = 0;
            for (CaseStudyResults caseResults : results) {
                List<String> parameterNames = ParameterOwnerParameterNamesXML.getParameterNames(caseResults.getPredictionProtocol());
                for (String parameterName : parameterNames) {
                    String parameterNameExt = PredictionProtocolXML.ELEMENT_NAME + "." + parameterName;
                    if (!newColumnNames.contains(parameterNameExt)) {
                        newColumnNames.add(parameterNameExt);
                    }
                }

                i++;
            }
            //EvaluationMeasures
            for (CaseStudyResults caseResults : results) {
                evaluationMeasures.addAll(caseResults.getEvaluationMeasuresResults().keySet());
            }
        }

        for (EvaluationMeasure evaluationMeasure : evaluationMeasures) {
            newColumnNames.add(evaluationMeasure.getName());
        }
        newColumnNames.add("Build_time");
        newColumnNames.add("Recommendation_time");

        //Valores
        values = new String[results.size()][newColumnNames.size()];
        {
            //Parametros del SR
            int i = 0;
            for (CaseStudyResults caseResults : results) {
                int j = 0;
                for (String column : newColumnNames) {

                    if (column.startsWith(RecommenderSystemXML.ELEMENT_NAME)) {
                        String parameterName = column.replace(RecommenderSystemXML.ELEMENT_NAME, "");

                        //Elimino el punto
                        parameterName = parameterName.substring(1);

                        Object parameterValueByName = ParameterOwnerParameterNamesXML.getParameterValueByName(caseResults.getRecommenderSystem(), parameterName);
                        if (parameterValueByName == null) {
                            values[i][j] = "";
                        } else {
                            values[i][j] = parameterValueByName.toString();
                        }
                    }

                    if (column.startsWith(DatasetLoaderXML.ELEMENT_NAME)) {
                        String parameterName = column.replace(DatasetLoaderXML.ELEMENT_NAME, "");

                        //Elimino el punto
                        parameterName = parameterName.substring(1);

                        Object parameterValueByName = ParameterOwnerParameterNamesXML.getParameterValueByName(caseResults.getDatasetLoader(), parameterName);
                        if (parameterValueByName == null) {
                            values[i][j] = "";
                        } else {
                            values[i][j] = parameterValueByName.toString();
                        }
                    }
                    if (column.startsWith(ValidationTechniqueXML.ELEMENT_NAME)) {
                        String parameterName = column.replace(ValidationTechniqueXML.ELEMENT_NAME, "");

                        //Elimino el punto
                        parameterName = parameterName.substring(1);

                        Object parameterValueByName = ParameterOwnerParameterNamesXML.getParameterValueByName(caseResults.getValidationTechnique(), parameterName);
                        if (parameterValueByName == null) {
                            values[i][j] = "";
                        } else {
                            values[i][j] = parameterValueByName.toString();
                        }
                    }
                    if (column.startsWith(PredictionProtocolXML.ELEMENT_NAME)) {
                        String parameterName = column.replace(PredictionProtocolXML.ELEMENT_NAME, "");

                        //Elimino el punto
                        parameterName = parameterName.substring(1);

                        Object parameterValueByName = ParameterOwnerParameterNamesXML.getParameterValueByName(caseResults.getPredictionProtocol(), parameterName);
                        if (parameterValueByName == null) {
                            values[i][j] = "";
                        } else {
                            values[i][j] = parameterValueByName.toString();
                        }
                    }

                    if (values[i][j] == null) {
                        for (EvaluationMeasure evaluationMeasure : evaluationMeasures) {
                            if (evaluationMeasure.getName().equals(column)) {
                                values[i][j] = caseResults.getEvaluationMeasuresResults().get(evaluationMeasure).toString();
                            }
                        }

                        if (values[i][j] == null) {
                            if (column.equals("Build_time")) {
                                values[i][j] = Long.toString(caseResults.getBuildTime());
                            }
                            if (column.equals("Recommendation_time")) {
                                values[i][j] = Long.toString(caseResults.getRecommendationTime());
                            }

                        }

                    }

                    j++;

                }

                i++;
            }
        }

        this.columnNames = newColumnNames.toArray(new String[0]);
        fireTableStructureChanged();
        fireTableDataChanged();
    }

    private String[] columnNames = {"Column"};
    private String[][] values;

    public String[][] getCSVData() {
        String[][] ret = new String[values.length + 1][columnNames.length];

        for (int i = 0; i < columnNames.length; i++) {
            String columnName = columnNames[i];
            ret[0][i] = columnName;
        }
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < columnNames.length; j++) {
                String value = values[i][j];
                ret[i + 1][j] = value;
            }
        }

        return ret;

    }

    public OutputTableModel() {
        values = new String[2][1];

        values[0][0] = "v1";
        values[1][0] = "v2";

    }

    @Override
    public int getRowCount() {
        return values.length;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return values[rowIndex][columnIndex];
    }
}
