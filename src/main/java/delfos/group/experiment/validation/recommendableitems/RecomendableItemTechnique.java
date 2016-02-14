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
package delfos.group.experiment.validation.recommendableitems;

import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.RatingsDataset;
import java.util.Collection;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Encapsula el comportamiento de una técnica para el cálculo de posibles
 * recomendaciones. En la bibliografía se han encontrado dos técnicas:
 *
 * Recomendación de productos de un solo uso: Productos que no se hayan
 * experimentado
 *
 * Recomendacion de productos de multiples usos: Se pueden recomendar productos
 * que ya se hayan experimentado
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public abstract class RecomendableItemTechnique {

    //TODO: completar javadoc
    /**
     * Dado un grupo de usuarios y los datasets de valoraciones, calcula qué
     * productos se pueden recomendar
     *
     * @param groupOfUsers Grupo para el que se calculan las alternativas
     * disponibles
     * @param ratingsDataset Dataset de valoraciones para comprobar qué
     * productos han valorado los miembros,
     * @param contentDataset Conjunto de todos los productos
     * @return Colección con los productos que el sistema de recomendación debe
     * tener en cuenta para recomendar
     */
    public abstract Collection<Integer> getRecommendableItems(GroupOfUsers groupOfUsers, RatingsDataset<? extends Rating> ratingsDataset, ContentDataset contentDataset);
}
