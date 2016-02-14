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
package delfos.experiment.validation.validationtechnique;

/**
 * Interfaz que deben implementar los objetos que deseen registrar el progreso
 * de ejecución de una validación
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 * @see ValidationTechnique#shuffle()
 */
public interface ValidationTechniqueProgressListener {

    /**
     * Metodo que se invoca cuando la técnica de validación observada cambia.
     *
     * @param message Tarea que está realizando.
     * @param percent Porcentaje completado de la tarea.
     */
    public void progressChanged(String message, int percent);
}
