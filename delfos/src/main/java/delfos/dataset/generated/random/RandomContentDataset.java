package delfos.dataset.generated.random;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.entity.EntityAlreadyExists;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterListener;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureType;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;

/**
 * Dataset generado aleatoriamente. Sirve para realizar pruebas previas a
 * utilizar un dataset real mientras que no se dispone de los datos reales.
 * Genera un dataset aleatorio en el que se puede configurar el número de
 * usuarios, número de productos, porcentaje de valoraciones definidas y rango
 * en que se da la valoración.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 15-Mar-2013
 */
public class RandomContentDataset extends ContentDatasetDefault {

    private static final long serialVersionUID = 1L;

    /**
     * Almacena para cada parámetro el valor que posee.
     */
    private final Map<Parameter, Object> parameterValues = new TreeMap<>();
    /**
     * Almacena los objetos que desean ser notificados de cambios en los
     * parámetros de este objeto.
     */
    private final Collection<ParameterListener> parammeterListeners = new LinkedList<>();

    private final long seed;
    private final int numNumericFeatures;
    private final int numNominalFeatures;
    private final int numNumericalDifferentValues;

    /**
     * Crea un dataset aleatorio.
     *
     * @param ratingsDataset Dataset de valoraciones asociado a este dataset de
     * contenido.
     * @param numNumericFeatures Número de características numéricas.
     * @param numNominalFeatures Número de características categóricas.
     * @param numNumericalDifferentValues Número de valores distintos en las
     * características.
     * @param seed Semilla que se usa para generar el dataset.
     */
    public RandomContentDataset(RatingsDataset<? extends Rating> ratingsDataset, int numNumericFeatures, int numNominalFeatures, int numNumericalDifferentValues, long seed) {
        this.seed = seed;
        this.numNumericFeatures = numNumericFeatures;
        this.numNominalFeatures = numNominalFeatures;
        this.numNumericalDifferentValues = numNumericalDifferentValues;

        Random random = new Random(seed);

        Collection<Integer> idItemSet = ratingsDataset.allRatedItems();

        Feature[] features = new Feature[numNumericFeatures + numNominalFeatures];

        for (int i = 0; i < numNumericFeatures; i++) {
            String name = "NumericalFeature_" + i;

            if (!featureGenerator.containsFeature(name)) {
                featureGenerator.createFeature(name, FeatureType.Numerical);
            }

            features[i] = featureGenerator.searchFeature(name);

        }

        for (int i = 0; i < numNominalFeatures; i++) {
            String name = "NominalFeature_" + i;
            if (!featureGenerator.containsFeature(name)) {
                featureGenerator.createFeature(name, FeatureType.Nominal);
            }

            features[numNumericFeatures + i] = featureGenerator.searchFeature(name);
        }

        for (int idItem : idItemSet) {
            Object[] values = new Object[features.length];
            for (int i = 0; i < features.length; i++) {

                Feature actualFeature = features[i];

                if (actualFeature.getType() == FeatureType.Numerical) {
                    values[i] = random.nextDouble();
                } else {
                    values[i] = "value_" + random.nextInt(numNumericalDifferentValues);
                }

            }
            Item item = new Item(idItem, "item_" + idItem, features, values);
            try {
                add(item);
            } catch (EntityAlreadyExists ex) {
                ERROR_CODES.UNDEFINED_ERROR.exit(ex);
                throw new IllegalArgumentException(ex);
            }

        }
    }

    public long getSeedValue() {
        return seed;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof ContentDataset) {
            ContentDataset contentDataset = (ContentDataset) o;
            return ContentDatasetDefault.compareTo(this, contentDataset);
        }
        throw new IllegalStateException("The object compared with has an unrecognised type.");
    }
}
