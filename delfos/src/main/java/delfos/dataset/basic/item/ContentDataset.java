package delfos.dataset.basic.item;

import java.util.Collection;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.dataset.basic.features.CollectionOfEntitiesWithFeatures;

/**
 * Interfaz que define los métodos de un dataset de contenido.
 *
* @author Jorge Castro Gallardo
 *
 * @version 16-sep-2013 Refactorización de la biblioteca para organizar los
 * dataset de contenido.
 */
public interface ContentDataset extends Comparable<Object>, CollectionOfEntitiesWithFeatures<Item> {

    /**
     * Devuelve el objeto que almacena el contenido del item que se consulta
     *
     * @param idItem id del item para el que se desea conocer su contenido
     * @return objeto que que almacena el contenido del item
     * @throws delfos.common.exceptions.dataset.entity.EntityNotFound
     */
    @Override
    public Item get(int idItem) throws EntityNotFound;

    /**
     * Devuelve el objeto que almacena el contenido del item que se consulta
     *
     * @param idItem id del item para el que se desea conocer su contenido
     * @return objeto que que almacena el contenido del item
     * @throws delfos.common.exceptions.dataset.items.ItemNotFound
     */
    public Item getItem(int idItem) throws ItemNotFound;

    /**
     * Devuelve el número de productos que hay actualmente en el dataset
     *
     * @return número de productos
     */
    public int size();

    /**
     * Devuelve un conjunto de todos los id de los items que se encuentran
     * actualmente en el dataset
     *
     * @return Colección con todos los id de los items.
     */
    public Collection<Integer> allID();

    /**
     * Devuelve el conjunto de productos que pueden ser recomendados en el
     * dataset. Los productos que no se encuentran disponibles pueden ser por
     * diversas causas, descatalogados, fuera de stock, entre otras. En
     * cualquier caso son productos que no se recomienda recomendar, valga la
     * redundancia.
     *
     * @return
     */
    public Collection<Integer> getAvailableItems();

    /**
     * Establece el producto indicado como producto disponible. Una vez se llama
     * a este método, se deben indicar todos los productos disponibles,ya que
     * supone que los que no se indicaron no están disponibles.
     *
     * @param idItem Producto que está disponible.
     * @param available True si el producto está disponible, false si no lo
     * esta.
     */
    public void setItemAvailable(int idItem, boolean available) throws ItemNotFound;
}
