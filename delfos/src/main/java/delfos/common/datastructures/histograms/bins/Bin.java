package delfos.common.datastructures.histograms.bins;

/**
 *
 * @version 01-jul-2014
* @author Jorge Castro Gallardo
 */
public class Bin {

    private final double minValue;
    private final double maxValue;
    private final double numberOfValues;

    public Bin(double minValue, double maxValue, double numberOfValues) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.numberOfValues = numberOfValues;
    }

}
