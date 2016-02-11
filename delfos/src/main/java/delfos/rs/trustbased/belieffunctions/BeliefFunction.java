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

import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;

/**
 *
 * @version 14-abr-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public abstract class BeliefFunction extends ParameterOwnerAdapter {

    @Override
    public final ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.BELIEF_FUNCTION;
    }

    public abstract double beliefFromCorrelation(double correlation);

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof BeliefFunction) {

            BeliefFunction beliefFunction = (BeliefFunction) obj;

            Set<Parameter> thisParameters = new TreeSet<Parameter>(this.getParameters());
            Set<Parameter> otherParameters = new TreeSet<Parameter>(beliefFunction.getParameters());

            EqualsBuilder equalsBuilder = new EqualsBuilder();

            equalsBuilder = equalsBuilder
                    .append(this.getClass(), beliefFunction.getClass())
                    .append(thisParameters, otherParameters);

            return equalsBuilder.isEquals();
        } else {
            return false;
        }

    }

    @Override
    public int hashCode() {

        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(17, 31).append(this.getClass());

        for (Parameter p : this.getParameters()) {
            Object parameterValue = this.getParameterValue(p);
            hashCodeBuilder = hashCodeBuilder.append(parameterValue);
        }

        return hashCodeBuilder.hashCode();
    }

}
