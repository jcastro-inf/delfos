package delfos.configureddatasets;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 19-mar-2014
 */
public class ConfiguredDataset {

    private final String name;
    private final String description;
    private final DatasetLoader<? extends Rating> datasetLoader;

    public ConfiguredDataset(String name, String description, DatasetLoader<? extends Rating> datasetLoader) {
        this.name = name;
        this.description = description;
        this.datasetLoader = datasetLoader;
        datasetLoader.setAlias(name);
    }

    public DatasetLoader<? extends Rating> getDatasetLoader() {
        return datasetLoader;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

}
