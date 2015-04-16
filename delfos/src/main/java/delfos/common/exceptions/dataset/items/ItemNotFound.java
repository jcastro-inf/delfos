package delfos.common.exceptions.dataset.items;

import delfos.dataset.basic.item.Item;
import delfos.common.exceptions.dataset.entity.EntityNotFound;

/**
 * Excepción que se lanza cuando no se encuentra un item en el modelo de un
 * sistema de recomendación, en los datasets, etc.
 *
* @author Jorge Castro Gallardo
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
