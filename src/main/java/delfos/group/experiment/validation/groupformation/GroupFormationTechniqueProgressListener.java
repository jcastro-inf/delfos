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
package delfos.group.experiment.validation.groupformation;

/**
 * Interfaz que define el método que se invoca cuando el progreso de cálculo de
 * los grupos de usuarios mediante el método {@link GroupFormationTechnique#shuffle()
 * }
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public interface GroupFormationTechniqueProgressListener {

    /**
     * Método para notificar a las clases listener de un cambio en el progreso
     * de formación de los grupos e indicar el tiempo restante.
     *
     * @param message Mensaje que informa de la fase en la que se encuentra
     * @param progressPercent Percent of completed job
     * @param remainingTimeInMS Estimated remaining time in miliseconds
     */
    public void progressChanged(String message, int progressPercent, long remainingTimeInMS);

    public default void progressChanged(String message, int progressPercent) {
        progressChanged(message, progressPercent, -1);
    }
}
