package delfos.factories;

import delfos.rs.trustbased.implicittrustcomputation.ShambourLu_UserBasedImplicitTrustComputation;
import delfos.rs.trustbased.JaccardGraph;
import delfos.rs.trustbased.PearsonCorrelationWithPenalty;
import delfos.rs.trustbased.WeightedGraphCalculation;

/**
 * Factoría que conoce los algoritmos de generación de grafos ponderados que la
 * biblioteca de recomendación incorppora.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 11-Septiembre-2013
 *
 * @see WeightedGraphCalculation
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class WeightedGraphCalculatorFactory extends Factory<WeightedGraphCalculation> {

    private static final WeightedGraphCalculatorFactory instance;

    public static WeightedGraphCalculatorFactory getInstance() {
        return instance;
    }

    static {
        instance = new WeightedGraphCalculatorFactory();
        instance.addClass(JaccardGraph.class);
        instance.addClass(ShambourLu_UserBasedImplicitTrustComputation.class);

        instance.addClass(PearsonCorrelationWithPenalty.class);
    }
}
