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
package delfos.dataset.generated.modifieddatasets.filter;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.BooleanParameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.loaders.given.DatasetLoaderGivenRatingsDataset;

/**
 *
 * @author jcastro
 */
public class DatasetLoader_restrictNumRatings extends DatasetLoaderAbstract<Rating> {

    private DatasetLoader<Rating> filteredDatasetLoader = null;

    public static final Parameter DATASET_LOADER = new Parameter(
            "datasetLoader",
            new ParameterOwnerRestriction(
                    DatasetLoader.class,
                    ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k")
            )
    );

    public static final Parameter MINIMUM_USER_RATINGS = new Parameter(
            "minimumUserRatings",
            new IntegerParameter(0, 1000, 5));

    public static final Parameter MINIMUM_ITEM_RATINGS = new Parameter(
            "minimumItemRatings",
            new IntegerParameter(0, 1000, 5));

    public static final Parameter ITERATE_UNTIL_SATISFIED = new Parameter(
            "iterateUntilSatisfied",
            new BooleanParameter(Boolean.TRUE),
            "Do the filtering until the conditions are fulfiled by the final dataset. If this parameter is false, only one step of filtering is done."
    );

    public DatasetLoader_restrictNumRatings() {

        super();
        addParameter(ITERATE_UNTIL_SATISFIED);
        addParameter(MINIMUM_ITEM_RATINGS);
        addParameter(MINIMUM_USER_RATINGS);
        addParameter(DATASET_LOADER);

        addParammeterListener(() -> {
            filteredDatasetLoader = null;
        });
    }

    @Override
    public RatingsDataset<Rating> getRatingsDataset() throws CannotLoadRatingsDataset {
        loadAndFilter();

        return filteredDatasetLoader.getRatingsDataset();
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {
        loadAndFilter();

        return filteredDatasetLoader.getContentDataset();
    }

    @Override
    public UsersDataset getUsersDataset() throws CannotLoadUsersDataset {
        loadAndFilter();

        return filteredDatasetLoader.getUsersDataset();
    }

    private synchronized void loadAndFilter() {

        if (filteredDatasetLoader == null) {

            DatasetLoader<Rating> datasetLoader = getDatasetLoader();

            int minimumUserRatings = getMinimumUserRatings();
            int minimumItemRatings = getMinimumItemRatings();

            RatingsDataset<Rating> filteredRatingsDataset;
            if (isIterateUntilSatisfied()) {

                filteredRatingsDataset = RatingsDataset_restrinctNumRatings.buildFilteringRatingsUntilAllConditionsAreSatisfied(
                        datasetLoader,
                        minimumUserRatings,
                        minimumItemRatings);
            } else {
                filteredRatingsDataset = new RatingsDataset_restrinctNumRatings<>(
                        datasetLoader,
                        minimumUserRatings,
                        minimumItemRatings);
            }

            filteredDatasetLoader = new DatasetLoaderGivenRatingsDataset<>(datasetLoader, filteredRatingsDataset);

        }
    }

    private DatasetLoader<Rating> getDatasetLoader() {
        return (DatasetLoader<Rating>) getParameterValue(DATASET_LOADER);
    }

    private Boolean isIterateUntilSatisfied() {
        return (Boolean) getParameterValue(ITERATE_UNTIL_SATISFIED);
    }

    private Integer getMinimumUserRatings() {
        return (Integer) getParameterValue(MINIMUM_USER_RATINGS);
    }

    private Integer getMinimumItemRatings() {
        return (Integer) getParameterValue(MINIMUM_ITEM_RATINGS);
    }

}
