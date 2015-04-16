package delfos.common.aggregationoperators.penalty.functions;

import delfos.factories.Factory;

/**
 *
* @author Jorge Castro Gallardo
 */
public class PenaltyFuncionsFactory extends Factory<PenaltyFunction> {

    private static final PenaltyFuncionsFactory instance;

    public static PenaltyFuncionsFactory getInstance() {
        return instance;
    }

    static {
        instance = new PenaltyFuncionsFactory();

        instance.addClass(PenaltyInitialFunction.class);
        instance.addClass(ErrorPenalty.class);

        instance.addClass(PenaltyWholeMatrix.class);
        instance.addClass(NoPenalty.class);
    }

    private PenaltyFuncionsFactory() {
    }

}
