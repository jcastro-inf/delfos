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

import delfos.dataset.changeable.ChangeableDatasetLoader;
import delfos.dataset.changeable.ChangeableDatasetLoaderAbstract;

/**
 * Clase para almacenar la configuración de un dataset modificable.
 *
 * @see ChangeableDatasetLoaderAbstract
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 17-Septiembre-2013
 */
public class ChangeableDatasetConfiguration {

    /**
     * Cargador de dataset modificable recuperado del fichero de configuración.
     */
    public final ChangeableDatasetLoader datasetLoader;

    /**
     * Constructor de la estructura.
     *
     * @param datasetLoader Cargador de dataset.
     */
    public ChangeableDatasetConfiguration(ChangeableDatasetLoader datasetLoader) {
        this.datasetLoader = datasetLoader;
    }
}
