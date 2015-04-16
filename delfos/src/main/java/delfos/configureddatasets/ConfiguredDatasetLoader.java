package delfos.configureddatasets;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.ObjectParameter;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;

/**
 *
 * @version 22-abr-2014
* @author Jorge Castro Gallardo
 */
public class ConfiguredDatasetLoader extends DatasetLoaderAbstract<Rating> implements ContentDatasetLoader, UsersDatasetLoader {

    public static final Parameter CONFIGURED_DATASET_NAME = new Parameter("CONFIGURED_DATASET_NAME", new ObjectParameter(ConfiguredDatasetsFactory.getInstance().keySet(), "ml-100k"));

    private String oldConfiguredDataset = "ml-100k";

    public ConfiguredDatasetLoader() {
        addParameter(CONFIGURED_DATASET_NAME);
        setAlias(getParameterValue(CONFIGURED_DATASET_NAME).toString());

        addParammeterListener(() -> {
            String newConfiguredDataset = (String) ConfiguredDatasetLoader.this.getParameterValue(CONFIGURED_DATASET_NAME);

            String newAlias = getAlias();

            if (!oldConfiguredDataset.equals(newConfiguredDataset)) {
                oldConfiguredDataset = newConfiguredDataset;
                setAlias(newConfiguredDataset);
            }
        });
    }

    public ConfiguredDatasetLoader(String identifier) {
        this();
        setParameterValue(CONFIGURED_DATASET_NAME, identifier);
        setAlias(identifier);
    }

    public DatasetLoader<? extends Rating> getDatasetLoader() {
        String name = getConfiguredDatasetName();
        return ConfiguredDatasetsFactory.getInstance().getDatasetLoader(name);
    }

    @Override
    public RatingsDataset<Rating> getRatingsDataset() throws CannotLoadRatingsDataset {
        return (RatingsDataset<Rating>) getDatasetLoader().getRatingsDataset();
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {
        if (!(getDatasetLoader() instanceof ContentDatasetLoader)) {
            String name = getConfiguredDatasetName();
            throw new IllegalArgumentException("The dataset loader " + name + " is not a content dataset loader");
        }
        return ((ContentDatasetLoader) getDatasetLoader()).getContentDataset();
    }

    @Override
    public UsersDataset getUsersDataset() throws CannotLoadUsersDataset {
        if (!(getDatasetLoader() instanceof UsersDatasetLoader)) {
            String name = getConfiguredDatasetName();
            throw new IllegalArgumentException("The dataset loader " + name + " is not a user dataset loader");
        }
        return ((UsersDatasetLoader) getDatasetLoader()).getUsersDataset();
    }

    public String getConfiguredDatasetName() {
        return (String) getParameterValue(CONFIGURED_DATASET_NAME);
    }

    @Override
    public String toString() {
        return ConfiguredDatasetLoader.class.getSimpleName();
    }
}
