package delfos.main.managers.gui.neighborhood;

import delfos.ConsoleParameters;
import delfos.main.managers.CaseUseMode;
import delfos.view.neighborhood.RecommendationsExplainedWindow;

/**
 *
 * @version 21-oct-2014
 * @author Jorge Castro Gallardo
 */
public class NeighborhoodGUI extends CaseUseMode {

    /**
     * Parámetro de la linea de comandos para especificar que se muestre la
     * interfaz de recomendación.
     */
    public static final String NEIGHBORHOOD_GUI_COMMAND_LINE_PARAMETER = "--neighborhood-gui";

    private static class Holder {

        private static final NeighborhoodGUI INSTANCE = new NeighborhoodGUI();
    }

    public static NeighborhoodGUI getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public String getModeParameter() {
        return NEIGHBORHOOD_GUI_COMMAND_LINE_PARAMETER;
    }

    private NeighborhoodGUI() {
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        RecommendationsExplainedWindow neighborhoodGUI = new RecommendationsExplainedWindow();
        neighborhoodGUI.setVisible(true);
    }
}
