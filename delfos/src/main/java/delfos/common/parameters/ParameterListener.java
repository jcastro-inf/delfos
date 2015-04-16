package delfos.common.parameters;

/**
 * Interfaz que deben implementar todos los objetos que deseen ser notificados
 * de los cambios de los parámetros de algún objeto que herede de
 * {@link ParameterOwner}
 *
* @author Jorge Castro Gallardo
 * 
 * @version 1.0 Unknown date
 * @version 1.1 22-Feb-2013
 */
public interface ParameterListener {

    /**
     * Método que los objetos se invoca cuando ocurren cambios en los parámetros
     * del {@link ParameterOwner} en el que se ha registrado.
     */
    public void parameterChanged();
}
