package delfos.rs.persistence;

import java.util.ArrayList;
import java.util.List;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.items.ItemAlreadyExists;
import delfos.common.exceptions.dataset.users.UserAlreadyExists;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureGenerator;
import delfos.dataset.basic.features.FeatureType;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.basic.user.UsersDatasetAdapter;
import delfos.dataset.basic.loader.types.CompleteDatasetLoaderAbstract_withTrust;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;

/**
 *
 * @version 07-may-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class MockDatasetLoader extends CompleteDatasetLoaderAbstract_withTrust<Rating> {

    private final BothIndexRatingsDataset<Rating> ratingsDataset;
    private final ContentDataset contentDataset;
    private final UsersDataset usersDataset;

    public MockDatasetLoader() throws ItemAlreadyExists, UserAlreadyExists {

        List<Rating> ratings = new ArrayList<>();
        List<User> users = new ArrayList<>();
        List<Item> items = new ArrayList<>();

        //Items
        {
            FeatureGenerator featureGenerator = new FeatureGenerator();

            Feature[] features = new Feature[4];
            featureGenerator.createFeature("feature1_float", FeatureType.Numerical);
            featureGenerator.createFeature("feature2_int", FeatureType.Numerical);
            featureGenerator.createFeature("feature3_class", FeatureType.Nominal);
            featureGenerator.createFeature("feature4_boolean", FeatureType.Nominal);

            features[0] = featureGenerator.searchFeature("feature1_float");
            features[1] = featureGenerator.searchFeature("feature2_int");
            features[2] = featureGenerator.searchFeature("feature3_class");
            features[3] = featureGenerator.searchFeature("feature4_boolean");

            Object[] item1_values = new Object[4];
            item1_values[0] = 5.4;
            item1_values[1] = 9;
            item1_values[2] = "class1";
            item1_values[3] = "true";

            Object[] item2_values = new Object[4];
            item2_values[0] = 5.5;
            item2_values[1] = 9;
            item2_values[2] = "class1";
            item2_values[3] = "false";

            Object[] item3_values = new Object[4];
            item3_values[0] = 5.6;
            item3_values[1] = 8;
            item3_values[2] = "class1";
            item3_values[3] = "false";

            Object[] item4_values = new Object[4];
            item4_values[0] = 5.7;
            item4_values[1] = 8;
            item4_values[2] = "class2";
            item4_values[3] = "false";

            items.add(new Item(1, "item1", features, item1_values));
            items.add(new Item(2, "item2", features, item2_values));
            items.add(new Item(3, "item1", features, item3_values));
            items.add(new Item(4, "item1", features, item4_values));
        }

        //Users
        {
            users.add(new User(11));
            users.add(new User(12));
            users.add(new User(13));
            users.add(new User(14));
            users.add(new User(15));
        }
        //Ratins
        {
            ratings.add(new Rating(11, 1, 5));
            ratings.add(new Rating(11, 2, 5));
            ratings.add(new Rating(12, 3, 4));
            ratings.add(new Rating(12, 4, 4));
            ratings.add(new Rating(13, 2, 3));
            ratings.add(new Rating(13, 3, 4));
            ratings.add(new Rating(14, 1, 1));
            ratings.add(new Rating(14, 4, 4));
            ratings.add(new Rating(15, 1, 5));
        }

        this.ratingsDataset = new BothIndexRatingsDataset<>(ratings);
        this.contentDataset = new ContentDatasetDefault(items);
        this.usersDataset = new UsersDatasetAdapter(users);

    }

    @Override
    public RatingsDataset<Rating> getRatingsDataset() throws CannotLoadRatingsDataset {
        return ratingsDataset;
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {
        return contentDataset;
    }

    @Override
    public UsersDataset getUsersDataset() throws CannotLoadUsersDataset {
        return usersDataset;
    }

}
