package delfos.main.managers.database.submanagers;

import delfos.ConsoleParameters;
import delfos.dataset.changeable.ChangeableDatasetLoader;
import delfos.main.managers.CaseUseSubManager;
import delfos.main.managers.database.DatabaseManager;
import static delfos.main.managers.database.DatabaseManager.extractChangeableDatasetHandler;

/**
 *
 * @version 21-oct-2014
 * @author Jorge Castro Gallardo
 */
public abstract class DatabaseCaseUseSubManager extends CaseUseSubManager {

    public DatabaseCaseUseSubManager() {
        super(DatabaseManager.getInstance());
    }

    /**
     * Comprueba si este es el manager correcto para la linea de comandos
     * especificada.
     *
     * @param consoleParameters Parámetros de la línea de comandos.
     * @return true si es el manager adecuado.
     */
    @Override
    public abstract boolean isRightManager(ConsoleParameters consoleParameters);

    @Override
    public final void manageCaseUse(ConsoleParameters consoleParameters) {
        ChangeableDatasetLoader changeableDatasetLoader = extractChangeableDatasetHandler(consoleParameters);
        this.manageCaseUse(consoleParameters, changeableDatasetLoader);
        changeableDatasetLoader.commitChangesInPersistence();
    }

    /**
     * Ejecuta las tareas necesarias de este caso de uso.
     *
     * @param consoleParameters Parámetros de la línea de comandos.
     * @param changeableDatasetLoader changeable dataset loader.
     */
    public abstract void manageCaseUse(ConsoleParameters consoleParameters, ChangeableDatasetLoader changeableDatasetLoader);

}
