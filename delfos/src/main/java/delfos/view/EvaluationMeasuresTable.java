package delfos.view;

import javax.swing.JTable;

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
public class EvaluationMeasuresTable extends JTable {

    private static final long serialVersionUID = 1L;

    /**
     * Creador de la tabla de medidas de evaluación.
     *
     * @param ordenCaracteristicasTableModel Modelo de datos que muestra la
     * tabla.
     */
    public EvaluationMeasuresTable(EvaluationMeasuresJTableModel ordenCaracteristicasTableModel) {
        super(ordenCaracteristicasTableModel);

        for (int j = 0; j < getColumnCount(); j++) {
            if (j == 0) {
                getColumnModel().getColumn(j).setMaxWidth(30);
            }
        }
    }
}
