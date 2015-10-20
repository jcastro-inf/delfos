package delfos.dataset.basic.item;

import delfos.common.Global;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.dataset.basic.features.CollectionOfEntitiesWithFeaturesDefault;
import delfos.dataset.basic.features.FeatureGenerator;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Dataset de contenido que almacena todos los items en memoria
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0.0 Unknown date
 * @version 1.0.1 07-Mar-2013 Eliminación de la especificación de las
 * características del dataset, un dataset de contenido tiene las
 * características que tienen sus productos.
 * @version 1.0.2 25-Julio-2013 Renombrado de DefatultContentDataset a
 * ContentDatasetAdapter y movido de paquete
 * @version 1.1.0 15-Mar-2013 Incorporación del {@link FeatureGenerator} dentro
 * del objeto, para que cada dataset de contenido tenga unas características
 * distintas.
 * @version 1.2.0 16-Septiembre-2013 Reorganización de los datasets de
 * contenido, elmiminando la duplicidad de los mismos con la incorporación de
 * disponibilidad de los productos.
 */
public class ContentDatasetDefault extends CollectionOfEntitiesWithFeaturesDefault<Item> implements ContentDataset {

    /**
     * Crea un dataset de contenido vacío
     */
    public ContentDatasetDefault() {
    }

    /**
     * Crea un dataset de contenido a partir de los items que contiene.
     *
     * @param items
     */
    public ContentDatasetDefault(Set<Item> items) {
        this();
        items.stream().forEach((item) -> add(item));
    }

    @Override
    public Collection<Integer> allID() {
        return getAllID();
    }

    /**
     * Devuelve el conjunto de productos que pueden ser recomendados en el
     * dataset. Los productos que no se encuentran disponibles pueden ser por
     * diversas causas, descatalogados, fuera de stock, entre otras. En
     * cualquier caso son productos que no se recomienda recomendar, valga la
     * redundancia.
     *
     * @return
     */
    @Override
    public Collection<Integer> getAvailableItems() {
        if (availableProducts == null) {
            return allID();
        } else {
            return Collections.unmodifiableCollection(availableProducts);
        }
    }
    protected Collection<Integer> availableProducts = null;

    @Override
    public void setItemAvailable(int idItem, boolean available) throws ItemNotFound {
        if (!allID().contains(idItem)) {
            throw new ItemNotFound(idItem);
        }
        if (availableProducts == null) {
            availableProducts = new TreeSet<>(allID());
        }

        if (available) {
            if (availableProducts.contains(idItem)) {
                //El producto estaba como disponible, no se hace nada.
            } else {
                //El producto estaba como no disponible, se pone disponible.
                availableProducts.add(idItem);
                if (Global.isVerboseAnnoying()) {
                    Global.showWarning("The product " + idItem + " is now available.");
                }
            }
        } else {
            if (availableProducts.contains(idItem)) {
                //Hay que quitar el producto de la lista de disponibles.
                availableProducts.remove(idItem);
                if (Global.isVerboseAnnoying()) {
                    Global.showWarning("The product " + idItem + " has been removed from available items");
                }
            } else {
                //El producto estaba como no disponible, no se hace nada.
                if (Global.isVerboseAnnoying()) {
                    Global.showWarning("The product " + idItem + " has been removed from available items");
                }
            }
        }
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof ContentDataset) {
            ContentDataset contentDataset = (ContentDataset) o;
            int diff = this.hashCode() - contentDataset.hashCode();
            return diff;
        } else {
            throw new IllegalStateException("The object compared with is not a ContentDataset.");
        }
    }

    public static int compareTo(ContentDataset cd1, ContentDataset cd2) {
        int diff = cd1.hashCode() - cd2.hashCode();
        return diff;
    }

    @Override
    public String toString() {
        Set<String> items = this.stream()
                .map((item) -> "Item " + item.getId())
                .collect(Collectors.toCollection(TreeSet::new));
        return items.toString();
    }

    @Override
    public Item getItem(int idItem) throws ItemNotFound {
        try {
            return get(idItem);
        } catch (EntityNotFound ex) {
            ex.isA(Item.class);
            throw new ItemNotFound(idItem, ex);
        }
    }

}
