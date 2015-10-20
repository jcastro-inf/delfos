package delfos.view.neighborhood.components.uknn;

import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.user.User;
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

        TableRowSorter<RatingsUserNeighborJTableModel> sorter = new TableRowSorter<>(ratingsJTableModel);

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

    public void setRatings(DatasetLoader datasetLoader, User user, User neighbor) {
        ratingsJTableModel.setRatings(datasetLoader, user, neighbor);
    }

}
