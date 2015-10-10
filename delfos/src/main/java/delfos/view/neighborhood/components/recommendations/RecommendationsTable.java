package delfos.view.neighborhood.components.recommendations;

import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import java.awt.Component;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

/**
 * Table to show the recommendations, initially sorted by prediction.
 *
 * @author Jorge Castro Gallardo
 */
public class RecommendationsTable {

    private final static long serialVersionUID = 1L;

    private final JScrollPane scroll;
    private final JTable recommendationsJTable;
    private final RecommendationsJTableModel recommendationsJTableModel;

    public RecommendationsTable() {

        recommendationsJTableModel = new RecommendationsJTableModel();
        recommendationsJTable = new JTable(recommendationsJTableModel);

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

        TableRowSorter<RecommendationsJTableModel> sorter = new TableRowSorter<>(recommendationsJTableModel);

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

    public void setRecomendaciones(Recommendations recommendations) {

        recommendationsJTableModel.setRecomendaciones(recommendations);
    }

    public void addRecommendationSelectorListener(ListSelectionListener listSelectionListener) {
        recommendationsJTable.getSelectionModel().addListSelectionListener(listSelectionListener);
    }

    public Recommendation getSelectedRecommendation() {
        int selectedRow = recommendationsJTable.getSelectedRow();

        return recommendationsJTableModel.getRecommendationAtRow(selectedRow);
    }

}
