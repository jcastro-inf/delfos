package delfos.main.managers;

import delfos.ConsoleParameters;

/**
 * Denotes a class who implements the behaviour associated to a specific command
 * line parameter combination.
 *
 * @version 21-oct-2014
 * @author Jorge Castro Gallardo
 */
public abstract class CaseUseModeSubManager {

    private final CaseUseModeManager parent;

    public CaseUseModeSubManager(CaseUseModeManager parent) {
        this.parent = parent;
    }

    /**
     * Comprueba si este es el manager correcto para la linea de comandos
     * especificada.
     *
     * @param consoleParameters Parámetros de la línea de comandos.
     * @return true si es el manager adecuado.
     */
    public abstract boolean isRightManager(ConsoleParameters consoleParameters);

    /**
     * Ejecuta las tareas necesarias de este caso de uso.
     *
     * @param consoleParameters Parámetros de la línea de comandos.
     */
    public abstract void manageCaseUse(ConsoleParameters consoleParameters);

    public CaseUseModeManager getParent() {
        return parent;
    }
}
