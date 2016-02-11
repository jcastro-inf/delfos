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
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.changeable.ChangeableDatasetLoader;
import static delfos.main.managers.database.DatabaseManager.MANAGE_RATING_DATABASE_ADD_ITEM;

/**
 *
 * @author jcastro
 */
public class AddItem extends DatabaseCaseUseSubManager {

    public static final AddItem instance = new AddItem();

    public static AddItem getInstance() {
        return instance;
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.isParameterDefined(MANAGE_RATING_DATABASE_ADD_ITEM);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters, ChangeableDatasetLoader changeableDatasetLoader) {

        try {

            int idItem = new Integer(consoleParameters.getValue(MANAGE_RATING_DATABASE_ADD_ITEM));
            if (changeableDatasetLoader.getContentDataset().getAllID().contains(idItem)) {
                IllegalArgumentException ex = new IllegalArgumentException();
                ERROR_CODES.MANAGE_RATING_DATABASE_ITEM_ALREADY_EXISTS.exit(ex);
            }
            changeableDatasetLoader.getChangeableContentDataset().addItem(new Item(idItem));
        } catch (NumberFormatException ex) {
            ERROR_CODES.ITEM_ID_NOT_RECOGNISED.exit(ex);
            throw new IllegalArgumentException(ex);
        } catch (UndefinedParameterException ex) {
            ERROR_CODES.MANAGE_RATING_DATABASE_ITEM_NOT_DEFINED.exit(ex);
            throw new IllegalArgumentException(ex);
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
            throw new IllegalArgumentException(ex);
        }
    }
}
