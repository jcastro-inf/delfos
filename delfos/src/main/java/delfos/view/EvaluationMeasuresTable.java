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

import javax.swing.JTable;

/**
 * Tabla que muestra la información de los características del dataset. Muestra
 * también qué características están activos y el orden de mostrado. Esta clase
 * se utiliza en la generación del diagrama de coordenadas paralelas
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class EvaluationMeasuresTable extends JTable {

    private static final long serialVersionUID = 1L;

    /**
     * Creador de la tabla de medidas de evaluación.
     *
     * @param ordenCaracteristicasTableModel Modelo de datos que muestra la
     * tabla.
     */
    public EvaluationMeasuresTable(EvaluationMeasuresJTableModel ordenCaracteristicasTableModel) {
        super(ordenCaracteristicasTableModel);

        for (int j = 0; j < getColumnCount(); j++) {
            if (j == 0) {
                getColumnModel().getColumn(j).setMaxWidth(30);
            }
        }
    }
}
