package delfos.group.grs.itemweighted;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Modelo de un grupo que almacena la valoración del grupo para cada producto.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 29-May-2013
 */
public class GroupModelPseudoUser_itemWeighted implements Serializable {

    private static final long serialVersionUID = 124L;

    private final Map<Integer, Number> ratings;
    private final Map<Integer, Double> itemWeights;
    private final GroupOfUsers group;

    public GroupModelPseudoUser_itemWeighted(Map<Integer, Number> ratings, Map<Integer, Double> itemWeights, GroupOfUsers group) {
        this.ratings = ratings;
        this.itemWeights = itemWeights;
        this.group = group;
    }

    public Map<Integer, Number> getRatings() {
        return Collections.unmodifiableMap(ratings);
    }

    public Map<Integer, Double> getItemWeights() {
        return itemWeights;
    }

    public GroupOfUsers getGroup() {
        return group;
    }
}
