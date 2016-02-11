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
