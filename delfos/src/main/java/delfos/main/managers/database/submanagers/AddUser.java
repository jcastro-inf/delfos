package delfos.main.managers.database.submanagers;

import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.dataset.basic.user.User;
import delfos.dataset.changeable.ChangeableDatasetLoader;
import static delfos.main.managers.database.DatabaseManager.MANAGE_RATING_DATABASE_ADD_USER;

/**
 *
 * @author jcastro
 */
public class AddUser extends DatabaseCaseUseSubManager {

    public static final AddUser instance = new AddUser();

    public static AddUser getInstance() {
        return instance;
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.isParameterDefined(MANAGE_RATING_DATABASE_ADD_USER);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters, ChangeableDatasetLoader changeableDatasetLoader) {

        try {

            int idUser = new Integer(consoleParameters.getValue(MANAGE_RATING_DATABASE_ADD_USER));
            if (changeableDatasetLoader.getUsersDataset().getAllID().contains(idUser)) {
                IllegalArgumentException ex = new IllegalArgumentException();
                ERROR_CODES.MANAGE_RATING_DATABASE_USER_ALREADY_EXISTS.exit(ex);
            }
            changeableDatasetLoader.getChangeableUsersDataset().addUser(new User(idUser));
        } catch (NumberFormatException ex) {
            ERROR_CODES.USER_ID_NOT_RECOGNISED.exit(ex);
            throw new IllegalArgumentException(ex);
        } catch (UndefinedParameterException ex) {
            ERROR_CODES.MANAGE_RATING_DATABASE_USER_NOT_DEFINED.exit(ex);
            throw new IllegalArgumentException(ex);
        } catch (CannotLoadUsersDataset ex) {
            ERROR_CODES.CANNOT_LOAD_USERS_DATASET.exit(ex);
            throw new IllegalArgumentException(ex);
        }
    }
}
