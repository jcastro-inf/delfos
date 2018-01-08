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
package delfos.dataset.loaders.jester;

import delfos.common.Global;
import delfos.common.datastructures.MultiSet;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.BooleanParameter;
import delfos.common.parameters.restriction.ObjectParameter;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.basic.user.UsersDatasetAdapter;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.dataset.storage.memory.DefaultMemoryRatingsDataset_UserIndexed;
import java.io.File;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.stream.Collectors;
import jxl.Sheet;
import jxl.Workbook;

/**
 * Implementa el cargador del dataset Jester, que toma los datos de los archivos
 * de Excel que se proporcionan en la página del dataset. No contiene
 * información sobre el contenido de los items, por lo que se genera un dataset
 * de contenido al cargar el dataset de ratings.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class Jester extends DatasetLoaderAbstract {

    private static final long serialVersionUID = 1L;
    private static final String EXTENSION;
    public static final Parameter DATASET_VERSION_PARAMETER;
    public static final String VERSION_1;
    public static final String VERSION_2;
    public static final String VERSION_3;
    public static final Parameter memoryEfficient;

    static {
        EXTENSION = ".xls";
        VERSION_1 = "." + File.separator + "datasets" + File.separator + "Jester" + File.separator + "jester-data-1" + EXTENSION;
        VERSION_2 = "." + File.separator + "datasets" + File.separator + "Jester" + File.separator + "jester-data-2" + EXTENSION;
        VERSION_3 = "." + File.separator + "datasets" + File.separator + "Jester" + File.separator + "jester-data-3" + EXTENSION;

        Object[] values = new Object[3];
        values[0] = VERSION_1;
        values[1] = VERSION_2;
        values[2] = VERSION_3;
        memoryEfficient = new Parameter("saveMemory", new BooleanParameter(Boolean.FALSE));
        DATASET_VERSION_PARAMETER = new Parameter("datasetVersion", new ObjectParameter(values, VERSION_1));
    }
    private RatingsDataset<Rating> ratingsDataset;
    private ContentDataset contentDataset;
    private UsersDataset usersDataset;

    public Jester() {
        addParameter(memoryEfficient);
        addParameter(DATASET_VERSION_PARAMETER);
        addParammeterListener(() -> {
            ratingsDataset = null;
            contentDataset = null;
            usersDataset = null;
        });
    }

    @Override
    public RatingsDataset<Rating> getRatingsDataset() throws CannotLoadRatingsDataset {
        if (ratingsDataset == null) {
            String fileName = (String) getParameterValue(DATASET_VERSION_PARAMETER);

            ArrayList<Rating> ratings = new ArrayList<>();

            TreeSet<Item> items = new TreeSet<>();
            try {

                MultiSet<Double> nullRatings = new MultiSet<>();
                Workbook archivoExcel = Workbook.getWorkbook(new File(fileName));
                for (int sheetNo = 0; sheetNo < 1; sheetNo++) {
                    Sheet hoja = archivoExcel.getSheet(sheetNo);
                    int numColumnas = hoja.getColumns();
                    int numFilas = hoja.getRows();
                    String data;
                    for (int fila = 0; fila < numFilas; fila++) {
                        data = hoja.getCell(0, fila).getContents();
                        int numRatingsUser_expected = Integer.parseInt(data);
                        for (int columna = 1; columna < numColumnas; columna++) {
                            data = hoja.getCell(columna, fila).getContents();
                            double rating;
                            try {
                                rating = Double.parseDouble(data);
                            } catch (NumberFormatException nfe) {
                                rating = Double.parseDouble(data.replace(',', '.'));
                            }
                            //Los 99 significa que no se ha valorado.
                            if (rating > 10 || rating < -10) {
                                nullRatings.add(rating);
                            } else {
                                ratings.add(new Rating(fila, columna, rating));
                            }
                        }

                    }
                }

                if ((Boolean) getParameterValue(memoryEfficient)) {
                    ratingsDataset = new DefaultMemoryRatingsDataset_UserIndexed(ratings);
                } else {
                    ratingsDataset = new BothIndexRatingsDataset(ratings);
                }

                nullRatings.removeAllOccurrences(new Double(99));
                if (!nullRatings.isEmpty()) {
                    Global.showInfoMessage(nullRatings.printContent());
                }
                Feature[] features = new Feature[0];
                Object[] values = new Object[0];
                for (long idItem : ratingsDataset.allRatedItems()) {
                    items.add(new Item(idItem, Long.toString(idItem), features, values));
                }

                contentDataset = new ContentDatasetDefault(items);
                usersDataset = new UsersDatasetAdapter(ratingsDataset.allUsers().stream().map(idUser -> new User(idUser)).collect(Collectors.toSet()));
            } catch (Throwable ex) {
                Global.showError(ex);
                ratingsDataset = null;
                contentDataset = null;

                throw new CannotLoadRatingsDataset(ex);
            }

        }
        return ratingsDataset;
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {
        try {
            getRatingsDataset();
            return contentDataset;
        } catch (CannotLoadRatingsDataset ex) {
            throw new CannotLoadContentDataset(ex);
        }
    }

    @Override
    public UsersDataset getUsersDataset() throws CannotLoadUsersDataset {
        try {
            getRatingsDataset();
            return usersDataset;
        } catch (CannotLoadRatingsDataset ex) {
            throw new CannotLoadUsersDataset(ex);
        }
    }

    /**
     * Paper del que e extraido este criterio:
     *
     * <p>
     * <p>
     * Collaborative Filtering Using Random Neighbours in Peer-to-Peer Networks.
     *
     * @return Criterio de relevancia de este dataset.
     */
    @Override
    public RelevanceCriteria getDefaultRelevanceCriteria() {
        return new RelevanceCriteria(2.5);
    }
}
