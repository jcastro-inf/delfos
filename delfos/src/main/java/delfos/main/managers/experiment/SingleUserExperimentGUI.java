package delfos.main.managers.experiment;

import delfos.ConsoleParameters;
import delfos.common.Global;
import delfos.main.managers.CaseUseManager;
import delfos.view.SwingGUI;

/**
 *
 * @version 21-oct-2014
* @author Jorge Castro Gallardo
 */
public class SingleUserExperimentGUI implements CaseUseManager {

    /**
     * Nombre del jar de esta biblioteca.
     */
    public final static String JAR_NAME = "delfos.jar";

    /**
     * Argumento para indicar a la biblioteca que se debe utilizará para
     * experimentación. Por lo tanto, la bilioteca iniciará dicha interfaz si
     * este argumento se encuentra presente.
     */
    public final static String EXPERIMENT_GUI_ARGUMENT = "-x";

    public static SingleUserExperimentGUI getInstance() {
        return SingleUserExperimentGUIHolder.INSTANCE;
    }

    private static class SingleUserExperimentGUIHolder {

        private static final SingleUserExperimentGUI INSTANCE = new SingleUserExperimentGUI();
    }

    public SingleUserExperimentGUI() {
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.isDefined(EXPERIMENT_GUI_ARGUMENT);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        Global.showMessage(JAR_NAME + " Using Experimentation GUI\n");
        SwingGUI.initEvaluationGUI();
    }

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        if (1 == 1) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        System.out.print("\tEXPERIMENTATION\n");
        System.out.print("\t\t" + EXPERIMENT_GUI_ARGUMENT + ": The option " + EXPERIMENT_GUI_ARGUMENT + " is used to indicate that "
                + "you want to use " + JAR_NAME + " with GUI\n");
        System.out.print("\t\n");
        return null;
    }
}
