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
package delfos.rs.contentbased.vsm.multivalued.profile;

import delfos.common.Global;
import delfos.common.datastructures.DoubleIndex_MultipleOccurrences;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureType;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Perfil de usuario para sistemas de recomendación multivaluados con
 * ponderación de características, como {@link EntropyDependenceCBRS}.
 *
 * @see EntropyDependenceCBRS
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
        _weights = new TreeMap<Feature, Double>();
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
    public EntropyDependenceUserProfile(int idUser, Map<Feature, Map<Object, Double>> valuesNominal, Map<Feature, Double> valuesNumerical, Map<Feature, Double> weights) {
        super(idUser, valuesNominal, valuesNumerical);
        this._weights = new TreeMap<Feature, Double>(weights);

    }

    /**
     * Devuelve la ponderación de la característica indicada.
     *
     * @param itemFeature Característica para la que se busca la ponderación.
     * @return Ponderación de la característica. Si el perfil no contiene la
     * característica, devuelve cero.
     */
    @Override
    public double getFeatureValueWeight(Feature itemFeature) {
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
    public void applyEntropy(Map<Feature, Double> entropias) {
        for (Feature f : entropias.keySet()) {
            double get = _weights.get(f);
            get = get * entropias.get(f);
            _weights.put(f, get);
        }
    }

    /**
     * Normaliza los pesos del perfil. Un vector de pesos normalizado es aquel
     * que cumple la propiedad Sum(pesos) = 1
     */
    public void normalizeWeights() {
        double norma = 0;
        for (Feature f : getFeatures()) {
            norma += _weights.get(f);
        }
        if (norma == 0) {
            return;
        }

        for (Feature f : getFeatures()) {
            double get = _weights.get(f);
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
        _weights = new TreeMap<Feature, Double>();

        for (Feature f : contentDataset.getFeatures()) {
            if (f.getType() == FeatureType.Nominal) {
                _weights.put(f, cramer(f, _idUser, ratingDataset, contentDataset));
            }
            if (f.getType() == FeatureType.Numerical) {
                _weights.put(f, pearson(f, _idUser, ratingDataset, contentDataset));
            }
            if (f.getType() != FeatureType.Numerical && f.getType() != FeatureType.Nominal) {
                _weights.put(f, 0.0);
            }

            if (_weights.get(f) > 1.00001 || _weights.get(f) < 0) {
                Global.showInfoMessage("La característica " + f.getName() + " tiene como intra " + _weights.get(f) + "\n");
            }
        }
    }

    private static double cramer(Feature feature, int idUser, RatingsDataset<? extends Rating> rd, ContentDataset cd) throws UserNotFound, ItemNotFound {

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
        double numerador = 0;

        for (Iterator<String> it = di.iteratorType1Values(); it.hasNext();) {
            String valor = it.next();
            for (Iterator<String> it2 = di.iteratorType2Values(); it2.hasNext();) {
                String puntuacion = it2.next();
                double freqPar = di.frequencyOfPair(valor, puntuacion);
                double freqValor = di.frequencyOfType1Value(valor);
                double freqPuntuacion = di.frequencyOfType2Value(puntuacion);

                numerador += Math.pow(freqPar - ((freqValor * freqPuntuacion) / di.size()), 2) / ((freqValor * freqPuntuacion) / di.size());
            }
        }
        double denominador = di.size() * (Math.min(di.numDistinctType1Values(), di.numDistinctType2Values()) - 1);
        if (numerador == 0 && denominador == 0) {
            return 0;
        } else {
            if (denominador == 0) {
                return 0;
            }
            double cramer = (double) Math.sqrt(numerador / denominador);
            return cramer;
        }
    }

    private static double pearson(Feature feature, int idUser, RatingsDataset<? extends Rating> rd, ContentDataset cd) throws UserNotFound, ItemNotFound {
        double pearson;

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
                double valor;
                if (value instanceof Double) {
                    valor = (Double) value;
                } else if (value instanceof Integer) {
                    valor = (Integer) value;
                } else {
                    throw new IllegalArgumentException("Feature type not supported.");
                }

                Rating puntuacion = userRatings.get(idItem);
                valoresReales.add(valor);
                puntuaciones.add(puntuacion.getRatingValue());
            }
        }
        double covarianza = 0;
        double valorMedio = media(valoresReales);
        double puntuacionMedia = media(puntuaciones);

        double varianzaFeature = 0;
        double varianzaPuntuacion = 0;
        int n = valoresReales.size();

        for (int i = 0; i < n; i++) {
            double sumandoAnyo = valoresReales.get(i).doubleValue() - valorMedio;
            sumandoAnyo = (double) Math.pow(sumandoAnyo, 2);

            double sumandoPuntuacion = puntuaciones.get(i).doubleValue() - puntuacionMedia;
            sumandoPuntuacion = (double) Math.pow(sumandoPuntuacion, 2);

            varianzaFeature += sumandoAnyo / n;
            varianzaPuntuacion += sumandoPuntuacion / n;

            covarianza += (valoresReales.get(i).doubleValue() * puntuaciones.get(i).doubleValue() - valorMedio * puntuacionMedia);
        }
        varianzaFeature = (double) Math.sqrt(varianzaFeature);
        varianzaPuntuacion = (double) Math.sqrt(varianzaPuntuacion);

        covarianza = covarianza / n;
        if (varianzaFeature == 0 || varianzaPuntuacion == 0) {
            return 0;
        } else {
            pearson = covarianza / (varianzaFeature * varianzaPuntuacion);
            pearson = Math.abs(pearson);
            return pearson;
        }
    }

    private static double media(List<? extends Number> lista) {
        double media = 0;
        for (Object d : lista) {
            if (d instanceof Number) {
                media += ((Number) d).doubleValue();
            } else {
                throw new IllegalArgumentException("El objeto no es double ni entero");
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
