package delfos.rs.hybridtechniques;

import java.io.Serializable;

/**
 *
 * @author Jorge Castro Gallardo (Sinbad2,Universidad de Jaén)
 * @version 1.0 28-May-2013
 */
public class HybridRecommenderSystemModel implements Serializable {
    
    private static final long serialVersionUID = 114;

    private final Object[] model;

    protected HybridRecommenderSystemModel() {
        model = null;
    }

    public HybridRecommenderSystemModel(Object... model) {
        this.model = model;
    }

    public Object getModel(int index) {
        return model[index];
    }
}
