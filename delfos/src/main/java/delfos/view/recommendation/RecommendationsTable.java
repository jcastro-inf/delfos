package delfos.view.recommendation;

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
public class RecommendationsTable extends JTable {

    private final static long serialVersionUID = 1L;

    /**
     * Crea una tabla para mostrar las recomendaciones a partir del modelo
     * indicado.
     *
     * @param recommendationsTableModel Modelo que muestra esta tabla.
     */
    public RecommendationsTable(RecommendationsJTableModel recommendationsTableModel) {
        super(recommendationsTableModel);

        TableColumn column;
        for (int j = 0; j < getColumnCount(); j++) {
            column = getColumnModel().getColumn(j);
            if (j == 0) {
                column.setMaxWidth(100);
            }
            if (j == 1) {
                column.setMaxWidth(100);
            }
        }

        TableRowSorter<RecommendationsJTableModel> sorter = new TableRowSorter<>(recommendationsTableModel);

        sorter.setComparator(0, (Number o1, Number o2) -> {
            return ((Double) o1.doubleValue()).compareTo(o2.doubleValue());
        });
        sorter.setComparator(1, (Number o1, Number o2) -> {
            return ((Double) o1.doubleValue()).compareTo(o2.doubleValue());
        });

        setRowSorter(sorter);
    }
}
