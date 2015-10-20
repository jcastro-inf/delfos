package delfos.view.neighborhood.components.iknn;

import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import java.awt.Component;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

/**
 * Table to show the ratings, initially sorted by id.
 *
 * @author Jorge Castro Gallardo
 */
public class RatingsItemNeighborTable {

    private final static long serialVersionUID = 1L;

    private final JScrollPane scroll;
    private final JTable ratingsJTable;
    private final RatingsItemNeighborJTableModel ratingsJTableModel;

    public RatingsItemNeighborTable() {

        ratingsJTableModel = new RatingsItemNeighborJTableModel();
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

        TableRowSorter<RatingsItemNeighborJTableModel> sorter = new TableRowSorter<>(ratingsJTableModel);

        sorter.setComparator(0, (Number o1, Number o2) -> {
            return ((Double) o1.doubleValue()).compareTo(o2.doubleValue());
        });
        sorter.setComparator(1, (Number o1, Number o2) -> {
            return ((Double) o1.doubleValue()).compareTo(o2.doubleValue());
        });

        ratingsJTable.setRowSorter(sorter);

        scroll = new JScrollPane(ratingsJTable);
    }

    public Component getComponent() {
        return scroll;
    }

    public void setRatings(DatasetLoader datasetLoader, Item target, Item neighbor) {
        ratingsJTableModel.setRatings(datasetLoader, target, neighbor);
    }

}
