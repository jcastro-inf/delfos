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
package delfos.common.parameters;

/**
 * Interfaz que deben implementar todos los objetos que deseen ser notificados
 * de los cambios de los parámetros de algún objeto que herede de
 * {@link ParameterOwner}
 *
* @author Jorge Castro Gallardo
 * 
 * @version 1.0 Unknown date
 * @version 1.1 22-Feb-2013
 */
public interface ParameterListener {

    /**
     * Método que los objetos se invoca cuando ocurren cambios en los parámetros
     * del {@link ParameterOwner} en el que se ha registrado.
     */
    public void parameterChanged();
}
