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
* @author Jorge Castro Gallardo
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
