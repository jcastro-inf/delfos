package delfos.group.casestudy.definedcases.estylf2014;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import delfos.dataset.loaders.movilens.ml100k.MovieLens100k;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.common.datastructures.histograms.RangeHistogram;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.group.casestudy.definedcases.jrs2014.CosineWithPenalty;
import delfos.group.casestudy.definedcases.jrs2014.CouldNotComputeTrust;
import delfos.group.casestudy.definedcases.jrs2014.PairwiseUserTrust;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 24-feb-2014
 */
public class SimilarityHistogram {

    @Test
    public void computeSimilarityHistogramOf_ml100k() throws UserNotFound {
        RangeHistogram histogram = new RangeHistogram(0, 1, 100);

        DatasetLoader datasetLoader = new MovieLens100k(new File("C:\\Dropbox\\Datasets\\MovieLens\\0 - MovieLens-100k ratings\\ml-100k"));

        PairwiseUserTrust trust = new CosineWithPenalty(1);

        List<Integer> users = new ArrayList<>(datasetLoader.getRatingsDataset().allUsers());
        Collections.sort(users);
        int i = 0;
        for (int idUser1 : users) {
            for (int idUser2 : users) {
                if (idUser1 >= idUser2) {
                    continue;
                }

                double trustValue;
                try {
                    trustValue = trust.getTrust(datasetLoader, idUser1, idUser2);
                } catch (CouldNotComputeTrust ex) {
                    trustValue = Double.NaN;
                }
                histogram.addValue(trustValue);
            }

            i++;

            double percent = ((i * 100.0) / users.size());
            System.out.println(percent + "% completed");
        }

        histogram.printHistogram(System.out);
    }

}
