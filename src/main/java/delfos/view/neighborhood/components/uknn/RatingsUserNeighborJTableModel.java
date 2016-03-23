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
package delfos.view.neighborhood.components.uknn;

import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.table.AbstractTableModel;

/**
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class RatingsUserNeighborJTableModel extends AbstractTableModel {

    private final static long serialVersionUID = 1L;
    Collection<Rating> lista = new LinkedList<>();
    private Object[][] datos = new Object[3][0];

    private static final int ID_ITEM_COLUMN = 0;
    private static final int ITEM_NAME_COLUMN = 1;
    private static final int USER_RATING_COLUMN = 2;
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
    public Class<?> getColumnClass(int column) {
        if (column == ID_ITEM_COLUMN) {
            return Integer.class;
        }
        if (column == ITEM_NAME_COLUMN) {
            return String.class;
        }
        if (column == USER_RATING_COLUMN) {
            return Object.class;
        }
        if (column == NEIGHBOR_RATING_COLUMN) {
            return Object.class;
        }
        return null;
    }

    @Override
    public String getColumnName(int column) {
        if (column == ID_ITEM_COLUMN) {
            return "idItem";
        }
        if (column == ITEM_NAME_COLUMN) {
            return "Name";
        }
        if (column == USER_RATING_COLUMN) {
            return "target";
        }
        if (column == NEIGHBOR_RATING_COLUMN) {
            return "neighbor";
        }
        return "fallo";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return datos[columnIndex][rowIndex];
    }

    public synchronized void setRatings(DatasetLoader datasetLoader, User user, User neighbor) {

        ContentDataset contentDataset = ((ContentDatasetLoader) datasetLoader).getContentDataset();
        RatingsDataset ratingsDataset = datasetLoader.getRatingsDataset();

        Set<Item> itemsRatedUnion = contentDataset.stream().filter(item -> {
            boolean userHasRated = user != null && ratingsDataset.getUserRated(user.getId()).contains(item.getId());
            boolean neighborHasRated = neighbor != null && ratingsDataset.getUserRated(neighbor.getId()).contains(item.getId());
            return userHasRated || neighborHasRated;
        }).collect(Collectors.toSet());

        datos = new Object[COLUMN_COUNT][itemsRatedUnion.size()];
        int index = 0;
        for (Item item : itemsRatedUnion) {
            Rating userRating = user == null ? null : ratingsDataset.getRating(user.getId(), item.getId());
            Rating neighborRating = neighbor == null ? null : ratingsDataset.getRating(neighbor.getId(), item.getId());

            datos[ID_ITEM_COLUMN][index] = item.getId();
            datos[USER_RATING_COLUMN][index] = userRating == null ? "" : userRating.getRatingValue().doubleValue();
            datos[NEIGHBOR_RATING_COLUMN][index] = neighborRating == null ? "" : neighborRating.getRatingValue().doubleValue();
            datos[ITEM_NAME_COLUMN][index] = item.getName();

            index++;
        }
        fireTableDataChanged();
    }

}
