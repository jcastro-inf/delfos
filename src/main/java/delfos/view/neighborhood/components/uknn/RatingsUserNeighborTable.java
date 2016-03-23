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

import delfos.common.StringsOrderings;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.user.User;
import java.awt.Component;
import java.util.Comparator;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

/**
 * Table to show the ratings, initially sorted by id.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class RatingsUserNeighborTable {

    private final static long serialVersionUID = 1L;

    private final JScrollPane scroll;
    private final JTable ratingsJTable;
    private final RatingsUserNeighborJTableModel ratingsJTableModel;

    public RatingsUserNeighborTable() {

        ratingsJTableModel = new RatingsUserNeighborJTableModel();
        ratingsJTable = new JTable(ratingsJTableModel);

        TableColumn column;
        for (int j = 0; j < ratingsJTable.getColumnCount(); j++) {
            column = ratingsJTable.getColumnModel().getColumn(j);
            if (j == 0) {
                column.setMaxWidth(100);
            }
            if (j == 1) {
                column.setMaxWidth(100);
            }
        }

        final Comparator<Object> numberSorter = (i1, i2) -> {
            if (i1 instanceof Number && i2 instanceof Number) {
                return Double.compare(((Number) i1).doubleValue(), ((Number) i2).doubleValue());
            } else {
                return StringsOrderings.compareNatural(i1.toString(), i2.toString());
            }
        };

        TableRowSorter<RatingsUserNeighborJTableModel> sorter = new TableRowSorter<>(ratingsJTableModel);

        sorter.setComparator(RatingsUserNeighborJTableModel.ID_ITEM_COLUMN, numberSorter);
        sorter.setComparator(RatingsUserNeighborJTableModel.ITEM_NAME_COLUMN, (i1, i2) -> StringsOrderings.compareNatural(i1.toString(), i2.toString()));
        sorter.setComparator(RatingsUserNeighborJTableModel.NEIGHBOR_RATING_COLUMN, numberSorter);
        sorter.setComparator(RatingsUserNeighborJTableModel.ID_ITEM_COLUMN, numberSorter);

        ratingsJTable.setRowSorter(sorter);

        scroll = new JScrollPane(ratingsJTable);
    }

    public Component getComponent() {
        return scroll;
    }

    public void setRatings(DatasetLoader datasetLoader, User user, User neighbor) {
        ratingsJTableModel.setRatings(datasetLoader, user, neighbor);
    }

}
