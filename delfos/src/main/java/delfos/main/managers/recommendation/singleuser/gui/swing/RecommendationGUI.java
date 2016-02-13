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
package delfos.main.managers.recommendation.singleuser.gui.swing;

import delfos.ConsoleParameters;
import delfos.main.managers.CaseUseMode;
import delfos.view.SwingGUI;

/**
 *
 * @version 21-oct-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
