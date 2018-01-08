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
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
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

    public static final DatasetPrinterManager INSTANCE = new DatasetPrinterManager();

    public static DatasetPrinterManager getInstance() {
        return INSTANCE;
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
    public void manageCaseUse(ConsoleParameters consoleParameters, DatasetLoader datasetLoader) {
        if (consoleParameters.isFlagDefined(PRINT_USER_SET)) {
            System.out.println(printUserSet(datasetLoader));
        }
        if (consoleParameters.isFlagDefined(PRINT_ITEM_SET)) {
            System.out.println(printItemSet(datasetLoader));
        }
        if (consoleParameters.isParameterDefined(PRINT_USER_RATINGS)) {

            try {
                List<String> idUserStrings = consoleParameters.getValues(PRINT_USER_RATINGS);

                for (String idUserString : idUserStrings) {
                    long idUser = new Long(idUserString);
                    System.out.println(printUserRatings(datasetLoader, idUser));
                }
            } catch (UndefinedParameterException ex) {
                throw new IllegalArgumentException(ex);
            }

        }
        if (consoleParameters.isParameterDefined(PRINT_ITEM_RATINGS)) {
            try {
                List<String> idItemStrings = consoleParameters.getValues(PRINT_ITEM_RATINGS);

                for (String idItemString : idItemStrings) {
                    long idItem = new Long(idItemString);
                    System.out.println(printItemRatings(datasetLoader, idItem));
                }
            } catch (UndefinedParameterException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        if (consoleParameters.isFlagDefined(PRINT_RATINGS_TABLE)) {
            System.out.println(printRatingsTable(datasetLoader));
        }

    }

    public String printUserRatings(DatasetLoader datasetLoader, long idUser) throws RuntimeException {
        StringBuilder str = new StringBuilder();
        try {

            User user = datasetLoader.getUsersDataset().getUser(idUser);
            Map<Long, Rating> userRatings = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser);
            str.append("==============================================================\n");
            str.append("User '").append(user.getName()).append("' (id=").append(idUser).append(") ratings size: ").append(userRatings.size()).append("\n");
            for (Map.Entry<Long, Rating> entry : userRatings.entrySet()) {
                long idItem = entry.getKey();
                Rating rating = entry.getValue();

                try {
                    Item item = datasetLoader.getContentDataset().get(idItem);
                    str.append("Item '").append(item.getName()).append("' (id=").append(idItem).append(") ---> ").append(rating.getRatingValue()).append("\n");
                } catch (EntityNotFound ex) {
                    ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
                    throw new IllegalArgumentException(ex);
                }
            }
            str.append("==============================================================").append("\n");
        } catch (UserNotFound ex) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return str.toString();
    }

    public String printItemRatings(DatasetLoader datasetLoader, long idItem) throws RuntimeException {
        StringBuilder str = new StringBuilder();
        try {

            Item item = datasetLoader.getContentDataset().getItem(idItem);
            Map<Long, Rating> userRatings = datasetLoader.getRatingsDataset().getItemRatingsRated(item.getId());
            str.append("==============================================================").append("\n");
            str.append("User '").append(item.getName()).append("' (id=").append(idItem).append(") ratings size: ").append(userRatings.size()).append("\n");
            for (Map.Entry<Long, Rating> entry : userRatings.entrySet()) {
                long idUser = entry.getKey();
                Rating rating = entry.getValue();

                try {
                    User user = datasetLoader.getUsersDataset().get(idUser);
                    str.append("User '").append(user.getName()).append("' (id=").append(idUser).append(") ---> ").append(rating.getRatingValue()).append("\n");
                } catch (EntityNotFound ex) {
                    ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
                    throw new IllegalArgumentException(ex);
                }
            }
            str.append("==============================================================").append("\n");
        } catch (ItemNotFound ex) {
            Logger.getLogger(DatabaseManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return str.toString();
    }

    public String printItemSet(DatasetLoader datasetLoader) throws CannotLoadContentDataset, RuntimeException {
        TreeSet<Long> items = new TreeSet<>(datasetLoader.getContentDataset().allIDs());

        StringBuilder str = new StringBuilder();
        str.append("==============================================================").append("\n");
        str.append("Item set size: ").append(items.size());
        for (long idItem : items) {
            try {
                Item item = datasetLoader.getContentDataset().getItem(idItem);
                str.append("\tidItem '").append(idItem).append("' with name ").append(item.getName()).append("\n");
            } catch (ItemNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
                throw new IllegalArgumentException(ex);
            }
        }
        str.append("==============================================================").append("\n");
        return str.toString();
    }

    public String printUserSet(DatasetLoader datasetLoader) throws CannotLoadUsersDataset, RuntimeException {

        StringBuilder str = new StringBuilder();

        TreeSet<Long> users = new TreeSet<>(datasetLoader.getUsersDataset().allIDs());
        str.append("==============================================================").append("\n");
        str.append("User set size: ").append(users.size());
        for (long idUser : users) {
            try {
                User u = datasetLoader.getUsersDataset().get(idUser);
                str.append("\tidUser '").append(idUser).append("' with name ").append(u.getName()).append("\n");
            } catch (EntityNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
                throw new IllegalArgumentException(ex);
            }
        }
        str.append("==============================================================").append("\n");

        return str.toString();
    }

    public String printRatingsTable(DatasetLoader datasetLoader) {
        Collection<Long> users = datasetLoader.getUsersDataset().allIDs();
        Collection<Long> items = datasetLoader.getContentDataset().allIDs();

        StringBuilder str = new StringBuilder();
        if (!users.isEmpty() && !items.isEmpty()) {
            String ratingTable = DatasetPrinter.printCompactRatingTable(
                    datasetLoader.getRatingsDataset(),
                    users,
                    items);

            str.append(ratingTable).append("\n");
        } else {
            str.append(printUserSet(datasetLoader));
            str.append("\n");
            str.append(printItemSet(datasetLoader));
        }
        return str.toString();
    }
}
