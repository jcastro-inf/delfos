package delfos.main.managers.recommendation.singleuser.gui.swing;

import delfos.ConsoleParameters;
import delfos.main.managers.CaseUseMode;
import delfos.view.SwingGUI;

/**
 *
 * @version 21-oct-2014
 * @author Jorge Castro Gallardo
 */
public class RecommendationGUI extends CaseUseMode {

    /**
     * Parámetro de la linea de comandos para especificar que se muestre la
     * interfaz de recomendación.
     */
    public static final String RECOMMENDATION_GUI_COMMAND_LINE_PARAMETER = "--recommendation-gui";

    private static class Holder {

        private static final RecommendationGUI INSTANCE = new RecommendationGUI();
    }

    public static RecommendationGUI getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public String getModeParameter() {
        return RECOMMENDATION_GUI_COMMAND_LINE_PARAMETER;
    }

    private RecommendationGUI() {
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        SwingGUI.initRecommendationGUI();
    }
}
