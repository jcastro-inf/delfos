package delfos.configureddatasets;

import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadTrustDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.configuration.scopes.ConfiguredDatasetsScope;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.TrustDatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.view.configureddatasets.NewConfiguredDatasetDialog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.JFrame;

/**
 * Factoría que devuelve datasets ya creados, es decir, datasets que funcionan
 * correctamente en la máquina local.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 13-mar-2014
 */
public final class ConfiguredDatasetsFactory {

    private static ConfiguredDatasetsFactory instance;

    private final Map<String, ConfiguredDataset> datasetLoaders;

    private final List<ConfiguredDatasetsListener> listeners = new ArrayList<>();

    public void addConfiguredDatasetsListener(ConfiguredDatasetsListener listener) {
        listeners.add(listener);
        listener.configuredDatasetsChanged();
    }

    public void removeConfiguredDatasetsListener(ConfiguredDatasetsListener listener) {
        listeners.remove(listener);
    }

    /**
     * Function to notify all listeners that there are changes in the list of
     * configured datasets.
     */
    private void notifyConfiguredDatasetsChanged() {
        for (ConfiguredDatasetsListener listener : listeners) {
            listener.configuredDatasetsChanged();
        }
    }

    private ConfiguredDatasetsFactory() {
        datasetLoaders = new TreeMap<>();
    }

    public static ConfiguredDatasetsFactory getInstance() {
        if (instance == null) {
            instance = new ConfiguredDatasetsFactory();
            ConfiguredDatasetsScope.getInstance().loadConfigurationScope();
        }
        return instance;
    }

    public void showCreateConfiguredDatasetDialog(JFrame frame) {
        NewConfiguredDatasetDialog configuredDatasetDialog = new NewConfiguredDatasetDialog(frame, true);
        configuredDatasetDialog.setVisible(true);
    }

    public void addDatasetLoader(String name, String description, DatasetLoader<? extends Rating> datasetLoader) {

        addDatasetLoaderNoNotifyChanges(name, description, datasetLoader);

        notifyConfiguredDatasetsChanged();
    }

    private void addDatasetLoaderNoNotifyChanges(String name, String description, DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadUsersDataset, IllegalArgumentException, CannotLoadRatingsDataset, CannotLoadTrustDataset, CannotLoadContentDataset {
        //Compruebo que el dataset funciona.
        datasetLoader.getRatingsDataset();
        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            contentDatasetLoader.getContentDataset();
        }

        if (datasetLoader instanceof UsersDatasetLoader) {
            UsersDatasetLoader usersDatasetLoader = (UsersDatasetLoader) datasetLoader;
            usersDatasetLoader.getUsersDataset();
        }

        if (datasetLoader instanceof TrustDatasetLoader) {
            TrustDatasetLoader trustDatasetLoader = (TrustDatasetLoader) datasetLoader;
            trustDatasetLoader.getTrustDataset();
        }

        if (datasetLoaders.containsKey(name)) {
            if (!datasetLoaders.get(name).getDatasetLoader().equals(datasetLoader)) {
                throw new IllegalArgumentException("The identifier '" + name + "' for datasets is already in use.");
            }
        } else {
            datasetLoaders.put(name, new ConfiguredDataset(name, description, datasetLoader));
        }

    }

    public void removeDatasetLoader(String identifier) {
        if (datasetLoaders.containsKey(identifier)) {
            datasetLoaders.remove(identifier);
        }
        notifyConfiguredDatasetsChanged();
    }

    public DatasetLoader<? extends Rating> getDatasetLoader(String identifier) {
        return getDatasetLoader(identifier, DatasetLoader.class);
    }

    public String getDatasetLoaderDescription(String identifier) {
        if (datasetLoaders.isEmpty()) {
            Global.showWarning("No configured datasets found, check configuration file.");
        }

        if (datasetLoaders.containsKey(identifier)) {
            return datasetLoaders.get(identifier).getDescription();
        } else {
            throw new IllegalArgumentException("Configured dataset with identifier '" + identifier + "' not defined.");
        }
    }

    public <DatasetLoaderType> DatasetLoaderType getDatasetLoader(String identifier, Class<DatasetLoaderType> clase) {
        if (datasetLoaders.isEmpty()) {
            Global.showWarning("No configured datasets found, check configuration file.");
        }

        if (datasetLoaders.containsKey(identifier)) {
            ConfiguredDataset configuredDataset = datasetLoaders.get(identifier);

            DatasetLoader datasetLoader = configuredDataset.getDatasetLoader();
            if (clase.isInstance(datasetLoader)) {
                return (DatasetLoaderType) datasetLoader;
            } else {
                throw new IllegalArgumentException("The dataset loader does not matches type '" + clase + "'.");
            }
        } else {
            throw new IllegalArgumentException("Configured dataset with identifier '" + identifier + "' not defined.");
        }
    }

    /**
     * Devuelve los identificadores de los dataset configurados definidos.
     *
     * @return
     */
    public Set<String> keySet() {
        return datasetLoaders.keySet();
    }

    public Collection<DatasetLoader> getAllConfiguredDatasetLoaders() {
        Collection<DatasetLoader> ret = new ArrayList<>();
        for (ConfiguredDataset datasetLoader : datasetLoaders.values()) {
            ret.add(datasetLoader.getDatasetLoader());
        }
        return ret;
    }

    public Collection<ConfiguredDataset> getAllConfiguredDatasets() {
        return datasetLoaders.values();
    }

    public void setAllConfiguredDatasets(Collection<ConfiguredDataset> configuredDatasets) {

        datasetLoaders.clear();
        for (ConfiguredDataset configuredDataset : configuredDatasets) {
            addDatasetLoaderNoNotifyChanges(configuredDataset.getName(), configuredDataset.getDescription(), configuredDataset.getDatasetLoader());
        }

        notifyConfiguredDatasetsChanged();
    }
}
