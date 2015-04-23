package delfos.rs;

/**
 * Interfaz que define el método de notificación del progreso de la tarea de de
 * construcción del sistema de recomendación.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
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
