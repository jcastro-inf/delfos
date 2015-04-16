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
