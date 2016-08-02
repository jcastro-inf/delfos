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
package delfos.dataset.loaders.bookcrossing;

import delfos.constants.DelfosTest;
import delfos.rs.collaborativefiltering.Recommender_DatasetProperties;
import java.io.File;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class BookCrossingDatasetLoaderTest extends DelfosTest {

    public BookCrossingDatasetLoaderTest() {
    }

    /**
     * Test of EPinionsDatasetLoader method, of class BookCrossingDatasetLoader.
     */
    @Test
    public void testEPinionsDatasetLoader() {

        System.out.println("EPinionsDatasetLoader");

        File datasetDirectory = new File("/home/jcastro/Dropbox/Datasets-new/Book-Cross/BX-CSV-Dump");

        BookCrossingDatasetLoader bookCrossingDataset = new BookCrossingDatasetLoader()
                .setDatasetDirectory(datasetDirectory);

        Recommender_DatasetProperties recommender_DatasetProperties = new Recommender_DatasetProperties();

        recommender_DatasetProperties.buildRecommendationModel(bookCrossingDataset);
    }
}
