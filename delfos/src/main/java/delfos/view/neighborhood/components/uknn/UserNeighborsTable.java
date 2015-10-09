package delfos.view.neighborhood.components.uknn;

import delfos.rs.recommendation.RecommendationsWithNeighbors;
import java.awt.Component;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

/**
 * Interface component that holds a table with the user-user neighborhood.
 *
 * @author Jorge Castro Gallardo
 */
public class UserNeighborsTable {

    private final static long serialVersionUID = 1L;

    private final JScrollPane scroll;
    private final JTable recommendationsJTable;
    private final UserNeighborsJTableModel userNeighborsJTableModel;

    public UserNeighborsTable() {

        userNeighborsJTableModel = new UserNeighborsJTableModel();
        recommendationsJTable = new JTable(userNeighborsJTableModel);

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

        TableRowSorter<UserNeighborsJTableModel> sorter = new TableRowSorter<>(userNeighborsJTableModel);

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

    public void setNeighbors(RecommendationsWithNeighbors recommendations) {
        userNeighborsJTableModel.setNeighbors(recommendations.getNeighbors());
    }
}
