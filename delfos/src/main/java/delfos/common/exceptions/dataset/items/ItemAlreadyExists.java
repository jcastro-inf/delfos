package delfos.common.exceptions.dataset.items;

/**
 * Excepción que indica que se ha intentado añadir un usuario al dataset de
 * usuarios y ya existía.
 *
* @author Jorge Castro Gallardo
 *
 * @version 16-sep-2013
 */
public class ItemAlreadyExists extends Exception {

    private static final long serialVersionUID = 1L;
    private final int idItem;

    public ItemAlreadyExists(int idItem) {
        this.idItem = idItem;
    }

    public ItemAlreadyExists(int idItem, Throwable cause) {
        super(cause);
        this.idItem = idItem;
    }

    public int getIdItem() {
        return idItem;
    }
}
