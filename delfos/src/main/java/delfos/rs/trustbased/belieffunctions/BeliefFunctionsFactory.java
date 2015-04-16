package delfos.rs.trustbased.belieffunctions;

import delfos.factories.Factory;

/**
 *
 * @author jcastro
 */
public class BeliefFunctionsFactory extends Factory<BeliefFunction> {

    static {
        getInstance().addClass(LinearBelief.class);
        getInstance().addClass(Type1Belief.class);
        getInstance().addClass(Type2Belief.class);
        getInstance().addClass(Type3Belief.class);
        getInstance().addClass(Type4Belief.class);
    }

    private BeliefFunctionsFactory() {
    }

    public static BeliefFunctionsFactory getInstance() {
        return BeliefFuncitonsFactoryHolder.INSTANCE;
    }

    private static class BeliefFuncitonsFactoryHolder {

        private static final BeliefFunctionsFactory INSTANCE = new BeliefFunctionsFactory();
    }
}
