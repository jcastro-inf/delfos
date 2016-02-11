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
package delfos.dataset.generated.modifieddatasets;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.DatasetLoaderParameterRestriction;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.loaders.csv.CSVfileDatasetLoader;
import delfos.experiment.SeedHolder;

/**
 * Clase que encapsula el funcionamiento de un dataset que se encarga de reducir
 * los datos de un dataset original.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 ( 10/04/2012 )
 */
public abstract class DatasetSampler extends DatasetLoaderAbstract implements SeedHolder {

    /**
     * Parámetro que almacena el dataset original.
     */
    public static final Parameter ORIGINAL_DATASET_PARAMETER = new Parameter("Original_dataset", new DatasetLoaderParameterRestriction(new CSVfileDatasetLoader()));

    public DatasetSampler() {
        super();
        addParameter(ORIGINAL_DATASET_PARAMETER);
        init();
    }

    public DatasetSampler(DatasetLoader<? extends Rating> originalDataset) {
        this();
        setParameterValue(ORIGINAL_DATASET_PARAMETER, originalDataset);
    }

    /**
     * Método que indica al dataset que debe volver a reducir los datos,
     * cambiando los datos que se seleccionan del dataset original (si procede)
     */
    public abstract void shuffle() throws CannotLoadRatingsDataset;

    public RatingsDataset<? extends Rating> getOriginalDataset() {
        return (RatingsDataset<? extends Rating>) getParameterValue(ORIGINAL_DATASET_PARAMETER);
    }

    @Override
    public void setSeedValue(long seedValue) {
        setParameterValue(SEED, seedValue);
    }

    @Override
    public long getSeedValue() {
        return (Long) getParameterValue(SEED);
    }

    private void init() {
        addParameter(SEED);
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {
        getRatingsDataset();

        if (getOriginalDataset() instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) getOriginalDataset();
            return contentDatasetLoader.getContentDataset();
        } else {
            throw new CannotLoadContentDataset("The dataset loader is not a ContentDatasetLoader, cannot apply a content-based ");
        }

    }

    @Override
    public UsersDataset getUsersDataset() throws CannotLoadUsersDataset {
        getRatingsDataset();

        if (getOriginalDataset() instanceof UsersDatasetLoader) {
            UsersDatasetLoader usersDatasetLoader = (UsersDatasetLoader) getOriginalDataset();

            return usersDatasetLoader.getUsersDataset();
        } else {
            throw new CannotLoadContentDataset("The dataset loader is not a ContentDatasetLoader, cannot apply a content-based ");
        }

    }
}
