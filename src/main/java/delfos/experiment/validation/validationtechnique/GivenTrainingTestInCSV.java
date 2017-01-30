/*
 * Copyright (C) 2017 jcastro
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
package delfos.experiment.validation.validationtechnique;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.filefilters.FileFilterByExtension;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.FileParameter;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.io.csv.dataset.rating.RatingsDatasetToCSV_JavaCSV20;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class GivenTrainingTestInCSV extends ValidationTechnique {

    public static final Parameter TRAINING_FILE = new Parameter(
            "TRAINING_FILE",
            new FileParameter(
                    new File("temp" + File.separator + "training.csv"),
                    new FileFilterByExtension(true, "csv")));

    public static final Parameter TEST_FILE = new Parameter(
            "TEST_FILE",
            new FileParameter(
                    new File("temp" + File.separator + "test.csv"),
                    new FileFilterByExtension(true, "csv")));

    public GivenTrainingTestInCSV() {

        super();

        addParameter(TRAINING_FILE);
        addParameter(TEST_FILE);
    }

    @Override
    public <RatingType extends Rating> PairOfTrainTestRatingsDataset[] shuffle(DatasetLoader<RatingType> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {

        try {

            PairOfTrainTestRatingsDataset[] pairOfTrainTestRatingsDataset
                    = new PairOfTrainTestRatingsDataset[5];

            pairOfTrainTestRatingsDataset[0] = new PairOfTrainTestRatingsDataset(
                    datasetLoader,
                    new BothIndexRatingsDataset<>(getRatings(new File("ratings_train_0.csv"), datasetLoader)),
                    new BothIndexRatingsDataset<>(getRatings(new File("ratings_test_0.csv"), datasetLoader)),
                    GivenTrainingTestInCSV.class.getSimpleName() + " using "
                    + getTrainingFile().getAbsolutePath()
                    + " and "
                    + getTestFile().getAbsolutePath());

            pairOfTrainTestRatingsDataset[1] = new PairOfTrainTestRatingsDataset(
                    datasetLoader,
                    new BothIndexRatingsDataset<>(getRatings(new File("ratings_train_1.csv"), datasetLoader)),
                    new BothIndexRatingsDataset<>(getRatings(new File("ratings_test_1.csv"), datasetLoader)),
                    GivenTrainingTestInCSV.class.getSimpleName() + " using "
                    + getTrainingFile().getAbsolutePath()
                    + " and "
                    + getTestFile().getAbsolutePath());

            pairOfTrainTestRatingsDataset[2] = new PairOfTrainTestRatingsDataset(
                    datasetLoader,
                    new BothIndexRatingsDataset<>(getRatings(new File("ratings_train_2.csv"), datasetLoader)),
                    new BothIndexRatingsDataset<>(getRatings(new File("ratings_test_2.csv"), datasetLoader)),
                    GivenTrainingTestInCSV.class.getSimpleName() + " using "
                    + getTrainingFile().getAbsolutePath()
                    + " and "
                    + getTestFile().getAbsolutePath());

            pairOfTrainTestRatingsDataset[3] = new PairOfTrainTestRatingsDataset(
                    datasetLoader,
                    new BothIndexRatingsDataset<>(getRatings(new File("ratings_train_3.csv"), datasetLoader)),
                    new BothIndexRatingsDataset<>(getRatings(new File("ratings_test_3.csv"), datasetLoader)),
                    GivenTrainingTestInCSV.class.getSimpleName() + " using "
                    + getTrainingFile().getAbsolutePath()
                    + " and "
                    + getTestFile().getAbsolutePath());

            pairOfTrainTestRatingsDataset[4] = new PairOfTrainTestRatingsDataset(
                    datasetLoader,
                    new BothIndexRatingsDataset<>(getRatings(new File("ratings_train_4.csv"), datasetLoader)),
                    new BothIndexRatingsDataset<>(getRatings(new File("ratings_test_4.csv"), datasetLoader)),
                    GivenTrainingTestInCSV.class.getSimpleName() + " using "
                    + getTrainingFile().getAbsolutePath()
                    + " and "
                    + getTestFile().getAbsolutePath());

            return pairOfTrainTestRatingsDataset;
        } catch (FileNotFoundException ex) {
            throw new CannotLoadRatingsDataset(ex);
        }
    }

    public static <RatingType extends Rating> Collection<RatingType> getRatings(
            File file, DatasetLoader<RatingType> datasetLoader) throws FileNotFoundException, CannotLoadRatingsDataset {

        RatingsDatasetToCSV_JavaCSV20 ratingsDatasetToCSV = new RatingsDatasetToCSV_JavaCSV20();
        ContentDataset contentDataset = datasetLoader.getContentDataset();
        UsersDataset usersDataset = datasetLoader.getUsersDataset();

        Collection<RatingType> ratings = ratingsDatasetToCSV
                .readRatingsDataset(file)
                .parallelStream()
                .map(rating -> {

                    Item item = contentDataset.getItem(rating.getIdItem());
                    User user = usersDataset.getUser(rating.getIdUser());
                    return (RatingType) rating.copyWithItem(item)
                            .copyWithUser(user);

                })
                .collect(Collectors.toList());
        return ratings;
    }

    @Override
    public int getNumberOfSplits() {
        return 5;
    }

    public File getTrainingFile() {
        return (File) getParameterValue(TRAINING_FILE);
    }

    public File getTestFile() {
        return (File) getParameterValue(TEST_FILE);
    }

}
