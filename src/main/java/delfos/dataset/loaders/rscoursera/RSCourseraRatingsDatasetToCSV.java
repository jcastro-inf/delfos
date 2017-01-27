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
package delfos.dataset.loaders.rscoursera;

import com.csvreader.CsvReader;
import delfos.common.Chronometer;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.io.csv.dataset.rating.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Clase para escribir un dataset de valoraciones a fichero csv.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 04-Mar-2013
 */
public class RSCourseraRatingsDatasetToCSV implements RatingsDatasetToCSV {

    private final int userColumn = 0;
    private final int itemColumn = 1;
    private final int ratingColumn = 2;

    public RSCourseraRatingsDatasetToCSV() {
    }

    @Override
    public <RatingType extends Rating> void writeDataset(RatingsDataset<RatingType> ratingsDataset, String fileName) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Collection<Rating> readRatingsDataset(File ratingsFile) throws CannotLoadRatingsDataset, FileNotFoundException {

        Global.showInfoMessage("Loading ratings dataset from " + ratingsFile.getAbsolutePath() + "\n");
        String ratingsCSV = ratingsFile.getAbsolutePath();

        Collection<Rating> ratings = new ArrayList<>();

        CsvReader reader = new CsvReader(
                new FileInputStream(ratingsCSV),
                Charset.forName("UTF-8"));
        Global.showInfoMessage("Loading CSV file" + "\n");
        reader.setRecordDelimiter('\n');
        reader.setDelimiter(',');

        Chronometer c = new Chronometer();
        c.reset();
        int i = 0;
        try {
            while (reader.readRecord()) {
                try {
                    int idUser = Integer.parseInt(reader.get(userColumn));
                    int idItem = Integer.parseInt(reader.get(itemColumn));
                    Number rating = Float.parseFloat(reader.get(ratingColumn));
                    ratings.add(new Rating(idUser, idItem, rating));

                    if (i % 1000000 == 0 && i != 0) {
                        Global.showInfoMessage("Loading CSV --> " + i / 1000000 + " millions ratings " + c.printPartialElapsed() + " / " + c.printTotalElapsed() + "\n");
                        c.setPartialEllapsedCheckpoint();
                    }

                    i++;
                } catch (IOException | NumberFormatException ex) {
                    Global.showError(ex);
                    Global.showWarning("Raw record  '" + reader.getRawRecord() + "' line " + (i + 1) + "\n");
                }
            }
        } catch (IOException ex) {
            throw new CannotLoadRatingsDataset(ex);
        }
        reader.close();

        return ratings;
    }

}
