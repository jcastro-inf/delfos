package delfos.dataset.generated.modifieddatasets;

import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.DatasetLoaderParameterRestriction;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.loaders.csv.CSVfileDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.experiment.SeedHolder;

/**
 * Clase que encapsula el funcionamiento de un dataset que se encarga de reducir
 * los datos de un dataset original.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 ( 10/04/2012 )
 */
public abstract class DatasetSampler extends DatasetLoaderAbstract implements SeedHolder {

    /**
     * Parámetro que almacena el dataset original.
     */
    public static final Parameter ORIGINAL_DATASET_PARAMETER = new Parameter("Original_dataset", new DatasetLoaderParameterRestriction(new CSVfileDatasetLoader()));

    public DatasetSampler() {
        super();
        addParameter(ORIGINAL_DATASET_PARAMETER);
        init();
    }

    public DatasetSampler(DatasetLoader<? extends Rating> originalDataset) {
        this();
        setParameterValue(ORIGINAL_DATASET_PARAMETER, originalDataset);
    }

    /**
     * Método que indica al dataset que debe volver a reducir los datos,
     * cambiando los datos que se seleccionan del dataset original (si procede)
     */
    public abstract void shuffle() throws CannotLoadRatingsDataset;

    public RatingsDataset<? extends Rating> getOriginalDataset() {
        return (RatingsDataset<? extends Rating>) getParameterValue(ORIGINAL_DATASET_PARAMETER);
    }

    @Override
    public void setSeedValue(long seedValue) {
        setParameterValue(SEED, seedValue);
    }

    @Override
    public long getSeedValue() {
        return (Long) getParameterValue(SEED);
    }

    private void init() {
        addParameter(SEED);
    }
}
