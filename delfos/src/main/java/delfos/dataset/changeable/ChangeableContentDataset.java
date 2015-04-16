/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package delfos.dataset.changeable;

import delfos.common.exceptions.dataset.items.ItemAlreadyExists;
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
     * @throws delfos.common.exceptions.dataset.items.ItemAlreadyExists
     */
    public void addItem(Item item) throws ItemAlreadyExists;

    /**
     * Ordena que los datos sean guardados en el método persistente
     * correspondiente del dataset.
     */
    public void commitChangesInPersistence();
}
