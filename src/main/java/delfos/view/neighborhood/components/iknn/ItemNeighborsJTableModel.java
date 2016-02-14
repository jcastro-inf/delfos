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
package delfos.view.neighborhood.components.iknn;

import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class ItemNeighborsJTableModel extends AbstractTableModel {

    private final static long serialVersionUID = 1L;

    final static int ID_ITEM_INDEX = 0;
    final static int SIMILARITY_INDEX = 1;
    final static int NAME_INDEX = 2;
    final static int TARGET_USER_RATING_INDEX = 3;
    final static int NEIGHBOR_INDEX = 4;

    private final static int COLUMN_COUNT = 4;
    private final static int DATA_COLUMN_COUNT = COLUMN_COUNT + 1;

    Collection<Recommendation> lista = new LinkedList<>();
    private Object[][] data = new Object[DATA_COLUMN_COUNT][0];

    @Override
    public int getRowCount() {
        return data[0].length;
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
        if (column == SIMILARITY_INDEX) {
            return "similarity";
        }
        if (column == NAME_INDEX) {
            return "name";
        }
        if (column == TARGET_USER_RATING_INDEX) {
            return "user rating";
        }
        return "fallo";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data[columnIndex][rowIndex];
    }

    public void setNeighbors(User targetUser, List<Neighbor> neighbors, DatasetLoader datasetLoader) {

        data = new Object[DATA_COLUMN_COUNT][neighbors.size()];
        int rowIndex = 0;
        for (Neighbor neighbor : neighbors) {
            Rating rating = datasetLoader.getRatingsDataset().getRating(targetUser.getId(), neighbor.getIdNeighbor());

            data[ID_ITEM_INDEX][rowIndex] = neighbor.getIdNeighbor();
            data[SIMILARITY_INDEX][rowIndex] = neighbor.getSimilarity();
            data[NAME_INDEX][rowIndex] = neighbor.getNeighbor().getName();
            data[TARGET_USER_RATING_INDEX][rowIndex] = rating == null ? "" : rating.getRatingValue().doubleValue();
            data[NEIGHBOR_INDEX][rowIndex] = neighbor;

            rowIndex++;
        }
        fireTableDataChanged();
    }

    Neighbor getNeighborAtRow(int selectedRow) {

        return (Neighbor) getValueAt(selectedRow, NEIGHBOR_INDEX);
    }

}
