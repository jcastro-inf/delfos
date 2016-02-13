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
package delfos.main.managers.database.submanagers;

import delfos.ConsoleParameters;
import delfos.common.Global;
import delfos.dataset.changeable.ChangeableDatasetLoader;
import delfos.main.managers.database.submanagers.DatabaseCaseUseSubManager;
import static delfos.main.managers.database.DatabaseManager.MANAGE_RATING_DATABASE_INIT_DATABASE;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class InitDatabase extends DatabaseCaseUseSubManager {

    public static final InitDatabase instance = new InitDatabase();

    public static InitDatabase getInstance() {
        return instance;
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.isFlagDefined(MANAGE_RATING_DATABASE_INIT_DATABASE);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters, ChangeableDatasetLoader changeableDatasetLoader) {
        manageCaseUse(changeableDatasetLoader);
    }

    public void manageCaseUse(ChangeableDatasetLoader changeableDatasetLoader) {
        if (Global.isVerboseAnnoying()) {
            Global.showInfoMessage("Starting database.\n");
        }
        changeableDatasetLoader.initStructures();

        if (Global.isVerboseAnnoying()) {
            Global.showInfoMessage("Database started.\n");
        }
    }
}
