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

import delfos.common.parameters.ParameterOwnerAdapter;
import java.util.LinkedList;

/**
 * Adaptador que implementa la interfaz {@link delfos.algorithm.Algorithm}
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 17-Julio-2013
 */
public abstract class AlgorithmAdapter extends ParameterOwnerAdapter implements Algorithm {

    public AlgorithmAdapter() {
        addParameter(SEED);
    }

    @Override
    public final void setSeedValue(long seedValue) {
        setParameterValue(SEED, seedValue);
    }

    @Override
    public final long getSeedValue() {
        return (Long) getParameterValue(SEED);
    }
    /**
     * Objetos que desean ser notificados de cambios en la ejecución del
     * experimento
     */
    private final LinkedList<AlgorithmProgressListener> progressListeners = new LinkedList<>();
    /**
     * Porcentaje completado de la ejecución actual.
     */
    private int progressPercent = 0;
    /**
     * Predicción de tiempo restante de la ejecución en milisegundos.
     */
    private long progressRemainingTime = -1;
    /**
     * Tarea que se ejecuta actualmente.
     */
    private String progressTask = "";

    /**
     * Dispara el evento de cambio en el progreso de ejecución del experimento,
     * notificando a todos los listener registrados
     */
    @Override
    public void fireProgressChanged(String task, int percent, long remainingTime) {

        progressTask = task;
        progressPercent = percent;

        if (remainingTime != -1) {
            progressRemainingTime = remainingTime;
        }

        for (AlgorithmProgressListener listener : progressListeners) {
            listener.progressChanged(this);
        }
    }

    @Override
    public void addProgressListener(AlgorithmProgressListener listener) {
        progressListeners.add(listener);
        listener.progressChanged(this);
    }

    @Override
    public void removeProgressListener(AlgorithmProgressListener listener) {
        progressListeners.remove(listener);
    }

    @Override
    public int getProgressPercent() {
        return progressPercent;
    }

    @Override
    public long getProgressRemainingTime() {
        return progressRemainingTime;
    }

    @Override
    public String getProgressTask() {
        return progressTask;
    }
}
