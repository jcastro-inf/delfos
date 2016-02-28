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
package delfos.rs.contentbased.vsm;

import java.util.TreeMap;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.features.Feature;
import delfos.rs.contentbased.vsm.booleanvsm.profile.BooleanUserProfile;

/**
 * Provee los métodos para convertir un perfil de usuario de sistema de
 * recomendación basado en contenido en un vector de valores.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 01-Mar-2013
 * @deprecated 10-Octubre-2013 Ya no se hace de esta manera, sino que se
 * consulta al modelo para que genere los vectores.
 */
@Deprecated
public class ProfileToVector {

    /**
     * Índice que almacena el orden de los valores de las características para
     * cada dataset de contenido.
     */
    private static final TreeMap<ContentDataset, TreeMap<Feature, TreeMap<String, Integer>>> booleanIndexes = new TreeMap<ContentDataset, TreeMap<Feature, TreeMap<String, Integer>>>();
    /**
     * Número de características en cada dataset de contenido.
     */
    private static final TreeMap<ContentDataset, Integer> sizeOfBooleanVector = new TreeMap<ContentDataset, Integer>();

    /**
     * Genera el orden para cada valor de cada característica en el dataset de
     * contenido dado.
     *
     * @param contentDataset Dataset de contenido para el que se desea generar
     * el índice.
     */
    private static void generateBooleanIndex(ContentDataset contentDataset) {

        if (!booleanIndexes.containsKey(contentDataset)) {
            TreeMap<Feature, TreeMap<String, Integer>> thisDatasetIndex = new TreeMap<Feature, TreeMap<String, Integer>>();
            booleanIndexes.put(contentDataset, thisDatasetIndex);
            int index = 0;
            for (Feature itemFeature : contentDataset.getFeatures()) {
                TreeMap<String, Integer> thisFeatureIndex = new TreeMap<String, Integer>();
                thisDatasetIndex.put(itemFeature, thisFeatureIndex);
                for (Object featureValue : contentDataset.getAllFeatureValues(itemFeature)) {
                    thisFeatureIndex.put(featureValue.toString(), index++);
                }
            }
            sizeOfBooleanVector.put(contentDataset, index);
        }
    }

    /**
     * Devuelve el índice del valor de la característica en el dataset de
     * contenido indicado.
     *
     * @param contentDataset Dataset de contenido.
     * @param itemFeature Característica.
     * @param value Valor de la característica.
     * @return Índice de la característica.
     */
    private static int indexOf_inBoolean(ContentDataset contentDataset, Feature itemFeature, Object value) {
        if (booleanIndexes.containsKey(contentDataset)) {
            //Conozco el dataset
            if (booleanIndexes.get(contentDataset).containsKey(itemFeature)) {
                //Conozco la característica
                if (booleanIndexes.get(contentDataset).get(itemFeature).containsKey(value.toString())) {
                    return booleanIndexes.get(contentDataset).get(itemFeature).get(value.toString());
                } else {
                    throw new IllegalArgumentException("No se conoce el valor de la característica");
                }
            } else {
                throw new IllegalArgumentException("No se conoce la característica");
            }
        } else {
            throw new IllegalArgumentException("No se conoce el dataset");
        }
    }

    /**
     * Extrae el vector asociado al producto a partir del dataset de contenido
     * al que pertenece.
     *
     * @param contentDataset Dataset de contenido.
     * @param item Producto que se desea convertir a vector.
     * @return Vector de valores asociado al producto.
     */
    public static double[] getBooleanVectorProfile_Values(ContentDataset contentDataset, Item item) {
        if (contentDataset == null) {
            throw new IllegalArgumentException("The content dataset cant be null");
        }
        if (item == null) {
            throw new IllegalArgumentException("The item profile cant be null");
        }
        if (!booleanIndexes.containsKey(contentDataset)) {
            generateBooleanIndex(contentDataset);
        }
        double[] ret = new double[sizeOfBooleanVector.get(contentDataset)];
        for (Feature itemFeature : item.getFeatures()) {
            Object value = item.getFeatureValue(itemFeature);
            int index = indexOf_inBoolean(contentDataset, itemFeature, value);
            ret[index] = 1;
        }
        return ret;
    }

