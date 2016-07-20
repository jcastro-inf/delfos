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
package delfos.view.neighborhood.components.uknn;

import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.recommendation.RecommendationsToUserWithNeighbors;
import java.awt.Component;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

/**
 * Interface component that holds a table with the user-user neighborhood.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class UserNeighborsTable {

    private final static long serialVersionUID = 1L;

    private final JScrollPane scroll;
    private final JTable userNeighborJTable;
    private final UserNeighborsJTableModel userNeighborsJTableModel;

    public UserNeighborsTable() {

        userNeighborsJTableModel = new UserNeighborsJTableModel();
        userNeighborJTable = new JTable(userNeighborsJTableModel);

        TableColumn column;
        for (int j = 0; j < userNeighborJTable.getColumnCount(); j++) {
            column = userNeighborJTable.getColumnModel().getColumn(j);
            if (j == 0) {
                column.setMaxWidth(100);
            }
            if (j == 1) {
                column.setMaxWidth(100);
            }
        }

        TableRowSorter<UserNeighborsJTableModel> sorter = new TableRowSorter<>(userNeighborsJTableModel);

        sorter.setComparator(UserNeighborsJTableModel.SIMILARITY_INDEX, (Number o1, Number o2) -> {
            return ((Double) o1.doubleValue()).compareTo(o2.doubleValue());
        });
        sorter.setComparator(UserNeighborsJTableModel.ID_USER_INDEX, (Number o1, Number o2) -> {
            return ((Double) o1.doubleValue()).compareTo(o2.doubleValue());
        });

        sorter.setComparator(UserNeighborsJTableModel.NAME_INDEX, (String o1, String o2) -> {
            return o1.compareToIgnoreCase(o2);
        });

        userNeighborJTable.setRowSorter(sorter);

        scroll = new JScrollPane(userNeighborJTable);
    }

    public Component getComponent() {
        return scroll;
    }

    public void setNeighbors(RecommendationsToUserWithNeighbors recommendations) {
        userNeighborsJTableModel.setNeighbors(recommendations.getNeighbors());
        userNeighborJTable.clearSelection();
    }

    public Neighbor getSelected() {
        int selectedRow = userNeighborJTable.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        } else {
            return userNeighborsJTableModel.getNeighborAtRow(selectedRow);
        }
    }

    public void addNeighborSelectorListener(ListSelectionListener listSelectionListener) {
        userNeighborJTable.getSelectionModel().addListSelectionListener(listSelectionListener);
    }
}
