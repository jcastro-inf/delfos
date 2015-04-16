/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delfos.dataset.basic.loader.types;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;

/**
 * Interfaz que deben implementar los objetos que almacenen un dataset de
 * valoraciones.
 *
* @author Jorge Castro Gallardo
 *
 * @version 26-Noviembre-2013
 * @param <RatingType> Tipo de los ratings del dataset de valoraciones.
 */
public interface RatingsDatasetLoader<RatingType extends Rating> {

    /**
     * Obtiene el dataset de ratings en memoria que se usará en la
     * recomendación.
     *
     * @return dataset de ratings que se usará en la recomendación NOTA:<br> El
     * dataset completo será cargado en memoria, por lo que los requerimientos
     * de memoria RAM se incrementarán según el tamaño del dataset. Es
     * aconsejable que este tipo de dataset se utilice solo en la fase de
     * evaluación y no en los sistemas de recomendación en entorno de producción
     */
    public RatingsDataset<RatingType> getRatingsDataset() throws CannotLoadRatingsDataset;

    /**
     * Devuelve el criterio de relevancia que se usará para decidir si una
     * valoración de un producto es positiva para el usuario o no
     *
     * Por ejemplo, en Netflix las valoraciones se dan de 1 a 5 y el criterio de
     * relevancia más utilizado es 4, considerándose las valoracionse iguales o
     * superiores a cuatro como positivas. El resto son valoraciones negativas
     *
     * @return Criterio de relevancia del dataset
     *
     */
    public RelevanceCriteria getDefaultRelevanceCriteria();

}
