package delfos.results.evaluationmeasures;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;

/**
 * Dataset para testear las metricas de evaluación.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 19-feb-2014
 */
public class RatingsDatasetMock extends BothIndexRatingsDataset<Rating> {

    public RatingsDatasetMock() {
        super();

        /*
         *     | I_10 | I_11 | I_12 | I_13 |
         * U_1 |   4  |   -  |   5  |   5  |
         * U_2 |   -  |   1  |   2  |   1  |
         * U_3 |   5  |   -  |   -  |   1  |
         * U_4 |   3  |   3  |   3  |   3  |
         * U_5 |   -  |   4  |   -  |   -  |
         */
        addOneRating(new Rating(1, 10, 4));
        addOneRating(new Rating(1, 12, 5));
        addOneRating(new Rating(1, 13, 5));

        addOneRating(new Rating(2, 11, 1));
        addOneRating(new Rating(2, 12, 2));
        addOneRating(new Rating(2, 13, 1));

        addOneRating(new Rating(3, 10, 5));
        addOneRating(new Rating(3, 13, 1));

        addOneRating(new Rating(4, 10, 3));
        addOneRating(new Rating(4, 11, 3));
        addOneRating(new Rating(4, 12, 3));
        addOneRating(new Rating(4, 13, 3));

        addOneRating(new Rating(5, 11, 4));
    }

}
