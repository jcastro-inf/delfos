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
package delfos.io.excel.parameterowner;

import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Class to extract different Excel relevant data from a ParameterOwner.
 *
 * @author Jorge Castro Gallardo
 */
public class ParameterOwnerExcel {

    public static Map<String, Object> extractParameterValues(ParameterOwner parameterOwner) {

        Map<String, Object> parameterValues = new TreeMap<>();

        parameterValues.put(parameterOwner.getParameterOwnerType().name(), parameterOwner.getName());

        List<Parameter> parameters = parameterOwner.getParameters().stream().sorted().collect(Collectors.toList());

        String prefix = parameterOwner.getParameterOwnerType().name() + "." + parameterOwner.getName();

        for (Parameter parameter : parameters) {
            Object parameterValue = parameterOwner.getParameterValue(parameter);

            parameterValues.put(prefix + "." + parameter.getName(), parameterValue.toString());

            if (parameterValue instanceof ParameterOwner) {
                ParameterOwner parameterValueParameterOwner = (ParameterOwner) parameterValue;

                Map<String, Object> parameterValueParameterValues = extractParameterValues(parameterValueParameterOwner);

                for (String innerKey : parameterValueParameterValues.keySet()) {
                    parameterValues.put(prefix + "." + innerKey, parameterValueParameterValues.get(innerKey));
                }
            }
        }

        return parameterValues;

    }

}
