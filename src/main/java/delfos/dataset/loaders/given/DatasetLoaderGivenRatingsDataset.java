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
package delfos.dataset.loaders.given;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.loader.types.CompleteDatasetLoaderAbstract_withTrust;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.UsersDataset;

/**
 * Dataset loader a partir de los datasets de contenido y de valoraciones.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version Unknown date
 * @version 26-Noviembre-2013
 * @param <RatingType>
 */
public class DatasetLoaderGivenRatingsDataset<RatingType extends Rating> extends CompleteDatasetLoaderAbstract_withTrust<RatingType> {

    private static final long serialVersionUID = 1L;
    private final DatasetLoader<? extends Rating> datasetLoader;
    private final RatingsDataset<RatingType> ratingsDataset;

    /**
     * Replaces the original ratings dataset in the dataset loader for the one
     * provided.
     *
     * @param datasetLoader
     * @param ratingsDataset
     */
    public DatasetLoaderGivenRatingsDataset(DatasetLoader<? extends Rating> datasetLoader, RatingsDataset<RatingType> ratingsDataset) {
        this.datasetLoader = datasetLoader;
        this.ratingsDataset = ratingsDataset;

        setAlias(datasetLoader.getAlias());
    }

    @Override
    public RelevanceCriteria getDefaultRelevanceCriteria() {
        return datasetLoader.getDefaultRelevanceCriteria();
    }

    @Override
    public RatingsDataset<RatingType> getRatingsDataset() throws CannotLoadRatingsDataset {
        return ratingsDataset;
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {
        final ContentDataset contentDataset;
        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            contentDataset = contentDatasetLoader.getContentDataset();
        } else {
            throw new CannotLoadContentDataset("The dataset loader is not a ContentDatasetLoader, cannot apply a content-based ");
        }

        return contentDataset;
    }

    @Override
    public synchronized UsersDataset getUsersDataset() throws CannotLoadUsersDataset {
        final UsersDataset usersDataset;
        if (datasetLoader instanceof UsersDatasetLoader) {
            UsersDatasetLoader usersDatasetLoader = (UsersDatasetLoader) datasetLoader;
            usersDataset = usersDatasetLoader.getUsersDataset();
        } else {
            throw new CannotLoadUsersDataset("The dataset loader is not a UsersDatasetLoader, cannot return the users dataset");
        }

        return usersDataset;
    }
}
