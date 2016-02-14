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
package delfos.main.managers;

import delfos.ConsoleParameters;
import delfos.common.Global;

/**
 * Denotes a case use that is also a mode. A mode has a specific command line
 * parameter, that can be queried through the method {@link CaseUseMode#getModeParameter()
 * }.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public abstract class CaseUseMode {

    /**
     * Returns the parameter that activates this manager. It is a mandatory
     * parameter.
     *
     * @return
     */
    public abstract String getModeParameter();

    /**
     * Comprueba si este es el manager correcto para la linea de comandos
     * especificada.
     *
     * This method can be overrided to support deprecated parameters.
     *
     * @param consoleParameters Parámetros de la línea de comandos.
     * @return true si es el manager adecuado.
     */
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        try {
            return consoleParameters.isFlagDefined(getModeParameter());
        } catch (IllegalArgumentException ex) {
            Global.showWarning(this.getClass().toString() + ": This class mode parameter is not a flag!");
            Global.showError(ex);
            throw ex;
        }
    }

    /**
     * Ejecuta las tareas necesarias de este caso de uso.
     *
     * @param consoleParameters Parámetros de la línea de comandos.
     */
    public abstract void manageCaseUse(ConsoleParameters consoleParameters);

    /**
     * Returns a multiline and friendly-user help for this mode.
     *
     * @return
     */
    public String getUserFriendlyHelpForThisCaseUse() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Returns the usage synopsis, which is a one-line pseudo-regular expression
     * that generates the allowed inputs for this mode,
     * e.g.,<code>--single-user [--build | --recommend -u \\<idUser\\> </code>].
     * This input indicates that the single user mode has two sub-modes: build
     * and recommend. Recommend mode must take the -u parameter that states the
     * idUser used in the recommend mode.
     *
     * @return Usage synopsis.
     */
    public String getUsageSynopsis() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
