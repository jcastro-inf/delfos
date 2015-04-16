package delfos.rs.trustbased.belieffunctions;

import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;

/**
 *
 * @version 14-abr-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class Type4Belief extends BeliefFunction {

    public static final Parameter K_PARAMETER = new Parameter("K", new IntegerParameter(1, 100, 3));

    public Type4Belief() {
        super();
        addParameter(K_PARAMETER);
    }

    public Type4Belief(int k) {
        this();

        setParameterValue(K_PARAMETER, k);
    }

    @Override
    public double beliefFromCorrelation(double correlation) {

        double k = ((Number) getParameterValue(K_PARAMETER)).doubleValue();

        double ret = 0.5 * (1 + Math.pow(correlation, k));

        return ret;
    }

}
