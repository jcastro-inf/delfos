package delfos.view.neighborhood.components.recommendations;

import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import java.util.Collection;
import java.util.LinkedList;
import javax.swing.table.AbstractTableModel;

/**
 * @author jcastro
 */
public class RecommendationsJTableModel extends AbstractTableModel {

    private final static long serialVersionUID = 1L;
    Collection<Recommendation> lista = new LinkedList<>();
    private Object[][] datos = new Object[3][0];

    @Override
    public int getRowCount() {
        return datos[0].length;
    }

    @Override
    public int getColumnCount() {
        return datos.length;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "idItem";
        }
        if (column == 1) {
            return "Pref";
        }
        if (column == 2) {
            return "Name";
        }
        return "fallo";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return datos[columnIndex][rowIndex];
    }

    public void setRecomendaciones(Recommendations recommendations) {

        datos = new Object[3][recommendations.getRecommendations().size()];
        int index = 0;
        for (Recommendation recommendation : recommendations.getRecommendations()) {
            datos[0][index] = recommendation.getItem().getId();
            datos[1][index] = recommendation.getPreference().floatValue();
            datos[2][index] = recommendation.getItem().getName();

            index++;
        }
        fireTableDataChanged();
    }

}
