package delfos.factories;

import delfos.rs.collaborativefiltering.predictiontechniques.ItemAverageAdjustment;
import delfos.rs.collaborativefiltering.predictiontechniques.PredictionTechnique;
import delfos.rs.collaborativefiltering.predictiontechniques.WeightedSum;

/**
 * Clase que almacena las clases que implementan una técnica de predicción.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 (18-02-2013) Restringidas las clases que se indican a clases que
 * extienden {@link PredictionTechnique}
 * @version 2.0 9-Mayo-2013 Ahora la clase hereda de {@link Factory}.
 */
public class PredictionTechniquesFactory extends Factory<PredictionTechnique> {

    private static final PredictionTechniquesFactory instance;

    static {
        instance = new PredictionTechniquesFactory();
        instance.addClass(WeightedSum.class);
        instance.addClass(ItemAverageAdjustment.class);
    }

    private PredictionTechniquesFactory() {
    }

    public static PredictionTechniquesFactory getInstance() {
        return instance;
    }
}
