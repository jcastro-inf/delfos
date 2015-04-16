package delfos.dataset.basic.loader.types;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.dataset.basic.item.ContentDataset;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 26-Noviembre-2013
 */
public interface ContentDatasetLoader {

    /**
     * Obtiene el dataset de contenido que se usará en la recomendación
     *
     * @return dataset de contenido que se usará en la recomendación
     */
    public ContentDataset getContentDataset() throws CannotLoadContentDataset;

}
