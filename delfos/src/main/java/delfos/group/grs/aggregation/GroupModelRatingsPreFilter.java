package delfos.group.grs.aggregation;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import delfos.dataset.basic.rating.Rating;

/**
 * Modelo de un grupo que almacena las valoraciones filtradas de los miembros.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 29-May-2013
 */
public class GroupModelRatingsPreFilter implements Serializable {

    private static final long serialVersionUID = 123L;

    private final Map<Integer, Map<Integer, Rating>> ratings;
    private final Object innerGRSGroupModel;

    public GroupModelRatingsPreFilter(Map<Integer, Map<Integer, Rating>> filteredRatings, Object innerGRSGroupModel) {
        this.ratings = filteredRatings;
        this.innerGRSGroupModel = innerGRSGroupModel;
    }

    public Map<Integer, Map<Integer, Rating>> getRatings() {
        return Collections.unmodifiableMap(ratings);
    }

    public Object getInnerGRSGroupModel() {
        return innerGRSGroupModel;
    }
}
