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
package delfos.main.managers.database.helpers;

import delfos.ConsoleParameters;
import delfos.main.managers.CaseUseMode;
import delfos.main.managers.database.DatabaseManager;
import static delfos.main.managers.database.DatabaseManager.MANAGE_RATING_DATABASE_INIT_DATABASE_GUI;
import delfos.view.dataset.changeable.ChangeableDatasetDefinitorSwingGUI;
import java.io.File;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class InitDatabaseGUI extends CaseUseMode {

    public static final InitDatabaseGUI instance = new InitDatabaseGUI();

    public static InitDatabaseGUI getInstance() {
        return instance;
    }

    private InitDatabaseGUI() {

    }

    @Override
    public String getModeParameter() {
        return MANAGE_RATING_DATABASE_INIT_DATABASE_GUI;
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        File configFile;
        if (consoleParameters.isParameterDefined(DatabaseManager.MANAGE_RATING_DATABASE_CONFIG_XML)) {
            String configFileStr = consoleParameters.getValue(DatabaseManager.MANAGE_RATING_DATABASE_CONFIG_XML);
            configFile = new File(configFileStr);
        } else {
            configFile = new File(DatabaseManager.MANAGE_RATING_DATABASE_DEFAULT_CONFIG_XML);
        }

        ChangeableDatasetDefinitorSwingGUI gui = new ChangeableDatasetDefinitorSwingGUI(configFile, true);

        gui.setVisible(true);
    }

}
