package delfos.dataset.papertestdatasets;

import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.dataset.util.DatasetOperations;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemAlreadyExists;

/**
 *
* @author Jorge Castro Gallardo
 * @version 1.0 02-May-2013
 */
public class ImplicitTrustDataset extends DatasetLoaderAbstract<Rating> implements ContentDatasetLoader {

    private static final long serialVersionUID = 1L;
    private RatingsDataset<Rating> ratingsDataset;
    private ContentDataset contentDataset;

    @Override
    public RatingsDataset<Rating> getRatingsDataset() throws CannotLoadRatingsDataset {

        if (ratingsDataset == null) {
            Map<Integer, Map<Integer, Number>> ratingsByUser = new TreeMap<Integer, Map<Integer, Number>>();
            for (int i = 1; i <= 4; i++) {
                ratingsByUser.put(i, new TreeMap<Integer, Number>());
            }

            //User 1 ratings
            ratingsByUser.get(1).put(2, 3);
            ratingsByUser.get(1).put(3, 4);
            ratingsByUser.get(1).put(6, 3);

            //User 2 ratings
            ratingsByUser.get(2).put(1, 4);
            ratingsByUser.get(2).put(4, 2);
            ratingsByUser.get(2).put(5, 4);
            ratingsByUser.get(2).put(6, 3);

            //User 3 ratings
            ratingsByUser.get(3).put(2, 5);
            ratingsByUser.get(3).put(4, 4);
            ratingsByUser.get(3).put(5, 4);
            ratingsByUser.get(3).put(6, 2);

            //User 4 ratings
            ratingsByUser.get(4).put(1, 2);

            //Create ratings dataset
            ratingsDataset = new BothIndexRatingsDataset(DatasetOperations.convertNumberToRatings(ratingsByUser));

        }
        return ratingsDataset;
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {

        if (contentDataset == null) {
            //Create content dataset
            LinkedList<Item> items = new LinkedList<Item>();

            items.add(new Item(1));
            items.add(new Item(2));
            items.add(new Item(3));
            items.add(new Item(4));
            items.add(new Item(5));
            items.add(new Item(6));
            try {
                contentDataset = new ContentDatasetDefault(items);
            } catch (ItemAlreadyExists ex) {
                throw new CannotLoadContentDataset(ex);
            }
        }
        return contentDataset;
    }

    @Override
    public RelevanceCriteria getDefaultRelevanceCriteria() {
        return new RelevanceCriteria(4);
    }
}
