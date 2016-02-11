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
package delfos.experiment.casestudy;

/**
 * Define los métodos que un listener del proceso de ejecución de una ejecución
 * debe implementar para ser notificado de los cambios en el mismo.
 *
* @author Jorge Castro Gallardo
 */
public interface ExecutionProgressListener {

    /**
     *
     * @param proceso Nombre de la tarea que se está ejecutando.
     * @param percent Porcentaje de la tarea actual.
     * @param remainingMiliSeconds Tiempo restante de la tarea actual
     */
    public void executionProgressChanged(String proceso, int percent, long remainingMiliSeconds);
}
