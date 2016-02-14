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
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
