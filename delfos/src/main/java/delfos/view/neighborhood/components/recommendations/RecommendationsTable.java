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
package delfos.view.neighborhood.components.recommendations;

import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import java.awt.Component;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

/**
 * Table to show the recommendations, initially sorted by prediction.
 *
 * @author Jorge Castro Gallardo
 */
public class RecommendationsTable {

    private final static long serialVersionUID = 1L;

    private final JScrollPane scroll;
    private final JTable recommendationsJTable;
    private final RecommendationsJTableModel recommendationsJTableModel;

    public RecommendationsTable() {

        recommendationsJTableModel = new RecommendationsJTableModel();
        recommendationsJTable = new JTable(recommendationsJTableModel);

        TableColumn column;
        for (int j = 0; j < recommendationsJTable.getColumnCount(); j++) {
            column = recommendationsJTable.getColumnModel().getColumn(j);
            if (j == 0) {
                column.setMaxWidth(100);
            }
            if (j == 1) {
                column.setMaxWidth(100);
            }
        }

        TableRowSorter<RecommendationsJTableModel> sorter = new TableRowSorter<>(recommendationsJTableModel);

        sorter.setComparator(0, (Number o1, Number o2) -> {
            return ((Double) o1.doubleValue()).compareTo(o2.doubleValue());
        });
        sorter.setComparator(1, (Number o1, Number o2) -> {
            return ((Double) o1.doubleValue()).compareTo(o2.doubleValue());
        });

        recommendationsJTable.setRowSorter(sorter);

        scroll = new JScrollPane(recommendationsJTable);
    }

    public Component getComponent() {
        return scroll;
    }

    public void setRecomendaciones(Recommendations recommendations) {

        recommendationsJTableModel.setRecomendaciones(recommendations);
    }

    public void addRecommendationSelectorListener(ListSelectionListener listSelectionListener) {
        recommendationsJTable.getSelectionModel().addListSelectionListener(listSelectionListener);
    }

    public Recommendation getSelectedRecommendation() {
        int selectedRow = recommendationsJTable.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        } else {
            return recommendationsJTableModel.getRecommendationAtRow(selectedRow);
        }
    }

}
