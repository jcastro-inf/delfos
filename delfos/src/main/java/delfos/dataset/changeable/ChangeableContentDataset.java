package delfos.dataset.changeable;

import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;

/**
 *
 * @author Jorge Castro Gallardo
 *
 * @version 16-sep-2013
 */
public interface ChangeableContentDataset extends ContentDataset {

    /**
     * Añade un item al dataset de contenido, para que el sistema de
     * recomendación que use este dataset lo tenga en cuenta
     *
     * @param item Contenido del objeto que se desea agregar
     */
    public void addItem(Item item);

    /**
     * Ordena que los datos sean guardados en el método persistente
     * correspondiente del dataset.
     */
    public void commitChangesInPersistence();
}
