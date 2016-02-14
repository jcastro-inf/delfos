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
package delfos.io.xml.casestudy;

import java.util.LinkedList;
import java.util.List;
import delfos.common.Global;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;

/**
 * Convierte los parámetros en nombres del estilo de los paquetes. Es decir, si
 * hay un párametro que es un sistema de recomendación llamado SREjemplo y éste
 * tiene un parámetro llamado Vecinos, el nombre será SREjemplo.Vecinos
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 19-Noviembre-2013
 */
public class ParameterOwnerParameterNamesXML {

    /**
     * Devuelve los nombres de los parámetros. Lo hace recursivamente para
     * completar los prefijos de los parámetros.
     *
     * @param parameterOwner
     * @return
     */
    public static List<String> getParameterNames(ParameterOwner parameterOwner) {

        List<String> ret = new LinkedList<>();

        ret.add(parameterOwner.getParameterOwnerType().name());
        for (Parameter parameter : parameterOwner.getParameters()) {

            if (parameter.getRestriction() instanceof ParameterOwnerRestriction) {
                //TODO: Aquí había un throw new IllegalArgumentException("arg");, averiguar por qué y si está bien quitarlo directamente
                Global.showInfoMessage(parameterOwner.getAlias() + " --> " + parameter.getName() + "(" + parameter.getRestriction().getName() + ")\n");
            }

            ret.add(parameterOwner.getName() + "." + parameter.getName());
        }

        return ret;
    }

    public static Object getParameterValueByName(ParameterOwner parameterOwner, String ownerAndParameterNames) {
        if (ownerAndParameterNames.indexOf(".") == -1) {
            return parameterOwner.getName();
        }

        String parameterOwnerName = ownerAndParameterNames.substring(0, ownerAndParameterNames.indexOf("."));
        String parameterName = ownerAndParameterNames.substring(ownerAndParameterNames.indexOf(".") + 1, ownerAndParameterNames.length());
        if (parameterOwner.getName().equals(parameterOwnerName)) {
            if (parameterName.contains(".")) {
                throw new IllegalArgumentException("Not supported.");
            }

            Parameter parameter = parameterOwner.getParameterByName(parameterName);
            if (parameter == null) {
                return null;
            }
            return parameterOwner.getParameterValue(parameter);
        }

        return null;
    }

}
