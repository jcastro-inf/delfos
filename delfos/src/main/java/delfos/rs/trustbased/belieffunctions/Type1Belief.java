package delfos.rs.trustbased.belieffunctions;

/**
 *
 * @version 14-abr-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class Type1Belief extends BeliefFunction {

    @Override
    public double beliefFromCorrelation(double correlation) {

        double ret;

        final double radians = correlation * (Math.PI / 2);

        double sin = Math.sin(radians);

        sin++;

        ret = 0.5 * sin;

        return ret;
    }

}
