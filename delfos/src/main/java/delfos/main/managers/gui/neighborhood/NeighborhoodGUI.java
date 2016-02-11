/* 
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package delfos.main.managers.gui.neighborhood;

import delfos.ConsoleParameters;
import delfos.main.managers.CaseUseMode;
import delfos.view.neighborhood.RecommendationsExplainedWindow;
import javax.swing.SwingUtilities;

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
        SwingUtilities.invokeLater(() -> {
            neighborhoodGUI.setVisible(true);
        });
    }
}
