package delfos.main.managers.experiment;

import delfos.ConsoleParameters;
import delfos.main.managers.CaseUseMode;
import delfos.view.resultanalysis.ResultAnalysisFrame;

/**
 *
 * @version 21-oct-2014
 * @author Jorge Castro Gallardo
 */
public class ResultAnalysis extends CaseUseMode {

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
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        ResultAnalysisFrame resultAnalysisFrame = new ResultAnalysisFrame();
        resultAnalysisFrame.setVisible(true);
    }

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
