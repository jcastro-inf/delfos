package delfos.dataset.memory.validationdatasets;

import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 27-feb-2014
 */
public class MockDatasetLoader_ValidationDatasets extends DatasetLoaderAbstract<Rating> {

    private RatingsDataset<Rating> ratingDataset;

    public MockDatasetLoader_ValidationDatasets() {
    }

    @Override
    public RatingsDataset<Rating> getRatingsDataset() throws CannotLoadRatingsDataset {
        if (ratingDataset == null) {
            createRatingsDataset();
        }

        return ratingDataset;
    }

    private void createRatingsDataset() {
        List<Rating> ratings = new ArrayList<>();

        ratings.add(new Rating(1, 11, 5));
        ratings.add(new Rating(1, 12, 4));
        ratings.add(new Rating(1, 13, 3));
        ratings.add(new Rating(1, 14, 2));
        ratings.add(new Rating(1, 15, 1));
        ratings.add(new Rating(2, 11, 5));
        ratings.add(new Rating(2, 12, 4));
        ratings.add(new Rating(3, 13, 3));
        ratings.add(new Rating(3, 14, 2));
        ratings.add(new Rating(3, 15, 1));

        ratingDataset = new BothIndexRatingsDataset<>(ratings);
    }

}
