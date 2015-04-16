package delfos.dataset.basic.rating.domain;

import java.io.Serializable;

public interface Domain extends Serializable {

    public Number getValueAssociatedToProbability(Number value);

    public Number max();

    public Number min();

    public Number trimValueToDomain(Number preference);

    public Number convertToDomain(Number valueInThisDomain, DecimalDomain destinyDomain);

    public Number convertToDomain(Number valueInThisDomain, IntegerDomain destinyDomain);

    public static long drawnInteger(double value, IntegerDomainWithProbabilities domain) {
        return domain.getValueAssociatedToProbability(value);
    }

    public Number width();

    public Number mean();

}
