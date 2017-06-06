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
package delfos.dataset.papertestdatasets;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.dataset.util.DatasetOperations;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 02-May-2013
 */
public class ImplicitTrustDataset extends DatasetLoaderAbstract<Rating> {

    private static final long serialVersionUID = 1L;
    private RatingsDataset<Rating> ratingsDataset;
    private ContentDataset contentDataset;
    private UsersDataset usersDataset;

    @Override
    public RatingsDataset<Rating> getRatingsDataset() throws CannotLoadRatingsDataset {

        if (ratingsDataset == null) {
            Map<Long, Map<Long, Number>> ratingsByUser = new TreeMap<>();
            for (long i = 1; i <= 4; i++) {
                ratingsByUser.put(i, new TreeMap<>());
            }

            //User 1 ratings
            ratingsByUser.get(1).put(2l, 3);
            ratingsByUser.get(1).put(3l, 4);
            ratingsByUser.get(1).put(6l, 3);

            //User 2 ratings
            ratingsByUser.get(2).put(1l, 4);
            ratingsByUser.get(2).put(4l, 2);
            ratingsByUser.get(2).put(5l, 4);
            ratingsByUser.get(2).put(6l, 3);

            //User 3 ratings
            ratingsByUser.get(3).put(2l, 5);
            ratingsByUser.get(3).put(4l, 4);
            ratingsByUser.get(3).put(5l, 4);
            ratingsByUser.get(3).put(6l, 2);

            //User 4 ratings
            ratingsByUser.get(4).put(1l, 2);

            //Create ratings dataset
            ratingsDataset = new BothIndexRatingsDataset(DatasetOperations.convertNumberToRatings(ratingsByUser));

        }
        return ratingsDataset;
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {

        if (contentDataset == null) {
            //Create content dataset
            TreeSet<Item> items = new TreeSet<>();

            items.add(new Item(1));
            items.add(new Item(2));
            items.add(new Item(3));
            items.add(new Item(4));
            items.add(new Item(5));
            items.add(new Item(6));

            contentDataset = new ContentDatasetDefault(items);

        }
        return contentDataset;
    }

    @Override
    public RelevanceCriteria getDefaultRelevanceCriteria() {
        return new RelevanceCriteria(4);
    }
}
