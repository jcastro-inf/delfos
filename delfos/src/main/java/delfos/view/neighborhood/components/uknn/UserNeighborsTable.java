package delfos.view.neighborhood.components.uknn;

import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.recommendation.RecommendationsWithNeighbors;
import java.awt.Component;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionListener;
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

    public void setNeighbors(RecommendationsWithNeighbors recommendations) {
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
