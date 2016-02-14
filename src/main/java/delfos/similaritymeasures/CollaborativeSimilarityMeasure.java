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
package delfos.similaritymeasures;

import java.util.Collection;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.collaborativefiltering.knn.CommonRating;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.collaborativefiltering.knn.modelbased.KnnModelBasedCFRS;
import delfos.rs.RecommenderSystemAdapter;
import delfos.common.exceptions.CouldNotComputeSimilarity;

/**
 * Interfaz para definir una medida de similitud especialmente diseñada para ser
 * utilizada en los sistemas de recomendación colaborativos
 * {@link KnnMemoryBasedCFRS} y/o {@link KnnModelBasedCFRS}. Para ello hace uso
 * de la estructura {@link CommonRating}.
 *
 * @see KnnMemoryBasedCFRS
 * @see KnnModelBasedCFRS
 * @see CommonRating
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public interface CollaborativeSimilarityMeasure extends SimilarityMeasure {

    /**
     * Función que se usa para obtener la similitud de dos entidades de
     * recomendación.
     *
     * @param commonRatings Valoraciones en común de las entidades
     * @param ratings Dataset de ratings. Se usa para obtener valoraciones
     * medias en caso de ser necesario
     * @return similitud de las entidades, 0 si son distintas 1 si son iguales.
     *
     * @throws CouldNotComputeSimilarity Si no se puede calcular la similitud,
     * porque no existen datos suficientes para ello.
     */
    public float similarity(Collection<CommonRating> commonRatings, RatingsDataset<? extends Rating> ratings) throws CouldNotComputeSimilarity;

    /**
     * Devuelve true si la medida de similitud se puede utilizar con un sistema
     * de recomendación, falso en otro caso.
     *
     * @param rs Sistema de recomendación para el que se desea saber si funciona
     * la medida de similitud
     * @return true si la medida de similitud se puede usar en el sistema de
     * recomendación especificado
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public boolean RSallowed(Class<? extends RecommenderSystemAdapter> rs);
}
