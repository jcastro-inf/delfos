package delfos.common;

/**
 * Clase que encapsula el funcionamiento de un reloj. Permite obtener el tiempo
 * desde que se reinición y el tiempo desde que se marcó el último tiempo
 * parcial.
 *
* @author Jorge Castro Gallardo
 * 
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class Chronometer {

    private long initTime;
    private long previousTime;

    /**
     * Constructor por defecto de la clase {@link Chronometer}. Inicializa los
     * tiempos totales y parciales al momento actual.
     */
    public Chronometer() {
        reset();
    }

    /**
     * Reinicia el cronómetro para volver a contar el tiempo desde el punto en
     * el que se reinició.
     */
    public final void reset() {
        previousTime = System.currentTimeMillis();
        initTime = previousTime;
    }

    /**
     * Establece el punto de control al tiempo actual.
     */
    public final void setPartialEllapsedCheckpoint() {
        previousTime = System.currentTimeMillis();
    }

    /**
     * Devuelve el tiempo que ha pasado desde que se reinició el cronómetro como
     * una cadena en la que se muestra el tiempo pasado
     *
     * @return Devuelve una cadena con el tiempo total transcurrido
     * @see DateCollapse#collapse(long)
     */
    public String printTotalElapsed() {
        long d = System.currentTimeMillis() - initTime;
        return DateCollapse.collapse(d);
    }

    /**
     * Devuelve el número de milisegundos que han transcurrido desde el inicio
     *
     * @return milisegundos que han transcurrido en total
     */
    public long getTotalElapsed() {
        return System.currentTimeMillis() - initTime;
    }

    /**
     * Devuelve el tiempo que ha pasado desde que se marcó un tiempo parcial
     * como una cadena en la que se muestra el tiempo pasado
     *
     * @return Devuelve una cadena con el tiempo parcial transcurrido
     * @see DateCollapse#collapse(long)
     */
    public String printPartialElapsed() {
        long d = (System.currentTimeMillis() - previousTime);
        return DateCollapse.collapse(d);

    }

    /**
     * Devuelve el número de milisegundos que han transcurrido desde el último
     * tiempo parcial
     *
     * @return milisegundos que han transcurrido desde el último tiempo parcial
     */
    public long getPartialElapsed() {
        long actual = System.currentTimeMillis();
        long ret = actual - previousTime;
        return ret;
    }
}
