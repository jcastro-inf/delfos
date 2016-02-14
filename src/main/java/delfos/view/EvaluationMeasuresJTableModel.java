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
package delfos.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import delfos.results.evaluationmeasures.EvaluationMeasure;

/**
 * Clase que tiene los datos necesarios para establecer el orden de izquierda a
 * derecha en que los características se muestran en el diagrama de coordenadas
 * paralelas
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class EvaluationMeasuresJTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    List<EvaluationMeasure> lista = new LinkedList<>();
    private Object[][] datos = new Object[2][0];

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return Boolean.class;
        }
        return super.getColumnClass(columnIndex);
    }

    /**
     * Devuelve las medidas de evaluación que han sido seleccionadas por el
     * usuario.
     *
     * @return Medidas de evaluación seleccionadas por el usuario.
     */
    public Collection<EvaluationMeasure> getSelectedEvaluationMeasures() {
        LinkedList<EvaluationMeasure> selectedEvaluationMeasures = new LinkedList<>();
        for (int i = 0; i < lista.size(); i++) {
            if (getSeleccionFila(i)) {
                selectedEvaluationMeasures.add(lista.get(i));
            }
        }
        return new ArrayList<>(selectedEvaluationMeasures);
    }

    /**
     * Permuta entre activo y no activo la característica de la fila
     * seleccionada
     *
     * @param rowIndex fila que determina la característica
     */
    public void setSeleccionFila(int rowIndex) {
        datos[0][rowIndex] = !(Boolean) datos[0][rowIndex];
        fireTableDataChanged();
    }

    /**
     * Obtiene si está activo o no una característica para ser mostrado en el
     * diagrama de coordenadas paralelas
     *
     * @param rowIndex fila que determina la característica
     * @return true si está activo, false si no está activo
     */
    public boolean getSeleccionFila(int rowIndex) {
        return (Boolean) datos[0][rowIndex];
    }

    @Override
    public int getRowCount() {
        return datos[0].length;
    }

    @Override
    public int getColumnCount() {
        return datos.length;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Sel.";
        }
        if (column == 1) {
            return "Name";
        }
        return "fallo";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return datos[columnIndex][rowIndex];
    }

    /**
     * Añade la medida de evaluación al modelo de datos.
     *
     * @param em Nueva medida de evaluación a considerar.
     */
    public void addEvaluationMeasure(EvaluationMeasure em) {
        lista.add(em);

        Collections.sort(lista);

        Object[][] datosActual = new Object[2][lista.size()];

        for (int i = 0; i < lista.size(); i++) {
            datosActual[0][i] = true;
            datosActual[1][i] = lista.get(i);
        }
        datos = datosActual;

        fireTableDataChanged();
    }
}
