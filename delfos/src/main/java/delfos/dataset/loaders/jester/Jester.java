package delfos.dataset.loaders.jester;

import delfos.common.Global;
import delfos.common.datastructures.MultiSet;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
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
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.dataset.storage.memory.DefaultMemoryRatingsDataset_UserIndexed;
import java.io.File;
import java.util.ArrayList;
import jxl.Sheet;
import jxl.Workbook;

/**
 * Implementa el cargador del dataset Jester, que toma los datos de los archivos
 * de Excel que se proporcionan en la página del dataset. No contiene
 * información sobre el contenido de los items, por lo que se genera un dataset
 * de contenido al cargar el dataset de ratings.
 *
 * @author Jorge Castro Gallardo
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
    private RatingsDataset<Rating> rd;
    private ContentDataset cd;

    public Jester() {
        addParameter(memoryEfficient);
        addParameter(DATASET_VERSION_PARAMETER);
        addParammeterListener(() -> {
            rd = null;
            cd = null;
        });
    }

    @Override
    public RatingsDataset<Rating> getRatingsDataset() throws CannotLoadRatingsDataset {
        if (rd == null) {
            String fileName = (String) getParameterValue(DATASET_VERSION_PARAMETER);

            ArrayList<Rating> ratings = new ArrayList<Rating>();

            ArrayList<Item> items = new ArrayList<Item>();
            try {

                MultiSet<Float> nullRatings = new MultiSet<Float>();
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
                            float rating;
                            try {
                                rating = Float.parseFloat(data);
                            } catch (NumberFormatException nfe) {
                                rating = Float.parseFloat(data.replace(',', '.'));
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
                    rd = new DefaultMemoryRatingsDataset_UserIndexed(ratings);
                } else {
                    rd = new BothIndexRatingsDataset(ratings);
                }

                nullRatings.removeAllOccurrences(new Float(99));
                if (!nullRatings.isEmpty()) {
                    Global.showInfoMessage(nullRatings.printContent());
                }
                Feature[] features = new Feature[0];
                Object[] values = new Object[0];
                for (int idItem : rd.allRatedItems()) {
                    items.add(new Item(idItem, Integer.toString(idItem), features, values));
                }

                cd = new ContentDatasetDefault(items);
            } catch (Throwable ex) {
                Global.showError(ex);
                rd = null;
                cd = null;

                throw new CannotLoadRatingsDataset(ex);
            }

        }
        return rd;
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
