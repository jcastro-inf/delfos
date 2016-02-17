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
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.dataset.changeable.ChangeableDatasetLoader;
import delfos.dataset.util.DatasetPrinter;
import delfos.main.managers.database.DatabaseManager;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class DatasetPrinterManager extends DatabaseCaseUseSubManager {

    public static final String PRINT_USER_SET = "--user-set";
    public static final String PRINT_ITEM_SET = "--item-set";
    public static final String PRINT_USER_RATINGS = "-user-ratings";
    public static final String PRINT_ITEM_RATINGS = "-item-ratings";

    public static final String PRINT_RATINGS_TABLE = "--ratings-table";

    private static final DatasetPrinterManager instance = new DatasetPrinterManager();

    public static DatasetPrinterManager getInstance() {
        return instance;
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        if (consoleParameters.isFlagDefined(PRINT_USER_SET)) {
            return true;
        }
        if (consoleParameters.isFlagDefined(PRINT_ITEM_SET)) {
            return true;
        }
        if (consoleParameters.isParameterDefined(PRINT_USER_RATINGS)) {
            return true;
        }
        if (consoleParameters.isParameterDefined(PRINT_ITEM_RATINGS)) {
            return true;
        }

        return consoleParameters.isFlagDefined(PRINT_RATINGS_TABLE);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters, ChangeableDatasetLoader changeableDatasetLoader) {
        if (consoleParameters.isFlagDefined(PRINT_USER_SET)) {
            printUserSet(changeableDatasetLoader);
        }
        if (consoleParameters.isFlagDefined(PRINT_ITEM_SET)) {
            printItemSet(changeableDatasetLoader);
        }
        if (consoleParameters.isParameterDefined(PRINT_USER_RATINGS)) {

            try {
                List<String> idUserStrings = consoleParameters.getValues(PRINT_USER_RATINGS);

                for (String idUserString : idUserStrings) {
                    int idUser = Integer.parseInt(idUserString);
                    printUserRatings(changeableDatasetLoader, idUser);
                }
            } catch (UndefinedParameterException ex) {
                throw new IllegalArgumentException(ex);
            }

        }
        if (consoleParameters.isParameterDefined(PRINT_ITEM_RATINGS)) {
            try {
                List<String> idItemStrings = consoleParameters.getValues(PRINT_ITEM_RATINGS);

                for (String idItemString : idItemStrings) {
                    int idItem = Integer.parseInt(idItemString);
                    printItemRatings(changeableDatasetLoader, idItem);
                }
            } catch (UndefinedParameterException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        if (consoleParameters.isFlagDefined(PRINT_RATINGS_TABLE)) {
            printRatingsTable(changeableDatasetLoader);
        }

    }

    private void printUserRatings(ChangeableDatasetLoader changeableDatasetLoader, int idUser) throws RuntimeException {
        try {

            User user = changeableDatasetLoader.getUsersDataset().getUser(idUser);
            Map<Integer, Rating> userRatings = changeableDatasetLoader.getRatingsDataset().getUserRatingsRated(idUser);
            System.out.println("==============================================================");
            System.out.println("User '" + user.getName() + "' (id=" + idUser + ") ratings size: " + userRatings.size());
            for (Map.Entry<Integer, Rating> entry : userRatings.entrySet()) {
                int idItem = entry.getKey();
                Rating rating = entry.getValue();

                try {
                    Item item = changeableDatasetLoader.getChangeableContentDataset().get(idItem);
                    System.out.println("Item '" + item.getName() + "' (id=" + idItem + ") ---> " + rating.getRatingValue());
                } catch (EntityNotFound ex) {
                    ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
                    throw new IllegalArgumentException(ex);
                }
            }
            System.out.println("==============================================================");
        } catch (UserNotFound ex) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void printItemRatings(ChangeableDatasetLoader changeableDatasetLoader, int idItem) throws RuntimeException {
        try {

            Item item = changeableDatasetLoader.getChangeableContentDataset().getItem(idItem);
            Map<Integer, Rating> userRatings = changeableDatasetLoader.getRatingsDataset().getItemRatingsRated(item.getId());
            System.out.println("==============================================================");
            System.out.println("User '" + item.getName() + "' (id=" + idItem + ") ratings size: " + userRatings.size());
            for (Map.Entry<Integer, Rating> entry : userRatings.entrySet()) {
                int idUser = entry.getKey();
                Rating rating = entry.getValue();

                try {
                    User user = changeableDatasetLoader.getChangeableUsersDataset().get(idUser);
                    System.out.println("User '" + user.getName() + "' (id=" + idUser + ") ---> " + rating.getRatingValue());
                } catch (EntityNotFound ex) {
                    ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
                    throw new IllegalArgumentException(ex);
                }
            }
            System.out.println("==============================================================");
        } catch (ItemNotFound ex) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void printItemSet(ChangeableDatasetLoader changeableDatasetLoader) throws CannotLoadContentDataset, RuntimeException {
        TreeSet<Integer> items = new TreeSet<>(changeableDatasetLoader.getContentDataset().allIDs());
        System.out.println("==============================================================");
        System.out.println("Item set size: " + items.size());
        for (int idItem : items) {
            try {
                Item item = changeableDatasetLoader.getContentDataset().getItem(idItem);
                System.out.println("\tidItem '" + idItem + "' with name " + item.getName());
            } catch (ItemNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
                throw new IllegalArgumentException(ex);
            }
        }
        System.out.println("==============================================================");
    }

    private void printUserSet(ChangeableDatasetLoader changeableDatasetLoader) throws CannotLoadUsersDataset, RuntimeException {
        TreeSet<Integer> users = new TreeSet<>(changeableDatasetLoader.getUsersDataset().allIDs());
        System.out.println("==============================================================");
        System.out.println("User set size: " + users.size());
        for (int idUser : users) {
            try {
                User u = changeableDatasetLoader.getUsersDataset().get(idUser);
                System.out.println("\tidUser '" + idUser + "' with name " + u.getName());
            } catch (EntityNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
                throw new IllegalArgumentException(ex);
            }
        }
        System.out.println("==============================================================");
    }

    private void printRatingsTable(ChangeableDatasetLoader changeableDatasetLoader) {
        Collection<Integer> users = changeableDatasetLoader.getUsersDataset().allIDs();
        Collection<Integer> items = changeableDatasetLoader.getContentDataset().allIDs();

        if (!users.isEmpty() && !items.isEmpty()) {
            String ratingTable = DatasetPrinter.printCompactRatingTable(
                    changeableDatasetLoader.getRatingsDataset(),
                    users,
                    items);

            System.out.println(ratingTable);
        } else {
            printUserSet(changeableDatasetLoader);
            System.out.println("");
            printItemSet(changeableDatasetLoader);
        }
    }

}
