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
package delfos.dataset.basic.item;

import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.dataset.basic.features.CollectionOfEntitiesWithFeatures;
import java.util.Collection;

/**
 * Interfaz que define los métodos de un dataset de contenido.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
    @Override
    public int size();

    /**
     * Devuelve un conjunto de todos los id de los items que se encuentran
     * actualmente en el dataset
     *
     * @return Colección con todos los id de los items.
     */
    public Collection<Integer> allIDs();

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
