package delfos.rs.contentbased.vsm.multivalued.profile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureType;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.common.datastructures.DoubleIndex_MultipleOccurrences;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.Global;

/**
 * Perfil de usuario para sistemas de recomendación multivaluados con
 * ponderación de características, como {@link EntropyDependenceCBRS}.
 *
 * @see EntropyDependenceCBRS
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 (18 Octubre 2011)
 * @version 1.1 1 de Marzo de 2013
 */
public class EntropyDependenceUserProfile extends BasicMultivaluedUserProfile {

    private static final long serialVersionUID = -3387516993124229948L;

    /**
     * Construye un perfil de usuario vacío, al que posteriormente se deben
     * asignar items valorados, crear los pesos, aplicar iuf y normalizar.
     *
     * @param idUser Id del usuario al que se refiere el perfil.
     */
    public EntropyDependenceUserProfile(int idUser) {
        super(idUser);
        _weights = new TreeMap<Feature, Float>();
    }

    /**
     * Construye un perfil de usuario completo, asignando todos sus valores.
     *
     * @param idUser Id del usuario al que se refiere el perfil.
     * @param valuesNominal Valores para cada valor de cada característica
     * nominal.
     * @param valuesNumerical Valor para cada característica numérica.
     * @param weights Ponderación de cada característica.
     */
    public EntropyDependenceUserProfile(int idUser, Map<Feature, Map<Object, Float>> valuesNominal, Map<Feature, Float> valuesNumerical, Map<Feature, Float> weights) {
        super(idUser, valuesNominal, valuesNumerical);
        this._weights = new TreeMap<Feature, Float>(weights);

    }

    /**
     * Devuelve la ponderación de la característica indicada.
     *
     * @param itemFeature Característica para la que se busca la ponderación.
     * @return Ponderación de la característica. Si el perfil no contiene la
     * característica, devuelve cero.
     */
    @Override
    public float getFeatureValueWeight(Feature itemFeature) {
        if (_weights.containsKey(itemFeature)) {
            return _weights.get(itemFeature);
        } else {
            return 0;
        }
    }

    /**
     * Aplica las entropías indicadas a los pesos del perfil.
     *
     * @param entropias entropías normalizadas de las características
     */
    public void applyEntropy(Map<Feature, Float> entropias) {
        for (Feature f : entropias.keySet()) {
            float get = _weights.get(f);
            get = get * entropias.get(f);
            _weights.put(f, get);
        }
    }

    /**
     * Normaliza los pesos del perfil. Un vector de pesos normalizado es aquel
     * que cumple la propiedad Sum(pesos) = 1
     */
    public void normalizeWeights() {
        float norma = 0;
        for (Feature f : getFeatures()) {
            norma += _weights.get(f);
        }
        if (norma == 0) {
            return;
        }

        for (Feature f : getFeatures()) {
            float get = _weights.get(f);
            get = get / norma;
            _weights.put(f, get);
        }
    }

    /**
     * Crea las ponderaciones intra usuario una vez se han especificado todos
     * los productos valorados por el usuario.
     *
     * @param ratingDataset Dataset de valoraciones.
     * @param contentDataset Dataset de contenido.
     *
     * @throws UserNotFound Si no se encuentra el usuario en el dataset de
     * valoraciones.
     * @throws ItemNotFound Si no se encuentra el producto en el dataset de
     * valoraciones.
     */
    public void createWeightsIntraUser(RatingsDataset<? extends Rating> ratingDataset, ContentDataset contentDataset) throws UserNotFound, ItemNotFound {
        _weights = new TreeMap<Feature, Float>();

        for (Feature f : contentDataset.getFeatures()) {
            if (f.getType() == FeatureType.Nominal) {
                _weights.put(f, cramer(f, _idUser, ratingDataset, contentDataset));
            }
            if (f.getType() == FeatureType.Numerical) {
                _weights.put(f, pearson(f, _idUser, ratingDataset, contentDataset));
            }
            if (f.getType() != FeatureType.Numerical && f.getType() != FeatureType.Nominal) {
                _weights.put(f, 0.0f);
            }

            if (_weights.get(f) > 1.00001 || _weights.get(f) < 0) {
                Global.showInfoMessage("La característica " + f.getName() + " tiene como intra " + _weights.get(f) + "\n");
            }
        }
    }

