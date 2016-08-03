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
package delfos.dataset.loaders.csv.changeable;

import delfos.ERROR_CODES;
import delfos.common.parameters.ParameterListener;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.changeable.ChangeableRatingsDataset;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.io.csv.dataset.rating.RatingsDatasetToCSV;
import delfos.io.csv.dataset.rating.RatingsDatasetToCSV_JavaCSV20;
import static delfos.utils.streams.IteratorToList.collectInList;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Implementa un dataset de valoraciones modificable sobre fichero CSV.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 16-sep-2013
 * @param <RatingType>
 */
public class ChangeableRatingsDatasetCSV<RatingType extends Rating> extends BothIndexRatingsDataset<RatingType> implements ChangeableRatingsDataset<RatingType> {

    private final ChangeableCSVFileDatasetLoader parent;

    public ChangeableRatingsDatasetCSV(final ChangeableCSVFileDatasetLoader parent) {
        this.parent = parent;
        parent.addParammeterListener(new ParameterListener() {
            private File usersDatasetFile = null;

            @Override
            public void parameterChanged() {
                if (usersDatasetFile == null) {
                    usersDatasetFile = parent.getUsersDatasetFile();
                } else if (!usersDatasetFile.equals(parent.getUsersDatasetFile())) {
                    commitChangesInPersistence();
                }
            }
        });
    }

    public ChangeableRatingsDatasetCSV(final ChangeableCSVFileDatasetLoader parent, Iterable<RatingType> ratings) {
        this(parent, collectInList(ratings));
    }

    public ChangeableRatingsDatasetCSV(final ChangeableCSVFileDatasetLoader parent, Collection<RatingType> ratings) {
        super(ratings);
        this.parent = parent;

        parent.addParammeterListener(new ParameterListener() {
            private File usersDatasetFile = null;

            @Override
            public void parameterChanged() {
                if (usersDatasetFile == null) {
                    usersDatasetFile = parent.getUsersDatasetFile();
                } else if (!usersDatasetFile.equals(parent.getUsersDatasetFile())) {
                    commitChangesInPersistence();
                }
            }
        });
    }

    @Override
    public void addRating(int idUser, int idItem, RatingType ratingValue) {
        super.addOneRating(ratingValue);
    }

    @Override
    public void removeRating(int idUser, int idItem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void commitChangesInPersistence() {
        try {
            RatingsDatasetToCSV ratingsDatasetToCSV = new RatingsDatasetToCSV_JavaCSV20();
            ratingsDatasetToCSV.writeDataset(this, parent.getRatingsDatasetFile().getAbsolutePath());
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_RATINGS_DATASET.exit(ex);
        }
    }
}
