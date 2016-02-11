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
package delfos.common.parameters.chain;

import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;

/**
 *
 * @author jcastro
 */
class Node {

    private final Parameter parameter;
    private final ParameterOwner parameterOwner;

    public Node(Parameter parameter, ParameterOwner parameterOwner) {
        this.parameter = parameter;
        this.parameterOwner = parameterOwner;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public ParameterOwner getParameterOwner() {
        return parameterOwner;
    }

    boolean isCompatibleWith(Node firstChainNode) {
        if (parameterOwner == null || firstChainNode.parameterOwner == null) {
            return true;
        }

        boolean parameterOwnerAreSameClass = parameterOwner.getClass().equals(firstChainNode.parameterOwner.getClass());

        if (!parameterOwnerAreSameClass) {
            return false;
        }

        boolean nodeParameterAreSame = parameter.equals(firstChainNode.parameter);

        return nodeParameterAreSame;
    }

}
