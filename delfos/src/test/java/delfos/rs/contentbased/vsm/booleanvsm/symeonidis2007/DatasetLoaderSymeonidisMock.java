package delfos.rs.contentbased.vsm.booleanvsm.symeonidis2007;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureGenerator;
import delfos.dataset.basic.features.FeatureType;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 31-oct-2013
 */
public class DatasetLoaderSymeonidisMock extends DatasetLoaderAbstract implements ContentDatasetLoader {

    private final RatingsDataset<? extends Rating> ratingsDataset;
    private final ContentDataset contentDataset;

    public DatasetLoaderSymeonidisMock() {

        Collection<Rating> ratings = new LinkedList<>();
        ratings.add(new Rating(1, 2, 4));
        ratings.add(new Rating(1, 5, 5));

        ratings.add(new Rating(2, 2, 3));
        ratings.add(new Rating(2, 4, 4));

        ratings.add(new Rating(3, 6, 4));

        ratings.add(new Rating(4, 1, 5));
        ratings.add(new Rating(4, 3, 3));

        ratingsDataset = new BothIndexRatingsDataset(ratings);

        TreeSet<Item> items = new TreeSet<>();
        FeatureGenerator featureGenerator = new FeatureGenerator();
        Feature feature1 = featureGenerator.createFeature("Feature1", FeatureType.Nominal);
        Feature feature2 = featureGenerator.createFeature("Feature2", FeatureType.Nominal);
        Feature feature3 = featureGenerator.createFeature("Feature3", FeatureType.Nominal);
        Feature feature4 = featureGenerator.createFeature("Feature4", FeatureType.Nominal);

        {
            //Item 5 construction.
            Map<Feature, Object> itemFeatures = new TreeMap<>();
            itemFeatures.put(feature1, "1");
            itemFeatures.put(feature2, "1");
            itemFeatures.put(feature3, "1");
            items.add(new Item(5, "Item5", itemFeatures));
        }
        {
            //Item 1 construction.
            Map<Feature, Object> itemFeatures = new TreeMap<>();
            itemFeatures.put(feature2, "1");
            items.add(new Item(1, "Item1", itemFeatures));
        }
        {
            //Item 2 construction.
            Map<Feature, Object> itemFeatures = new TreeMap<>();
            itemFeatures.put(feature1, "1");
            itemFeatures.put(feature2, "1");
            items.add(new Item(2, "Item2", itemFeatures));
        }
        {
            //Item 3 construction.
            Map<Feature, Object> itemFeatures = new TreeMap<>();
            itemFeatures.put(feature2, "1");
            itemFeatures.put(feature3, "1");
            items.add(new Item(3, "Item3", itemFeatures));
        }
        {
            //Item 4 construction.
            Map<Feature, Object> itemFeatures = new TreeMap<>();
            itemFeatures.put(feature2, "1");
            items.add(new Item(4, "Item4", itemFeatures));
        }
        {
            //Item 6 construction.
            Map<Feature, Object> itemFeatures = new TreeMap<>();
            itemFeatures.put(feature4, "1");
            items.add(new Item(6, "Item6", itemFeatures));
        }
        contentDataset = new ContentDatasetDefault(items);

    }

    @Override
    public RatingsDataset<? extends Rating> getRatingsDataset() throws CannotLoadRatingsDataset {
        return ratingsDataset;
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {
        return contentDataset;
    }

    @Override
    public RelevanceCriteria getDefaultRelevanceCriteria() {
        return new RelevanceCriteria(2);
    }
}
