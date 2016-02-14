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

import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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

    @Override
    public String toString() {
        return getName();
    }

}
