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
package delfos.common.parameters.chain;

import delfos.common.StringsOrderings;
import delfos.common.parameters.ParameterOwner;
import delfos.main.managers.experiment.join.xml.GroupCaseStudyResult;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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

    private final Map<String, Map<String, Number>> tabulatedValues = new TreeMap<>(StringsOrderings.getNaturalComparator());
    private final Set<String> rowNames;
    private final Set<String> columnNames;

    public CaseStudyResultMatrix(List<ParameterChain> rowsChains, List<ParameterChain> columnsChains, String evaluationMeasure) {
        validateParameters(rowsChains, columnsChains, evaluationMeasure);

        this.rowsChains = Collections.unmodifiableList(rowsChains);
        this.columnsChains = Collections.unmodifiableList(columnsChains);
        this.evaluationMeasure = evaluationMeasure;

        rowNames = new TreeSet<>(StringsOrderings.getNaturalComparator());
        columnNames = new TreeSet<>(StringsOrderings.getNaturalComparator());
    }

    public void addValue(ParameterOwner parameterOwner, Number value) {
        validateParameters(parameterOwner, value);

        String row = getRowIdentifier(parameterOwner);
        String column = getColumnIdentifier(parameterOwner);

        if (!tabulatedValues.containsKey(row)) {
            tabulatedValues.put(row, new TreeMap<>(StringsOrderings.getNaturalComparator()));
        }

        if (tabulatedValues.get(row).containsKey(column)) {
            tabulatedValues.get(row).remove(column);
        }
        tabulatedValues.get(row).put(column, value);
    }

    public Number getValue(ParameterOwner parameterOwner) {
        validateParameterOwner(parameterOwner);

        String row = getRowIdentifier(parameterOwner);
        String column = getColumnIdentifier(parameterOwner);

        if (!tabulatedValues.containsKey(row)) {
            return null;
        } else if (!tabulatedValues.get(row).containsKey(column)) {
            return null;
        } else {
            return tabulatedValues.get(row).get(column);
        }
    }

    public String getRow(ParameterOwner parameterOwner) {
        return getRowIdentifier(parameterOwner);
    }

    public String getColumn(ParameterOwner parameterOwner) {
        return getColumnIdentifier(parameterOwner);
    }

    private String getRowIdentifier(ParameterOwner parameterOwner) {
        StringBuilder str = new StringBuilder();

        for (ParameterChain chain : rowsChains) {

            if (!chain.isApplicableTo(parameterOwner)) {
                continue;
            }

            Object value = chain.getValueOn(parameterOwner);
            String parameterName = chain.getLeaf().getParameter().getName();
            str.append(parameterName).append("=").append(value.toString()).append("_");
        }
        str.delete(str.length() - 1, str.length());

        final String rowName = str.toString();

        if (!rowNames.contains(rowName)) {
            rowNames.add(rowName);
        }

        return rowName;
    }

    private String getColumnIdentifier(ParameterOwner parameterOwner) {
        StringBuilder str = new StringBuilder();

        for (ParameterChain chain : columnsChains) {

            if (!chain.isApplicableTo(parameterOwner)) {
                continue;
            }

            Object value = chain.getValueOn(parameterOwner);
            String parameterName = chain.getLeaf().getParameter().getName();
            str.append(parameterName).append("=").append(value.toString()).append("_");
        }
        str.delete(str.length() - 1, str.length());

        final String columnName = str.toString();
        if (!columnNames.contains(columnName)) {
            columnNames.add(columnName);
        }

        return columnName;

    }

    public Set<String> getColumnNames() {
        return Collections.unmodifiableSet(columnNames);
    }

    public Set<String> getRowNames() {
        return Collections.unmodifiableSet(rowNames);
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

    public boolean containsValue(String rowName, String columnName) {
        if (!tabulatedValues.containsKey(rowName)) {
            return false;
        } else if (!tabulatedValues.get(rowName).containsKey(columnName)) {
            return false;
        } else {
            return true;
        }
    }

    public Number getValue(String rowName, String columnName) {
        Number value = tabulatedValues.get(rowName).get(columnName);
        return value;
    }

    private void validateParameters(List<ParameterChain> rowsChains, List<ParameterChain> columnsChains, String evaluationMeasure) {
        if (rowsChains == null) {
            throw new IllegalArgumentException("Rows chains to identify cannot be null");
        }
        if (rowsChains.isEmpty()) {
            throw new IllegalArgumentException("Rows chains to identify cannot be empty");
        }
        if (columnsChains == null) {
            throw new IllegalArgumentException("Columns chains to identify cannot be null");
        }
        if (columnsChains.isEmpty()) {
            throw new IllegalArgumentException("Columns chains to identify cannot be empty");
        }
    }

    private void validateParameters(ParameterOwner parameterOwner, Number value) {
        if (parameterOwner == null) {
            throw new IllegalArgumentException("parameterOwner cannot be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("Evaluatio measure value cannot be null");
        }
    }

    private void validateParameterOwner(ParameterOwner parameterOwner) {
        if (parameterOwner == null) {
            throw new IllegalArgumentException("parameterOwner cannot be null");
        }
    }

    public void prepareColumnAndRowNames(List<GroupCaseStudyResult> groupCaseStudyResults) {

        groupCaseStudyResults.stream().map(groupCaseStudyResult -> groupCaseStudyResult.getGroupCaseStudy()).forEach(groupCaseStudy -> this.getColumnIdentifier(groupCaseStudy));
        groupCaseStudyResults.stream().map(groupCaseStudyResult -> groupCaseStudyResult.getGroupCaseStudy()).forEach(groupCaseStudy -> this.getRowIdentifier(groupCaseStudy));
    }
}