    private static float cramer(Feature feature, int idUser, RatingsDataset<? extends Rating> rd, ContentDataset cd) throws UserNotFound, ItemNotFound {

        //composición de las EEDD que ayuda en el cálculo
        DoubleIndex_MultipleOccurrences<String, String> di = new DoubleIndex_MultipleOccurrences<String, String>();

        Map<Integer, ? extends Rating> userRatings = rd.getUserRatingsRated(idUser);
        for (int idItem : userRatings.keySet()) {
            Item item;
            try {
                item = cd.get(idItem);
            } catch (EntityNotFound ex) {
                throw new IllegalStateException(ex);
            }
            String valor = (String) item.getFeatureValue(feature);
            String puntuacion = userRatings.get(idItem).toString();
            di.add(valor, puntuacion);
        }

        //la variable i va a ser siempre la característica elegido
        //la variable j siempre es la puntuación
        float numerador = 0;

        for (Iterator<String> it = di.iteratorType1Values(); it.hasNext();) {
            String valor = it.next();
            for (Iterator<String> it2 = di.iteratorType2Values(); it2.hasNext();) {
                String puntuacion = it2.next();
                float freqPar = di.frequencyOfPair(valor, puntuacion);
                float freqValor = di.frequencyOfType1Value(valor);
                float freqPuntuacion = di.frequencyOfType2Value(puntuacion);

                numerador += Math.pow(freqPar - ((freqValor * freqPuntuacion) / di.size()), 2) / ((freqValor * freqPuntuacion) / di.size());
            }
        }
        float denominador = di.size() * (Math.min(di.numDistinctType1Values(), di.numDistinctType2Values()) - 1);
        if (numerador == 0 && denominador == 0) {
            return 0;
        } else {
            if (denominador == 0) {
                return 0;
            }
            float cramer = (float) Math.sqrt(numerador / denominador);
            return cramer;
        }
    }

    private static float pearson(Feature feature, int idUser, RatingsDataset<? extends Rating> rd, ContentDataset cd) throws UserNotFound, ItemNotFound {
        float pearson;

        List<Number> valoresReales = new ArrayList<Number>();
        List<Number> puntuaciones = new ArrayList<Number>();

        Map<Integer, ? extends Rating> userRatings = rd.getUserRatingsRated(idUser);

        for (int idItem : userRatings.keySet()) {
            Item item;
            try {
                item = cd.get(idItem);
            } catch (EntityNotFound ex) {
                throw new IllegalStateException(ex);
            }
            Object value = item.getFeatureValue(feature);
            if (value != null) {
                float valor;
                if (value instanceof Float) {
                    valor = (Float) value;
                } else if (value instanceof Integer) {
                    valor = (Integer) value;
                } else {
                    throw new IllegalArgumentException("Feature type not supported.");
                }

                Rating puntuacion = userRatings.get(idItem);
                valoresReales.add(valor);
                puntuaciones.add(puntuacion.ratingValue);
            }
        }
        float covarianza = 0;
        float valorMedio = media(valoresReales);
        float puntuacionMedia = media(puntuaciones);

        float varianzaFeature = 0;
        float varianzaPuntuacion = 0;
        int n = valoresReales.size();

        for (int i = 0; i < n; i++) {
            float sumandoAnyo = valoresReales.get(i).floatValue() - valorMedio;
            sumandoAnyo = (float) Math.pow(sumandoAnyo, 2);

            float sumandoPuntuacion = puntuaciones.get(i).floatValue() - puntuacionMedia;
            sumandoPuntuacion = (float) Math.pow(sumandoPuntuacion, 2);

            varianzaFeature += sumandoAnyo / n;
            varianzaPuntuacion += sumandoPuntuacion / n;

            covarianza += (valoresReales.get(i).floatValue() * puntuaciones.get(i).floatValue() - valorMedio * puntuacionMedia);
        }
        varianzaFeature = (float) Math.sqrt(varianzaFeature);
        varianzaPuntuacion = (float) Math.sqrt(varianzaPuntuacion);

        covarianza = covarianza / n;
        if (varianzaFeature == 0 || varianzaPuntuacion == 0) {
            return 0;
        } else {
            pearson = covarianza / (varianzaFeature * varianzaPuntuacion);
            pearson = Math.abs(pearson);
            return pearson;
        }
    }

    private static float media(List<? extends Number> lista) {
        float media = 0;
        for (Object d : lista) {
            if (d instanceof Number) {
                media += ((Number) d).floatValue();
            } else {
                throw new IllegalArgumentException("El objeto no es float ni entero");
            }

        }
        media = media / lista.size();
        return media;
    }

    @Override
    public boolean contains(Feature f, Object value) {
        if (f.getType() == FeatureType.Nominal) {
            if (_nominalValues.containsKey(f)) {
                return _nominalValues.get(f).containsKey(value);
            } else {
                return false;
            }
        }
        if (f.getType() == FeatureType.Numerical) {
            return _numericalValues.containsKey(f);
        }
        return false;
    }
}
