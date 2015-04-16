package delfos.common.fuzzylabels;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 16-ene-2014
 */
public class FuzzyLabel {

    private final double _a;
    private final double _b;
    private final double _c;
    private final double _d;

    protected FuzzyLabel(double _a, double _b, double _c, double _d) {
        this._a = _a;
        this._b = _b;
        this._c = _c;
        this._d = _d;

        if (_a <= _b && _b <= _c && _c <= _d) {
//OK
        } else {
            throw new IllegalArgumentException("Bad definition ("
                    + " a < b < c < d "
                    + " --> "
                    + _a + " < " + _b + " < " + _c + " < " + _d
                    + ")");
        }
    }

    public double alphaCut(double value) {

        if (value < 0 || value > 1.0001) {
            throw new IllegalArgumentException("Value must be given in [0,1] interval and it was " + value);
        }

        if (value <= _a) {
            return 0;
        }

        if (_a < value && value < _b) {

            double ret;
            ret = (value - _a) / (_b - _a);
            return ret;
        }

        if (_b <= value && value <= _c) {
            return 1;
        }

        if (_c < value && value < _d) {
            double ret;
            ret = (_d - value) / (_d - _c);
            return ret;
        }

        if (value >= _d) {
            return 0;
        }

        throw new IllegalArgumentException("Imposible situation!");
    }

    public static FuzzyLabel createAscendentLabel(double a, double b) {
        return new FuzzyLabel(a, b, 1, 1);
    }

    public static FuzzyLabel createDescendentLabel(double a, double b) {
        return new FuzzyLabel(0, 0, a, b);
    }
}
