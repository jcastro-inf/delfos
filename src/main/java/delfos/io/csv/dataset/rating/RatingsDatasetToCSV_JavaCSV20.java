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

import com.csvreader.CsvReader;
import delfos.common.Chronometer;
import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
public class RatingsDatasetToCSV_JavaCSV20 implements RatingsDatasetToCSV {

    private final char stringSeparator;
    private final char fieldSeparator;
    private final char rowSeparator;

    public RatingsDatasetToCSV_JavaCSV20() {
        this('"', ',', '\n');
    }

    public RatingsDatasetToCSV_JavaCSV20(char fieldSeparator) {
        this('"', ',', fieldSeparator);
    }

    public RatingsDatasetToCSV_JavaCSV20(char fieldSeparator, char rowSeparator) {
        this('"', fieldSeparator, rowSeparator);
    }

    public RatingsDatasetToCSV_JavaCSV20(char stringSeparator, char fieldSeparator, char rowSeparator) {
        this.stringSeparator = stringSeparator;
        this.fieldSeparator = fieldSeparator;
        this.rowSeparator = rowSeparator;
    }

    @Override
    public <RatingType extends Rating> void writeDataset(RatingsDataset<RatingType> ratingsDataset, String fileName) throws IOException {

        String fileNameWithExtension = fileName;
        if (!fileNameWithExtension.endsWith(".csv")) {
            fileNameWithExtension = fileNameWithExtension + "." + CSV_EXTENSION;
        }
        final File fileWithExtension = new File(fileNameWithExtension);

        FileUtilities.createDirectoriesForFileIfNotExist(fileWithExtension);

        BufferedWriter bw = new BufferedWriter(new FileWriter(fileWithExtension));

        bw.write(stringSeparator + ID_USER_COLUMN_NAME + stringSeparator
                + fieldSeparator
                + stringSeparator + ID_ITEM_COLUMN_NAME + stringSeparator
                + fieldSeparator
                + stringSeparator + RATING_COLUMN_NAME + stringSeparator
                + rowSeparator);

        for (RatingType r : ratingsDataset) {
            String idUser = ((Number) r.getIdUser()).toString();
            String idItem = ((Number) r.getIdItem()).toString();
            String rating = r.getRatingValue().toString();

            bw.write(idUser + fieldSeparator + idItem + fieldSeparator + rating + rowSeparator);
        }
        bw.close();
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
        reader.setDelimiter(',');

        try {
            reader.readHeaders();
            checkHeaders(reader.getHeaders());
        } catch (CannotLoadRatingsDataset | IOException ex) {
            throw new CannotLoadRatingsDataset(
                    ex.getMessage() + "[File '" + ratingsFile.getAbsolutePath() + "']",
                    ex
            );
        }

        Chronometer c = new Chronometer();
        c.reset();
        int i = 0;
        try {
            while (reader.readRecord()) {
                try {
                    int idUser = Integer.parseInt(reader.get("idUser"));
                    int idItem = Integer.parseInt(reader.get("idItem"));
                    Number rating = Double.parseDouble(reader.get("rating"));
                    ratings.add(new Rating(idUser, idItem, rating));

                    if (i % 1000000 == 0 && i != 0) {
                        Global.showInfoMessage("Loading CSV --> " + i / 1000000 + " millions ratings " + c.printPartialElapsed() + " / " + c.printTotalElapsed() + "\n");
                        c.setPartialEllapsedCheckpoint();
                    }

                    i++;
                } catch (Throwable ex) {
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

    private void checkHeaders(String[] headers) {
        checkHeaderPresent(headers, "idUser");
        checkHeaderPresent(headers, "idItem");
        checkHeaderPresent(headers, "rating");
    }

    private void checkHeaderPresent(String[] headers, String headerThatMustBePresent) {
        boolean isHeaderPresent = false;
        for (String header : headers) {
            if (header.equals(headerThatMustBePresent)) {
                isHeaderPresent = true;
            }
        }

        if (!isHeaderPresent) {
            throw new CannotLoadRatingsDataset("Header '" + headerThatMustBePresent + "' is not present");
        }
    }

}
