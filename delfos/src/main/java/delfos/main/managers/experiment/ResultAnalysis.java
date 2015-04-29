package delfos.main.managers.experiment;

import delfos.ConsoleParameters;
import delfos.main.managers.CaseUseModeManager;
import delfos.view.resultanalysis.ResultAnalysisFrame;

/**
 *
 * @version 21-oct-2014
 * @author Jorge Castro Gallardo
 */
public class ResultAnalysis extends CaseUseModeManager {

    private static final String RESULT_ANALYSIS_GUI_OLD = "-resultAnalysisGUI";

    private static final String RESULT_ANALYSIS_GUI = "--result-analysis-gui";

    private ResultAnalysis() {
    }

    private static final ResultAnalysis INSTANCE = new ResultAnalysis();

    public static ResultAnalysis getInstance() {
        return INSTANCE;
    }

    @Override
    public String getModeParameter() {
        return RESULT_ANALYSIS_GUI;
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {

        if (consoleParameters.deprecatedParameter_isDefined(RESULT_ANALYSIS_GUI_OLD, RESULT_ANALYSIS_GUI)) {
            return true;
        }
        return consoleParameters.isDefined(RESULT_ANALYSIS_GUI_OLD);
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
