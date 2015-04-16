package delfos.view;

import delfos.Constants;
import delfos.view.rsbuilder.RSBuilderFrame;
import delfos.view.recommendation.RecommendationWindow;

/**
 * Clase que contiene los métodos para utilizar la biblioteca de recomendación
 * con una interfaz en swing.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
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
