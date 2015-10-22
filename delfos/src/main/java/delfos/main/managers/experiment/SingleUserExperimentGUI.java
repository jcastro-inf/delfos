package delfos.main.managers.experiment;

import delfos.ConsoleParameters;
import delfos.Constants;
import delfos.common.Global;
import delfos.main.managers.CaseUseMode;
import delfos.view.SwingGUI;

/**
 *
 * @version 21-oct-2014
 * @author Jorge Castro Gallardo
 */
public class SingleUserExperimentGUI extends CaseUseMode {

    /**
     * Argumento para indicar a la biblioteca que se debe utilizará para
     * experimentación. Por lo tanto, la bilioteca iniciará dicha interfaz si
     * este argumento se encuentra presente.
     */
    public final static String EXPERIMENT_GUI_ARGUMENT = "--x";

    public static SingleUserExperimentGUI getInstance() {
        return SingleUserExperimentGUIHolder.INSTANCE;
    }

    @Override
    public String getModeParameter() {
        return EXPERIMENT_GUI_ARGUMENT;
    }

    private static class SingleUserExperimentGUIHolder {

        private static final SingleUserExperimentGUI INSTANCE = new SingleUserExperimentGUI();
    }

    public SingleUserExperimentGUI() {
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        Global.showInfoMessage(Constants.LIBRARY_NAME + " Using Experimentation GUI\n");
        SwingGUI.initEvaluationGUI();
    }

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        if (1 == 1) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        } else {
            Global.show("\tEXPERIMENTATION\n");
            Global.show("\t\t" + EXPERIMENT_GUI_ARGUMENT + ": The option " + EXPERIMENT_GUI_ARGUMENT + " is used to indicate that "
                    + "you want to use " + Constants.LIBRARY_NAME + " with GUI\n");
            Global.show("\t\n");
            return null;
        }
    }
}
