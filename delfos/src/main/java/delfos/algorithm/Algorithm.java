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
package delfos.algorithm;

import delfos.common.parameters.ParameterOwner;
import delfos.experiment.SeedHolder;

/**
 * Interfaz que define los métodos que un experimento debe implementar para la
 * notificación de su progreso de ejecución.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 17-Julio-2013
 */
public interface Algorithm extends SeedHolder, ParameterOwner {

    /**
     * Obtiene el progreso del algoritmo.
     *
     * @return Porcentaje completado del algoritmo.
     */
    public int getProgressPercent();

    /**
     * Obtiene el tiempo restante del algoritmo .
     *
     * @return Tiempo en milisegundos.
     */
    public long getProgressRemainingTime();

    /**
     * Obtiene la tarea que se está ejecutando en la ejecución .
     *
     * @return Descripción de la tarea.
     */
    public String getProgressTask();

    /**
     * Dispara el evento de cambio en el progreso de ejecución del experimento,
     * notificando a todos los listener registrados
     *
     * @param task Nombre de la tarea que se está realizando.
     * @param percent Porcentaje realizado.
     * @param remainingTime Tiempo restante estimado en milisegundos.
     */
    public void fireProgressChanged(String task, int percent, long remainingTime);

    /**
     * Añade el objeto para que sea notificado de cambios en el progreso del
     * algoritmo.
     *
     * @param listener Objeto que desea ser notificado.
     */
    public void addProgressListener(AlgorithmProgressListener listener);

    /**
     * Elimina el objeto para que no sea notificado de cambios en el progreso
     * del algoritmo.
     *
     * @param listener Objeto que no desea ser notificado.
     */
    public void removeProgressListener(AlgorithmProgressListener listener);
}
