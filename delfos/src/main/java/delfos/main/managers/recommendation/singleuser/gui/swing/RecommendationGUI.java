package delfos.main.managers.recommendation.singleuser.gui.swing;

import delfos.ConsoleParameters;
import delfos.main.managers.CaseUseManager;
import delfos.view.SwingGUI;

/**
 *
 * @version 21-oct-2014
* @author Jorge Castro Gallardo
 */
public class RecommendationGUI implements CaseUseManager {

    /**
     * Parámetro de la linea de comandos para especificar que se muestre la
     * interfaz de recomendación.
     */
    public static final String RECOMMENDATION_GUI_COMMAND_LINE_PARAMETER = "-recommendationGUI";

    private static class Holder {

        private static final RecommendationGUI INSTANCE = new RecommendationGUI();
    }

    public static RecommendationGUI getInstance() {
        return Holder.INSTANCE;
    }

    public RecommendationGUI() {
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.isDefined(RECOMMENDATION_GUI_COMMAND_LINE_PARAMETER);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        SwingGUI.initRecommendationGUI();
    }

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
