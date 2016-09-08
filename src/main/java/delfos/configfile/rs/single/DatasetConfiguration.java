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
package delfos.configfile.rs.single;

import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.changeable.ChangeableDatasetLoader;
import delfos.dataset.changeable.ChangeableDatasetLoaderAbstract;
import delfos.main.managers.database.submanagers.DatabaseCaseUseSubManager;

/**
 * Clase para almacenar la configuración de un dataset modificable.
 *
 * @see ChangeableDatasetLoaderAbstract
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 17-Septiembre-2013
 */
public class DatasetConfiguration {

    /**
     * Cargador de dataset modificable recuperado del fichero de configuración.
     */
    private final DatasetLoader<? extends Rating> datasetLoader;

    /**
     * Constructor de la estructura.
     *
     * @param datasetLoader Cargador de dataset.
     */
    public DatasetConfiguration(DatasetLoader<? extends Rating> datasetLoader) {
        this.datasetLoader = datasetLoader;
    }

    /**
     * @return the datasetLoader
     */
    public DatasetLoader<? extends Rating> getDatasetLoader() {
        return datasetLoader;
    }

    public ChangeableDatasetLoader getChangeableDatasetLoader() {
        return DatabaseCaseUseSubManager.viewDatasetLoaderAsChangeable(datasetLoader);
    }
}
