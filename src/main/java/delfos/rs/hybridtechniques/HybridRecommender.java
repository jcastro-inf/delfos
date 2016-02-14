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
package delfos.rs.hybridtechniques;

import java.util.List;
import delfos.rs.RecommenderSystem;
import delfos.rs.RecommenderSystemAdapter;

/**
 * Interfaz que define la funcionalidad que debe ofrecer un sistema de
 * recomendación híbrido, es decir, que combina el funcionamiento de varios
 * sistemas de recomendación básicos.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.1 21-Jan-2013
 * @version 1.0 Unknow date
 * @param <RecommendationModel>
 */
public abstract class HybridRecommender<RecommendationModel> extends RecommenderSystemAdapter<RecommendationModel> {

    /**
     * Devuelve los sistemas de recomendación que hibrida este sistema.
     *
     * @return Lista de sistemas de recomendación hibridados
     */
    protected abstract List<RecommenderSystem<Object>> getHybridizedRecommenderSystems();

    @Override
    public final boolean isRatingPredictorRS() {
        return false;
    }
}
