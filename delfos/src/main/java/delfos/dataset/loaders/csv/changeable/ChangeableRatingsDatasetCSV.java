package delfos.dataset.loaders.csv.changeable;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.changeable.ChangeableRatingsDataset;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.ERROR_CODES;
import delfos.io.csv.dataset.rating.RatingsDatasetToCSV;
import delfos.io.csv.dataset.rating.RatingsDatasetToCSV_JavaCSV20;
import delfos.common.parameters.ParameterListener;

/**
 * Implementa un dataset de valoraciones modificable sobre fichero CSV.
 *
* @author Jorge Castro Gallardo
 *
 * @version 16-sep-2013
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
                } else {
                    if (!usersDatasetFile.equals(parent.getUsersDatasetFile())) {
                        commitChangesInPersistence();
                    }
                }
            }
        });
    }

    public ChangeableRatingsDatasetCSV(final ChangeableCSVFileDatasetLoader parent, Iterable<RatingType> ratings) {
        super(ratings);
        this.parent = parent;

        parent.addParammeterListener(new ParameterListener() {
            private File usersDatasetFile = null;

            @Override
            public void parameterChanged() {
                if (usersDatasetFile == null) {
                    usersDatasetFile = parent.getUsersDatasetFile();
                } else {
                    if (!usersDatasetFile.equals(parent.getUsersDatasetFile())) {
                        commitChangesInPersistence();
                    }
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
