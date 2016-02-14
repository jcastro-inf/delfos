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

import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.table.AbstractTableModel;

/**
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class RatingsItemNeighborJTableModel extends AbstractTableModel {

    private final static long serialVersionUID = 1L;
    Collection<Rating> lista = new LinkedList<>();
    private Object[][] datos = new Object[3][0];

    private static final int USER_ID_COLUMN = 0;
    private static final int USER_NAME_COLUMN = 1;
    private static final int TARGET_RATING_COLUMN = 2;
    private static final int NEIGHBOR_RATING_COLUMN = 3;

    private static final int COLUMN_COUNT = 4;

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
        if (column == USER_ID_COLUMN) {
            return "idUser";
        }
        if (column == USER_NAME_COLUMN) {
            return "name";
        }
        if (column == TARGET_RATING_COLUMN) {
            return "target";
        }
        if (column == NEIGHBOR_RATING_COLUMN) {
            return "neighbor";
        }
        throw new IndexOutOfBoundsException("Collumn index out of bound, '" + column + "' > '" + COLUMN_COUNT + "' ");
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return datos[columnIndex][rowIndex];
    }

    public synchronized void setRatings(DatasetLoader datasetLoader, Item target, Item neighbor) {

        RatingsDataset ratingsDataset = datasetLoader.getRatingsDataset();
        UsersDataset usersDataset = datasetLoader.getUsersDataset();

        Set<User> usersRatedUnion = usersDataset.stream().filter(user -> {
            boolean itemHasRated = target != null && ratingsDataset.getItemRated(target.getId()).contains(user.getId());
            boolean neighborHasRated = neighbor != null && ratingsDataset.getItemRated(neighbor.getId()).contains(user.getId());
            return itemHasRated || neighborHasRated;
        }).collect(Collectors.toSet());

        datos = new Object[COLUMN_COUNT][usersRatedUnion.size()];
        int index = 0;
        for (User user : usersRatedUnion) {
            Rating targetRating = target == null ? null : ratingsDataset.getRating(user.getId(), target.getId());
            Rating neighborRating = neighbor == null ? null : ratingsDataset.getRating(user.getId(), neighbor.getId());

            datos[USER_ID_COLUMN][index] = user.getId();
            datos[USER_NAME_COLUMN][index] = user.getName();

            datos[TARGET_RATING_COLUMN][index] = targetRating == null ? "" : targetRating.getRatingValue().doubleValue();
            datos[NEIGHBOR_RATING_COLUMN][index] = neighborRating == null ? "" : neighborRating.getRatingValue().doubleValue();

            index++;
        }
        fireTableDataChanged();
    }

}
