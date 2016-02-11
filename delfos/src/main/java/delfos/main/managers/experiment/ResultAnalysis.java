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
