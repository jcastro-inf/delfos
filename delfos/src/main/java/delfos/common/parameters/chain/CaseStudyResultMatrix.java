package delfos.common.parameters.chain;

import delfos.common.Global;
import delfos.common.StringsOrderings;
import delfos.common.parameters.ParameterOwner;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Stores in a tabular form the results of the groupCaseStudy for a given
 * evaluationMeasure.
 *
 * @author Jorge Castro Gallardo
 *
 */
public class CaseStudyResultMatrix {

    private final String evaluationMeasure;
    private final List<ParameterChain> rowsChains;
    private final List<ParameterChain> columnsChains;

    private final Map<String, Map<String, Number>> tabulatedValues = new TreeMap<>();

    public CaseStudyResultMatrix(List<ParameterChain> rowsChains, List<ParameterChain> columnsChains, String evaluationMeasure) {
        this.rowsChains = Collections.unmodifiableList(rowsChains);
        this.columnsChains = Collections.unmodifiableList(columnsChains);
        this.evaluationMeasure = evaluationMeasure;
    }

    public void addValue(ParameterOwner parameterOwner, Number value) {
        String row = getRowIdentifier(parameterOwner);
        String column = getColumnIdentifier(parameterOwner);

        if (!tabulatedValues.containsKey(row)) {
            tabulatedValues.put(row, new TreeMap<>());
        }

        if (tabulatedValues.get(row).containsKey(column)) {
            //throw new IllegalArgumentException("The value was already set!");
            Global.showWarning("The value [" + row + "," + column + "] was already set!");
        }
        tabulatedValues.get(row).put(column, value);
    }

    private String getRowIdentifier(ParameterOwner parameterOwner) {
        StringBuilder str = new StringBuilder();

        for (ParameterChain chain : rowsChains) {
            Object value = chain.getValueOn(parameterOwner);
            str.append(value.toString()).append("_");
        }
        str.delete(str.length() - 1, str.length());

        return str.toString();
    }

    private String getColumnIdentifier(ParameterOwner parameterOwner) {
        StringBuilder str = new StringBuilder();

        for (ParameterChain chain : columnsChains) {
            Object value = chain.getValueOn(parameterOwner);
            str.append(value.toString()).append("_");
        }
        str.delete(str.length() - 1, str.length());

        return str.toString();
    }

    public List<String> getColumnNames() {

        Set<String> columnNames = new TreeSet<>();

        for (String row : tabulatedValues.keySet()) {
            columnNames.addAll(tabulatedValues.get(row).keySet());
        }

        List<String> columnNamesSorted = columnNames.stream().collect(Collectors.toList());

        columnNamesSorted.sort(StringsOrderings.getNaturalComparator());

        return columnNamesSorted;
    }

    public List<String> getRowNames() {

        Set<String> rowNames = new TreeSet<>(tabulatedValues.keySet());

        List<String> rowNamesSorted = rowNames.stream().collect(Collectors.toList());

        rowNamesSorted.sort(StringsOrderings.getNaturalComparator());

        return rowNamesSorted;

    }

    public String print() {
        StringBuilder str = new StringBuilder();
        final String NEW_FIELD = "\t";
        final String NEW_RECORD = "\n";

        str.append(evaluationMeasure).append(NEW_FIELD);
        for (String columnName : getColumnNames()) {
            str.append(columnName).append(NEW_FIELD);
        }
        str.replace(str.length() - 1, str.length(), NEW_RECORD);

        for (String rowName : getRowNames()) {
            str.append(rowName).append(NEW_FIELD);
            for (String columnName : getColumnNames()) {

                Number value = tabulatedValues.get(rowName).get(columnName);

                str.append(value.toString()).append(NEW_FIELD);
            }
            str.replace(str.length() - 1, str.length(), NEW_RECORD);
        }

        return str.toString();
    }

    public Number getValue(String rowName, String columnName) {
        Number value = tabulatedValues.get(rowName).get(columnName);
        return value;
    }
}
