package delfos.dataset.basic.rating.domain;

import java.io.Serializable;

public abstract class Domain implements Serializable {

    public abstract Number getValueAssociatedToProbability(Number value);

    public abstract Number max();

    public abstract Number min();

    public abstract Number trimValueToDomain(Number preference);

    public Number convertToDomain(Number valueInThisDomain, Domain destinyDomain) {
        if (destinyDomain instanceof IntegerDomain) {
            IntegerDomain integerDomain = (IntegerDomain) destinyDomain;
            return convertToIntegerDomain(valueInThisDomain, integerDomain);
        }

        if (destinyDomain instanceof DecimalDomain) {
            DecimalDomain decimalDomain = (DecimalDomain) destinyDomain;
            return convertToDecimalDomain(valueInThisDomain, decimalDomain);
        }

        throw new IllegalStateException("Unknown destiny domain type '" + destinyDomain.getClass() + "'");
    }

    public abstract Number convertToDecimalDomain(Number valueInThisDomain, DecimalDomain destinyDomain);

    public abstract Number convertToIntegerDomain(Number valueInThisDomain, IntegerDomain destinyDomain);

    public static long drawnInteger(double value, IntegerDomainWithProbabilities domain) {
        return domain.getValueAssociatedToProbability(value);
    }

    public abstract Number width();

    public abstract Number mean();

}
