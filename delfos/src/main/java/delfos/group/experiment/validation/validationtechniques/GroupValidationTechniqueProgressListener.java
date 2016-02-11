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
package delfos.group.experiment.validation.validationtechniques;

/**
 * Interfaz que deben implementar los objetos que deseen registrar el progreso
 * de ejecución de una validación
 *
* @author Jorge Castro Gallardo
 * @see GroupValidationTechnique#shuffle() 
 *
 * @version 1.0 (12/12/2012)
 */
public interface GroupValidationTechniqueProgressListener {

    /**
     * Método para informar que el progreso de ejecución ha cambiado.
     *
     * @param message Tarea actual
     * @param percent Porcentaje completado
     */
    public void progressChanged(String message, int percent);
}
