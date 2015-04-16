package delfos.main.managers.experiment;

import delfos.ConsoleParameters;
import delfos.main.managers.CaseUseManager;
import delfos.view.resultanalysis.ResultAnalysisFrame;

/**
 *
 * @version 21-oct-2014
* @author Jorge Castro Gallardo
 */
public class ResultAnalysis implements CaseUseManager {

    private static final String RESULT_ANALYSIS_GUI = "-resultAnalysisGUI";

    private ResultAnalysis() {
    }

    public static ResultAnalysis getInstance() {
        return ResultAnalysisHolder.INSTANCE;
    }

    private static class ResultAnalysisHolder {

        private static final ResultAnalysis INSTANCE = new ResultAnalysis();
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.isDefined(RESULT_ANALYSIS_GUI);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        ResultAnalysisFrame resultAnalysisFrame = new ResultAnalysisFrame();
        resultAnalysisFrame.setVisible(true);
    }

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
