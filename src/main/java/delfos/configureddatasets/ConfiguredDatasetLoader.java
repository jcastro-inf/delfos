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
package delfos.configureddatasets;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import delfos.common.parameters.restriction.ObjectParameter;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.UsersDataset;

/**
 *
 * @version 22-abr-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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

    @Override
    public boolean isSameClass(ParameterOwner parameterOwner) {
        if (parameterOwner instanceof ConfiguredDatasetLoader) {
            ConfiguredDatasetLoader configuredDatasetLoader = (ConfiguredDatasetLoader) parameterOwner;
            return this.getConfiguredDatasetName().equals(configuredDatasetLoader.getConfiguredDatasetName());
        } else {
            return super.isSameClass(parameterOwner); //To change body of generated methods, choose Tools | Templates.
        }
    }

}
