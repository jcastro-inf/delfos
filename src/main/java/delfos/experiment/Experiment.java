package delfos.experiment;

import delfos.common.parameters.ParameterOwner;
import org.jdom2.Element;

import java.io.File;

public interface Experiment extends ParameterOwner,SeedHolder {

        public final String FINISHED = "Finished";
        public final String RUNNING = "Running";
        public final String WAITING = "Waiting";
        public final String UNKNOWN = "Unknown";

    /*
     * ===== Control del progreso del experimento ===========================
     */
        /**
         * Registra el objeto indicado para ser notificado de cambios en la
         * ejecución del experimento.
         *
         * @param listener Objeto que desea ser notificado de cambios.
         */
    public void addExperimentListener(ExperimentListener listener);

    /**
     * Elimina un objeto para que no sea notificado más sobre los cambios de
     * ejecución del experimento.
     *
     * @param listener Objeto que se desea dar de baja.
     */
    public void removeExperimentListener(ExperimentListener listener);

    /**
     * Obtiene el porcentaje de ejecución completada del experimento.
     *
     * @return Porcentaje completado
     */
    public int getExperimentProgressPercent();

    /**
     * Obtiene el tiempo restante del experimento
     *
     * @return Tiempo en milisegundos
     */
    public long getExperimentProgressRemainingTime();

    /**
     * Obtiene el nombre de la tarea actual que está realizando el experimento.
     *
     * @return Tarea actual.
     */
    public String getExperimentProgressTask();

    /*
     * ========== Control del progreso de la ejecución actual =================
     */
    /**
     * Obtiene el progreso de la ejecuión actual.
     *
     * @return Porcentaje completado de la ejecución actual.
     */
    public int getExecutionProgressPercent();

    /**
     * Obtiene el tiempo restante de la ejecución actual.
     *
     * @return Tiempo en milisegundos.
     */
    public long getExecutionProgressRemainingTime();

    /**
     * Obtiene la tarea que se está ejecutando en la ejecución actual.
     *
     * @return Descripción de la tarea.
     */
    public String getExecutionProgressTask();

    /*
     * ================= Control del número de ejecuciones ====================
     */
    /**
     * Obtiene el número de vueltas que el experimento realiza. El número de
     * vueltas es el número de ejecuciones multiplicado por el número de
     * particiones.
     *
     * @return Número de vueltas que el experimento realiza.
     */
    public int getNumVueltas();

    /**
     * Obtiene la vuelta actual que está ejecutando el experimento.
     *
     * <p>
     * NOTA: Los valores comienzan en cero.
     *
     * @return Vuelta actual.
     */
    public int getVueltaActual();

    public void setVueltaActual(int vueltaActual);

    /**
     * Devuelve el tiempo estimado restante.
     *
     * @return Tiempo en milisegundos.
     */
    public long getExperimentRemainingTime();

    public boolean isFinished();

    public boolean hasErrors();
    public void execute();

    public void setResultsDirectory(File directory);
    public File getResultsDirectory();

    public void addResultsToElement(Element experimentElement);
    public void setResultsFromElement(Element experimentElement);
}
