/* 
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
