package delfos.dataset.loaders.epinions;

import java.io.File;
import java.io.IOException;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.TrustDatasetLoader;
import delfos.Path;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadTrustDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.DirectoryParameter;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 10-dic-2013
 */
public class EPinionsDatasetLoader extends DatasetLoaderAbstract implements ContentDatasetLoader, TrustDatasetLoader {

    public static final Parameter EPINIONS_DATASET_DIRECTORY;

    private EPinionsRatingsDataset ratingsDataset = null;
    private EPinionsContentDataset contentDataset = null;
    private EPinionsTrustDataset trustDataset = null;

    static {
        String directory = Path.getDatasetDirectory();
        File epinionsDatasetDirectory = new File(directory + File.separator + "epinions" + File.separator);
        EPINIONS_DATASET_DIRECTORY = new Parameter("EPINIONS_DATASET_DIRECTORY", new DirectoryParameter(epinionsDatasetDirectory));
    }

    public EPinionsDatasetLoader() {
        addParameter(EPINIONS_DATASET_DIRECTORY);
    }

    @Override
    public EPinionsRatingsDataset getRatingsDataset() throws CannotLoadRatingsDataset {
        if (ratingsDataset == null) {
            try {
                File ratingsFile = new File(getDatasetDirectory() + File.separator + "rating.txt");
                ratingsDataset = new EPinionsRatingsDataset(ratingsFile, getContentDataset());
            } catch (IOException ex) {
                throw new CannotLoadRatingsDataset(ex);
            }
        }

        return ratingsDataset;
    }

    public File getDatasetDirectory() {
        return (File) getParameterValue(EPINIONS_DATASET_DIRECTORY);
    }

    @Override
    public EPinionsContentDataset getContentDataset() throws CannotLoadContentDataset {

        if (contentDataset == null) {
            try {
                File contentFile = new File(getDatasetDirectory() + File.separator + "mc.txt");
                contentDataset = new EPinionsContentDataset(contentFile);
            } catch (IOException ex) {
                throw new CannotLoadRatingsDataset(ex);
            }
        }

        return contentDataset;
    }

    @Override
    public EPinionsTrustDataset getTrustDataset() throws CannotLoadTrustDataset {
        if (trustDataset == null) {
            try {
                File trustFile = new File(getDatasetDirectory() + File.separator + "user_rating.txt");
                trustDataset = new EPinionsTrustDataset(trustFile, getRatingsDataset().getUsersIndex());
            } catch (IOException ex) {
                throw new CannotLoadTrustDataset(ex);
            }
        }

        return trustDataset;
    }
}
