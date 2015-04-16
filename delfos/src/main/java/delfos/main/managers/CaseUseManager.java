package delfos.main.managers;

import delfos.ConsoleParameters;

/**
 *
 * @version 21-oct-2014
 * @author Jorge Castro Gallardo
 */
public interface CaseUseManager {

    /**
     * Comprueba si este es el manager correcto para la linea de comandos
     * especificada.
     *
     * @param consoleParameters Parámetros de la línea de comandos.
     * @return true si es el manager adecuado.
     */
    public boolean isRightManager(ConsoleParameters consoleParameters);

    /**
     * Ejecuta las tareas necesarias de este caso de uso.
     *
     * @param consoleParameters Parámetros de la línea de comandos.
     */
    public void manageCaseUse(ConsoleParameters consoleParameters);

    public String getUserFriendlyHelpForThisCaseUse();

    //public String getUsageSynopsis();
}