    /**
     * Extrae el vector asociado al usuario a partir del dataset de contenido al
     * que pertenece.
     *
     * @param contentDataset Dataset de contenido.
     * @param userProfile Perfil de usuario que se desea convertir a vector.
     * @return Vector de valores asociado al usuario.
     */
    public static double[] getBooleanVectorProfile_Values(ContentDataset contentDataset, BooleanUserProfile userProfile) {

        if (contentDataset == null) {
            throw new IllegalArgumentException("The content dataset cant be null");
        }
        if (userProfile == null) {
            throw new IllegalArgumentException("The user profile cant be null");
        }

        if (!booleanIndexes.containsKey(contentDataset)) {
            generateBooleanIndex(contentDataset);
        }
        double[] ret = new double[sizeOfBooleanVector.get(contentDataset)];

        for (Feature itemFeature : userProfile.getFeatures()) {
            for (Object value : userProfile.getValuedFeatureValues(itemFeature)) {
                int index = indexOf_inBoolean(contentDataset, itemFeature, value);
                ret[index] = (double) userProfile.getFeatureValueValue(itemFeature, value);
            }
        }
        return ret;
    }

    /**
     * Extrae el vector de ponderaciones del usuario a partir del dataset de
     * contenido al que pertenece.
     *
     * @param contentDataset Dataset de contenido.
     * @param userProfile Perfil de usuario que se desea convertir a vector.
     * @return Vector de ponderación de características del usuario.
     */
    public static double[] getBooleanVectorProfile_Weights(ContentDataset contentDataset, BooleanUserProfile userProfile) {
        if (contentDataset == null) {
            throw new IllegalArgumentException("The content dataset cant be null");
        }
        if (userProfile == null) {
            throw new IllegalArgumentException("The user profile cant be null");
        }
        if (!booleanIndexes.containsKey(contentDataset)) {
            generateBooleanIndex(contentDataset);
        }
        double[] ret = new double[sizeOfBooleanVector.get(contentDataset)];
        for (Feature itemFeature : userProfile.getFeatures()) {
            for (Object value : userProfile.getValuedFeatureValues(itemFeature)) {
                int index = indexOf_inBoolean(contentDataset, itemFeature, value);
                ret[index] = (double) userProfile.getFeatureValueWeight(itemFeature, value);
            }
        }
        return ret;
    }

    public static String printBooleanProfile(ContentDataset contentDataset, BooleanUserProfile userProfile) {
        if (contentDataset == null) {
            throw new IllegalArgumentException("The content dataset cant be null");
        }
        if (userProfile == null) {
            throw new IllegalArgumentException("The user profile cant be null");
        }

        StringBuilder ret = new StringBuilder();

        ret.append("Profile of user ");
        ret.append(userProfile.getId());
        ret.append("\n");

        {
            double[] booleanVectorProfile_Values = getBooleanVectorProfile_Values(contentDataset, userProfile);
            ret.append("Values:  ");
            for (double value : booleanVectorProfile_Values) {
                ret.append(value).append("\t");
            }
            ret.append("\n");
        }

        double[] booleanVectorProfile_Weights = getBooleanVectorProfile_Weights(contentDataset, userProfile);
        ret.append("Weights: ");
        for (double value : booleanVectorProfile_Weights) {
            ret.append(value).append("\t");
        }
        ret.append("\n");

        return ret.toString();
    }

    public static TreeMap<Feature, TreeMap<String, Integer>> getFeatureValueIndex(ContentDataset contentDataset) {
        if (!booleanIndexes.containsKey(contentDataset)) {
            generateBooleanIndex(contentDataset);
        }
        return booleanIndexes.get(contentDataset);
    }
}
