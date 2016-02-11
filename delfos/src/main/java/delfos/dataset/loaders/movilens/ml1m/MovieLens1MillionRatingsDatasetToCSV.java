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
package delfos.dataset.loaders.movilens.ml1m;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import delfos.common.Chronometer;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.dataset.basic.rating.RatingWithTimestamp;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.io.csv.dataset.rating.CSVReader;
import static delfos.io.csv.dataset.rating.RatingsDatasetToCSV.CSV_EXTENSION;
import static delfos.io.csv.dataset.rating.RatingsDatasetToCSV.ID_ITEM_COLUMN_NAME;
import static delfos.io.csv.dataset.rating.RatingsDatasetToCSV.ID_USER_COLUMN_NAME;
import static delfos.io.csv.dataset.rating.RatingsDatasetToCSV.RATING_COLUMN_NAME;

/**
 * Clase para escribir un dataset de valoraciones a fichero csv.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 04-Mar-2013
 */
public class MovieLens1MillionRatingsDatasetToCSV {

    private final String stringSeparator;
    private final String fieldSeparator;
    private final String rowSeparator;

    public MovieLens1MillionRatingsDatasetToCSV() {
        this("\"", ",", "\n");
    }

    public MovieLens1MillionRatingsDatasetToCSV(String fieldSeparator) {
        this("\"", ",", fieldSeparator);
    }

    public MovieLens1MillionRatingsDatasetToCSV(String fieldSeparator, String rowSeparator) {
        this("\"", fieldSeparator, rowSeparator);
    }

    public MovieLens1MillionRatingsDatasetToCSV(String stringSeparator, String fieldSeparator, String rowSeparator) {
        this.stringSeparator = stringSeparator;
        this.fieldSeparator = fieldSeparator;
        this.rowSeparator = rowSeparator;
    }

    public void writeDataset(RatingsDataset<RatingWithTimestamp> ratingsDataset, String fileName) throws IOException {

        String fileNameWithExtension = fileName;
        if (!fileNameWithExtension.endsWith(".csv")) {
            fileNameWithExtension = fileNameWithExtension + "." + CSV_EXTENSION;
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileNameWithExtension)));

        bw.write(stringSeparator + ID_USER_COLUMN_NAME + stringSeparator
                + fieldSeparator
                + stringSeparator + ID_ITEM_COLUMN_NAME + stringSeparator
                + fieldSeparator
                + stringSeparator + RATING_COLUMN_NAME + stringSeparator
                + rowSeparator);

        for (RatingWithTimestamp r : ratingsDataset) {
            String idUser = ((Number) r.getIdUser()).toString();
            String idItem = ((Number) r.getIdItem()).toString();
            String rating = r.getRatingValue().toString();

            bw.write(idUser + fieldSeparator + idItem + fieldSeparator + rating + rowSeparator);
        }
        bw.flush();
        bw.close();
    }

    public Collection<RatingWithTimestamp> readRatingsDataset(File ratingsFile) throws CannotLoadRatingsDataset, FileNotFoundException {

        Global.showInfoMessage(
                this.getClass().getSimpleName() + ": "
                + "Loading ratings dataset from " + ratingsFile.getAbsolutePath() + " (no header)\n");

        Collection<RatingWithTimestamp> ratings = new ArrayList<RatingWithTimestamp>();

        try {
            CSVReader reader = new CSVReader(ratingsFile, "\"", "::");

            Chronometer c = new Chronometer();
            c.reset();
            int i = 0;
            while (reader.readRecord()) {
                try {
                    int idUser = Integer.parseInt(reader.get(0));
                    int idItem = Integer.parseInt(reader.get(1));
                    Number rating = Float.parseFloat(reader.get(2));
                    long timestamp = Long.parseLong(reader.get(3));
                    ratings.add(new RatingWithTimestamp(idUser, idItem, rating, timestamp));

                    if (i % 1000000 == 0 && i != 0) {
                        Global.showInfoMessage("Loading  --> " + i / 1000000 + " millions ratings " + c.printPartialElapsed() + " / " + c.printTotalElapsed() + "\n");
                        c.setPartialEllapsedCheckpoint();
                    }

                    i++;
                } catch (Throwable ex) {
                    Global.showError(ex);
                    Global.showWarning("Raw record  '" + reader.getRawRecord() + "' line " + (i + 1) + "\n");
                }
            }
            reader.close();
        } catch (IOException ex) {
            throw new CannotLoadRatingsDataset(ex);
        }

        return ratings;
    }
}
