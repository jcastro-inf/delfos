package delfos.io.csv.dataset.rating;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.io.csv.dataset.item.ContentDatasetToCSV;
import delfos.io.csv.dataset.user.UsersDatasetToCSV;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 12-mar-2014
 */
public interface RatingsDatasetToCSV {

    public static final String CSV_EXTENSION = "csv";
    public static final String ID_ITEM_COLUMN_NAME = ContentDatasetToCSV.ID_ITEM_COLUMN_NAME;
    public static final String ID_USER_COLUMN_NAME = UsersDatasetToCSV.ID_USER_COLUMN_NAME;
    public static final String RATING_COLUMN_NAME = "rating";

    public Collection<Rating> readRatingsDataset(File ratingsFile) throws CannotLoadRatingsDataset, FileNotFoundException;

    public <RatingType extends Rating> void writeDataset(RatingsDataset<RatingType> ratingsDataset, String fileName) throws IOException;

}
