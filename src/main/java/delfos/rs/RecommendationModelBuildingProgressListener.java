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
package delfos.rs;

/**
 * Interfaz que define el método de notificación del progreso de la tarea de de
 * construcción del sistema de recomendación.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public interface RecommendationModelBuildingProgressListener {

    /**
     * Método que se invocará cuando el sistema de recomendación tenga un
     * progreso en el cálculo del modelo
     *
     * @param actualJob Nombre de la tarea actual
     * @param percent Porcentaje completado de la tarea actual [0-100]
     * @param remainingTime Tiempo restante que le queda para terminar la tarea
     * actual. Si el parámetro es negativo, se considerará desconocido.
     */
    public void buildingProgressChanged(String actualJob, int percent, long remainingTime);
}
