/* 
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package delfos.rs.trustbased.belieffunctions;

import delfos.factories.Factory;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
