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
package delfos.view;

import delfos.Constants;
import delfos.view.rsbuilder.RSBuilderFrame;
import delfos.view.recommendation.RecommendationWindow;

/**
 * Clase que contiene los métodos para utilizar la biblioteca de recomendación
 * con una interfaz en swing.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 *
 * @version 1.1 08-Mar-2013 Movida la funcionalidad para indicar los mensajes
 * que se muestran, a la clase {@link Constants}.
 */
public class SwingGUI {

    private SwingGUI() {
    }

    /**
     * Inicia la interfaz para evaluar los sistemas de recomendación de los que
     * la biblioteca dispone.
     */
    public static void initEvaluationGUI() {
        InitialFrame frame = new InitialFrame();
        frame.setVisible(true);
        frame.toFront();
    }

    /**
     * Inicia la interfaz para utilizar los sistemas de recomendación de los que
     * la biblioteca dispone.
     */
    public static void initRecommendationGUI() {
        RecommendationWindow frame = new RecommendationWindow();
        frame.setVisible(true);
        frame.toFront();

    }

    /**
     * Inicia la interfaz para generar modelos de recomendación para los
     * sistemas de recomendación de los que la biblioteca dispone.
     *
     * @param configFile Fichero de configuración en el que se encuentran
     * almacenados los parámetros.
     */
    public static void initRSBuilderGUI(String configFile) {
        RSBuilderFrame configFileFrame = new RSBuilderFrame(configFile);
        configFileFrame.setVisible(true);
    }
}
