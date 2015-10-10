package delfos.view.neighborhood.components.ratings;

import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.Rating;
import java.awt.Component;
import java.util.Collection;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

/**
 * Table to show the ratings, initially sorted by id.
 *
 * @author Jorge Castro Gallardo
 */
public class RatingsTable {

    private final static long serialVersionUID = 1L;

    private final JScrollPane scroll;
    private final JTable ratingsJTable;
    private final RatingsJTableModel ratingsJTableModel;

    public RatingsTable() {

        ratingsJTableModel = new RatingsJTableModel();
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

        TableRowSorter<RatingsJTableModel> sorter = new TableRowSorter<>(ratingsJTableModel);

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

    public void setRatings(Collection<? extends Rating> ratings, ContentDataset contentDataset) {
        ratingsJTableModel.setRatings(ratings, contentDataset);
    }

}
