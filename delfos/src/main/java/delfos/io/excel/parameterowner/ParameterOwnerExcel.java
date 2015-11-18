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
