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
package delfos.main.managers.library;

import delfos.ConsoleParameters;
import delfos.common.Global;
import delfos.main.Main;
import delfos.main.exceptions.ManyCaseUseActivatedException;
import delfos.main.managers.CaseUseMode;
import java.util.List;

/**
 *
 * @version 21-oct-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class Help {

    public static final String PRINT_HELP = "--help";
    public static final String PRINT_HELP_SHORT = "--h";

    public static Help getInstance() {
        return HelpHolder.INSTANCE;
    }

    private static class HelpHolder {

        private static final Help INSTANCE = new Help();
    }

    public Help() {
    }

    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.isFlagDefined(PRINT_HELP) || consoleParameters.isFlagDefined(PRINT_HELP_SHORT);
    }

    public void manageCaseUse(ConsoleParameters consoleParameters) {
        List<CaseUseMode> suitableCaseUseManagers = Main.getSuitableCaseUse(Main.getAllCaseUse(), consoleParameters);

        switch (suitableCaseUseManagers.size()) {
            case 0:
                throw new UnsupportedOperationException("A generic help of this library should be provided");
            case 1:
                final CaseUseMode selectedCaseUseManager = suitableCaseUseManagers.get(0);
                Global.showInfoMessage("Requested help for " + selectedCaseUseManager);
                Global.showln(selectedCaseUseManager.getUserFriendlyHelpForThisCaseUse());
                break;
            default:
                Main.manyCaseUseActivated(consoleParameters, suitableCaseUseManagers);
                throw new ManyCaseUseActivatedException(consoleParameters, suitableCaseUseManagers);
        }
    }
}
