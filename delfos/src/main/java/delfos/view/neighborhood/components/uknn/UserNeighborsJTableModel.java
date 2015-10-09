package delfos.view.neighborhood.components.uknn;

import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * @author jcastro
 */
public class UserNeighborsJTableModel extends AbstractTableModel {

    private final static long serialVersionUID = 1L;
    Collection<Recommendation> lista = new LinkedList<>();
    private Object[][] data = new Object[2][0];

    @Override
    public int getRowCount() {
        return data[0].length;
    }

    @Override
    public int getColumnCount() {
        return data.length;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "idUser";
        }
        if (column == 1) {
            return "similarity";
        }
        return "fallo";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data[columnIndex][rowIndex];
    }

    public void setNeighbors(List<Neighbor> neighbors) {

        data = new Object[2][neighbors.size()];
        int index = 0;
        for (Neighbor neighbor : neighbors) {

            data[0][index] = neighbor.getIdNeighbor();
            data[1][index] = neighbor.getSimilarity();

            index++;
        }
        fireTableDataChanged();
    }

}
