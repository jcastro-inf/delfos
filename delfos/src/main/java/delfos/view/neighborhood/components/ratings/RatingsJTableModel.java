package delfos.view.neighborhood.components.ratings;

import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.rating.Rating;
import java.util.Collection;
import java.util.LinkedList;
import javax.swing.table.AbstractTableModel;

/**
 * @author jcastro
 */
public class RatingsJTableModel extends AbstractTableModel {

    private final static long serialVersionUID = 1L;
    Collection<Rating> lista = new LinkedList<>();
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
            return "Rating";
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

    public void setRatings(Collection< ? extends Rating> ratings, ContentDataset contentDataset) {
        datos = new Object[3][ratings.size()];
        int index = 0;
        for (Rating rating : ratings) {
            Item item = contentDataset.get(rating.getIdItem());

            datos[0][index] = item.getId();
            datos[1][index] = rating.getRatingValue().floatValue();
            datos[2][index] = item.getName();

            index++;
        }
        fireTableDataChanged();
    }

}
