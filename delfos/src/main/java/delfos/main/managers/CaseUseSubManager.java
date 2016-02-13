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

/**
 * Denotes a class who implements the behaviour associated to a specific command
 * line parameter combination.
 *
 * @version 21-oct-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public abstract class CaseUseSubManager {

    private final CaseUseModeWithSubManagers parent;

    public CaseUseSubManager(CaseUseModeWithSubManagers parent) {
        this.parent = parent;
    }

    /**
     * Comprueba si este es el manager correcto para la linea de comandos
     * especificada.
     *
     * @param consoleParameters Parámetros de la línea de comandos.
     * @return true si es el manager adecuado.
     */
    public abstract boolean isRightManager(ConsoleParameters consoleParameters);

    /**
     * Ejecuta las tareas necesarias de este caso de uso.
     *
     * @param consoleParameters Parámetros de la línea de comandos.
     */
    public abstract void manageCaseUse(ConsoleParameters consoleParameters);

    public CaseUseModeWithSubManagers getParent() {
        return parent;
    }
}
