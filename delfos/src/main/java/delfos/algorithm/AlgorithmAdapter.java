package delfos.algorithm;

import java.util.LinkedList;
import delfos.common.parameters.ParameterOwnerAdapter;

/**
 * Adaptador que implementa la interfaz {@link Algorithm}
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
    private final LinkedList<AlgorithmProgressListener> progressListeners = new LinkedList<AlgorithmProgressListener>();
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
