/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delfos.rs.trustbased.belieffunctions;

/**
 *
 * @version 14-abr-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class LinearBelief extends BeliefFunction {

    @Override
    public double beliefFromCorrelation(double correlation) {
        if (correlation > 1 || correlation < -1) {
            throw new IllegalArgumentException("The correlation must be given in [-1,1] ( value " + correlation + ")");
        }

        return (correlation + 1) / (2.0);
    }

}
