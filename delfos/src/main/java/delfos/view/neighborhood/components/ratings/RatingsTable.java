package delfos.view.neighborhood.components.recommendations;

import delfos.rs.recommendation.Recommendations;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

/**
 * Tabla que muestra la información de los características del dataset. Muestra
 * también qué características están activos y el orden de mostrado. Esta clase
 * se utiliza en la generación del diagrama de coordenadas paralelas
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class RecommendationsTable {

    private final static long serialVersionUID = 1L;

    private final JScrollPane scroll;
    private final JTable recommendationsJTable;
    private final RecommendationsJTableModel recommendationsJTableModel;

    public RecommendationsTable() {

        recommendationsJTable = new JTable();
        recommendationsJTableModel = new RecommendationsJTableModel();

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
        scroll.setMinimumSize(new Dimension(200, 200));
    }

    public Component getComponent() {
        return scroll;
    }

    public void setRecomendaciones(Recommendations recommendations) {
        recommendationsJTableModel.setRecomendaciones(recommendations);
    }

}
