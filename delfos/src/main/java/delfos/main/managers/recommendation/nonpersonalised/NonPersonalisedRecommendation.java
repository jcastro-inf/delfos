package delfos.main.managers.recommendation.nonpersonalised;

import delfos.ConsoleParameters;
import delfos.main.managers.CaseUseModeManager;

/**
 *
 * @version 22-oct-2014
 * @author Jorge Castro Gallardo
 */
public class NonPersonalisedRecommendation extends CaseUseModeManager {

    /**
     * Parámetro de la linea de comandos para usar el modo non-personalised.
     */
    public static final String NON_PERSONALISED_MODE = "--non-personalised";

    public static final NonPersonalisedRecommendation instance = new NonPersonalisedRecommendation();

    public static NonPersonalisedRecommendation getInstance() {
        return instance;
    }

    @Override
    public String getModeParameter() {
        return NON_PERSONALISED_MODE;
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
