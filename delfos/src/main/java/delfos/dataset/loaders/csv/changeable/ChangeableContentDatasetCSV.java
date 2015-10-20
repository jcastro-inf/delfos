package delfos.dataset.loaders.csv.changeable;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.parameters.ParameterListener;
import delfos.dataset.basic.features.CollectionOfEntitiesWithFeaturesDefault;
import delfos.dataset.basic.item.Item;
import delfos.dataset.changeable.ChangeableContentDataset;
import delfos.io.csv.dataset.item.ContentDatasetToCSV;
import delfos.io.csv.dataset.item.DefaultContentDatasetToCSV;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 * Implementa un dataset de contenido con persistencia sobre fichero CSV con la
 * posibilidad de modificar los productos del mismo.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 16-sep-2013
 */
public class ChangeableContentDatasetCSV extends CollectionOfEntitiesWithFeaturesDefault<Item> implements ChangeableContentDataset {

    private final ChangeableCSVFileDatasetLoader parent;

    public ChangeableContentDatasetCSV(final ChangeableCSVFileDatasetLoader parent) {
        super();
        this.parent = parent;

        parent.addParammeterListener(new ParameterListener() {
            private File file = null;

            @Override
            public void parameterChanged() {
                if (file == null) {
                    file = parent.getUsersDatasetFile();
                } else {
                    if (!file.equals(parent.getUsersDatasetFile())) {
                        commitChangesInPersistence();
                    }
                }
            }
        });
    }

    public ChangeableContentDatasetCSV(final ChangeableCSVFileDatasetLoader parent, Set<Item> items) {
        this(parent);

        for (Item item : items) {
            addItem(item);
        }
    }

    @Override
    public final void addItem(Item item) {
        super.add(item);
    }

    @Override
    public Item get(int idItem) throws EntityNotFound {
        if (entitiesById.containsKey(idItem)) {
            return entitiesById.get(idItem);
        } else {
            throw new ItemNotFound(idItem);
        }
    }

    @Override
    public void commitChangesInPersistence() {
        try {
            ContentDatasetToCSV contentDatasetToCSV = new DefaultContentDatasetToCSV();
            contentDatasetToCSV.writeDataset(this, parent.getContentDatasetFile().getAbsolutePath());
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_CONTENT_DATASET.exit(ex);
        }
    }

    @Override
    public int size() {
        return allID().size();
    }

    @Override
    public Collection<Integer> allID() {
        return super.getAllID();
    }

    @Override
    public Collection<Integer> getAvailableItems() {
        return allID();
    }

    @Override
    public void setItemAvailable(int idItem, boolean available) throws ItemNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int compareTo(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
