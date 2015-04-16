package delfos.rs.trustbased.belieffunctions;

/**
 *
 * @version 14-abr-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class Type2Belief extends BeliefFunction {

    @Override
    public double beliefFromCorrelation(double correlation) {

        double arcsin = Math.asin(correlation);

        double inside = arcsin / Math.PI;

        double ret = 0.5 + inside;

        return ret;
    }

}
