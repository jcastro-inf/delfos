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

    public static final Parameter EPINIONS_DATASET_FOLDER;

    private EPinionsRatingsDataset ratingsDataset = null;
    private EPinionsContentDataset contentDataset = null;
    private EPinionsTrustDataset trustDataset = null;

    static {
        String folder = Path.getDatasetDirectory();
        File epinionsDatasetFolder = new File(folder + File.separator + "epinions" + File.separator);
        EPINIONS_DATASET_FOLDER = new Parameter("EPINIONS_DATASET_FOLDER", new DirectoryParameter(epinionsDatasetFolder));
    }

    public EPinionsDatasetLoader() {
        addParameter(EPINIONS_DATASET_FOLDER);
    }

    @Override
    public EPinionsRatingsDataset getRatingsDataset() throws CannotLoadRatingsDataset {
        if (ratingsDataset == null) {
            try {
                File ratingsFile = new File(getDatasetFolder() + File.separator + "rating.txt");
                ratingsDataset = new EPinionsRatingsDataset(ratingsFile, getContentDataset());
            } catch (IOException ex) {
                throw new CannotLoadRatingsDataset(ex);
            }
        }

        return ratingsDataset;
    }

    public File getDatasetFolder() {
        return (File) getParameterValue(EPINIONS_DATASET_FOLDER);
    }

    @Override
    public EPinionsContentDataset getContentDataset() throws CannotLoadContentDataset {

        if (contentDataset == null) {
            try {
                File contentFile = new File(getDatasetFolder() + File.separator + "mc.txt");
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
                File trustFile = new File(getDatasetFolder() + File.separator + "user_rating.txt");
                trustDataset = new EPinionsTrustDataset(trustFile, getRatingsDataset().getUsersIndex());
            } catch (IOException ex) {
                throw new CannotLoadTrustDataset(ex);
            }
        }

        return trustDataset;
    }
}
