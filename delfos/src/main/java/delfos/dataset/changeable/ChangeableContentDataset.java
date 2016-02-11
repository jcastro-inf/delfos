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
