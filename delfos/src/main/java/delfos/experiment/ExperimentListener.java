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
package delfos.experiment;

/**
 * Interfaz que define el método que se invoca para notificar del cambio de 
 * estado en la ejecución de un experimento
 * 
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @see ExperimentProgress
 * @see AlgorithmExperimentAdapter
 */
public interface ExperimentListener {
    
    /**
     * Este método debe implementar el comportamiento adecuado cuando cambia el
     * estado de ejecución del algoritmo que se observa.
     * @param algorithmExperiment Algoritmo que lanza el evento
     */
    public void progressChanged(ExperimentProgress algorithmExperiment);
}
