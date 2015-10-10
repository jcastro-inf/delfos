package delfos.view.neighborhood.components.recommendations;

import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import javax.swing.table.AbstractTableModel;

/**
 * @author jcastro
 */
public class RecommendationsJTableModel extends AbstractTableModel {

    private final static long serialVersionUID = 1L;

    private static final int ID_ITEM_INDEX = 0;
    private static final int PREFERENCE_INDEX = 1;
    private static final int NAME_INDEX = 2;
    private static final int RECOMMENDATION_INDEX = 3;

    private static final int COLUMN_COUNT = 3;
    private static final int DATA_COLUMN_COUNT = COLUMN_COUNT + 1;

    private Object[][] datos = new Object[DATA_COLUMN_COUNT][0];

    @Override
    public int getRowCount() {
        return datos[0].length;
    }

    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    @Override
    public String getColumnName(int column) {
        if (column == ID_ITEM_INDEX) {
            return "idItem";
        }
        if (column == PREFERENCE_INDEX) {
            return "Preference";
        }
        if (column == NAME_INDEX) {
            return "Name";
        }
        return "fallo";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return datos[columnIndex][rowIndex];
    }

    public void setRecomendaciones(Recommendations recommendations) {

        datos = new Object[DATA_COLUMN_COUNT][recommendations.getRecommendations().size()];
        int rowIndex = 0;
        for (Recommendation recommendation : recommendations.getRecommendations()) {
            datos[RECOMMENDATION_INDEX][rowIndex] = recommendation;
            datos[ID_ITEM_INDEX][rowIndex] = recommendation.getItem().getId();
            datos[PREFERENCE_INDEX][rowIndex] = recommendation.getPreference().floatValue();
            datos[NAME_INDEX][rowIndex] = recommendation.getItem().getName();

            rowIndex++;
        }
        fireTableDataChanged();
    }

    Recommendation getRecommendationAtRow(int selectedRow) {
        return (Recommendation) getValueAt(selectedRow, RECOMMENDATION_INDEX);
    }

}
