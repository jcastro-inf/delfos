package delfos.group.grs.aggregation;

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
public class GroupModelPseudoUser implements Serializable {

    private static final long serialVersionUID = 124L;

    private final Map<Integer, Number> ratings;
    private final GroupOfUsers group;

    public GroupModelPseudoUser(GroupOfUsers group, Map<Integer, Number> ratings) {
        this.ratings = ratings;
        this.group = group;
    }

    public Map<Integer, Number> getRatings() {
        return Collections.unmodifiableMap(ratings);
    }

    public GroupOfUsers getGroup() {
        return group;
    }
}
