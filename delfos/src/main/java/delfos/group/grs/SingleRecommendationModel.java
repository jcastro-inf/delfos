package delfos.group.grs;

import java.io.Serializable;

/**
 *
* @author Jorge Castro Gallardo
 * @version 1.0 29-May-2013
 */
public class SingleRecommendationModel implements Serializable{
    
    private static final long serialVersionUID = 121L;

    private final Object RecommendationModel;

    public SingleRecommendationModel(Object RecommendationModel) {
        this.RecommendationModel = RecommendationModel;
    }

    public Object getRecommendationModel() {
        return RecommendationModel;
    }
}
