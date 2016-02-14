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
package delfos.common.exceptions.dataset.items;

import delfos.dataset.basic.item.Item;
import delfos.common.exceptions.dataset.entity.EntityNotFound;

/**
 * Excepción que se lanza cuando no se encuentra un item en el modelo de un
 * sistema de recomendación, en los datasets, etc.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2103
 */
public class ItemNotFound extends EntityNotFound {

    private static final long serialVersionUID = 1L;
    public final int idItem;

    /**
     * Crea la excepción con el mensaje indicado.
     *
     * @param msg Mensaje a mostrar.
     */
    public ItemNotFound(int idItem, String msg) {
        super(Item.class, idItem, msg);
        this.idItem = idItem;
    }

    /**
     * Crea la excepción a partir del id del producto que no se encuentra.
     *
     * @param idItem Id del producto no encontrado.
     */
    public ItemNotFound(int idItem) {
        super(Item.class, idItem, "Item '" + idItem + "' not found");
        this.idItem = idItem;
    }

    public ItemNotFound(int idItem, Throwable cause) {

        super(Item.class, idItem, cause, "Item '" + idItem + "' not found");
        this.idItem = idItem;
    }
}
