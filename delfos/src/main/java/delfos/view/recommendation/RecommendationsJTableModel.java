package delfos.view.recommendation;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.LinkedList;
import javax.swing.table.AbstractTableModel;

/**
 * Clase que tiene los datos necesarios para establecer el orden de izquierda a
 * derecha en que los características se muestran en el diagrama de coordenadas
 * paralelas.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version Unknown date
 * @version 10-Octubre-2013
 */
public class RecommendationsJTableModel extends AbstractTableModel {

    private final static long serialVersionUID = 1L;
    Collection<Recommendation> lista = new LinkedList<>();
    private Object[][] datos = new Object[3][0];
    private ContentDataset cd;

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

    void setRecomendaciones(Collection<Recommendation> recommendOnly) {
        if (this.cd == null) {
            datos = new Object[3][0];
            return;
        }

        datos = new Object[3][recommendOnly.size()];
        int index = 0;
        for (Recommendation r : recommendOnly) {
            datos[0][index] = r.getIdItem();
            datos[1][index] = r.getPreference().floatValue();
            try {
                datos[2][index] = cd.get(r.getIdItem()).getName();
            } catch (ItemNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            } catch (EntityNotFound ex) {
                ex.isA(Item.class);
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            }
            index++;
        }
        fireTableDataChanged();
    }

    void setContentDataset(ContentDataset cd) {
        this.cd = cd;
        datos = new Object[3][0];
        fireTableDataChanged();
    }
}
