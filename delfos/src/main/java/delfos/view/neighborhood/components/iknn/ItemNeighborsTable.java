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
package delfos.view.neighborhood.components.iknn;

import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.user.User;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import java.awt.Component;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

/**
 * Interface component that holds a table with the item-item neighborhood.
 *
 * @author Jorge Castro Gallardo
 */
public class ItemNeighborsTable {

    private final static long serialVersionUID = 1L;

    private final JScrollPane scroll;
    private final JTable itemNeighborJTable;
    private final ItemNeighborsJTableModel itemNeighborsJTableModel;

    public ItemNeighborsTable() {

        itemNeighborsJTableModel = new ItemNeighborsJTableModel();
        itemNeighborJTable = new JTable(itemNeighborsJTableModel);

        TableColumn column;
        for (int j = 0; j < itemNeighborJTable.getColumnCount(); j++) {
            column = itemNeighborJTable.getColumnModel().getColumn(j);
            if (j == 0) {
                column.setMaxWidth(100);
            }
            if (j == 1) {
                column.setMaxWidth(100);
            }
        }

        TableRowSorter<ItemNeighborsJTableModel> sorter = new TableRowSorter<>(itemNeighborsJTableModel);

        sorter.setComparator(ItemNeighborsJTableModel.SIMILARITY_INDEX, (Number o1, Number o2) -> {
            return ((Double) o1.doubleValue()).compareTo(o2.doubleValue());
        });
        sorter.setComparator(ItemNeighborsJTableModel.ID_ITEM_INDEX, (Number o1, Number o2) -> {
            return ((Double) o1.doubleValue()).compareTo(o2.doubleValue());
        });

        sorter.setComparator(ItemNeighborsJTableModel.NAME_INDEX, (String o1, String o2) -> {
            return o1.compareToIgnoreCase(o2);
        });

        itemNeighborJTable.setRowSorter(sorter);

        scroll = new JScrollPane(itemNeighborJTable);
    }

    public Component getComponent() {
        return scroll;
    }

    public void setNeighbors(User targetUser, List<Neighbor> itemNeighbors, DatasetLoader datasetLoader) {
        itemNeighborsJTableModel.setNeighbors(targetUser, itemNeighbors, datasetLoader);
        itemNeighborJTable.clearSelection();
    }

    public Neighbor getSelected() {
        int selectedRow = itemNeighborJTable.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        } else {
            return itemNeighborsJTableModel.getNeighborAtRow(selectedRow);
        }
    }

    public void addNeighborSelectorListener(ListSelectionListener listSelectionListener) {
        itemNeighborJTable.getSelectionModel().addListSelectionListener(listSelectionListener);
    }
}
