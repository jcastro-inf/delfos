package delfos.main.managers.database.submanagers;

import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.users.UserAlreadyExists;
import delfos.dataset.basic.user.User;
import delfos.dataset.changeable.ChangeableDatasetLoader;

/**
 *
 * @author jcastro
 */
public class AddUser implements DatabaseManagerCaseUseManager {

    /**
     * Parametro para especificar que la biblioteca añada un usuario a la base
     * de datos que está siendo administrada.
     */
    public static final String MANAGE_RATING_DATABASE_ADD_USER_OLD = "-addUser";
    /**
     * Parametro para especificar que la biblioteca añada un usuario a la base
     * de datos que está siendo administrada.
     */
    public static final String MANAGE_RATING_DATABASE_ADD_USER = "-add-user";

    public static final AddUser instance = new AddUser();

    public static AddUser getInstance() {
        return instance;
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.deprecatedParameter_isDefined(MANAGE_RATING_DATABASE_ADD_USER_OLD, MANAGE_RATING_DATABASE_ADD_USER);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters, ChangeableDatasetLoader changeableDatasetLoader) {

        try {

            int idUser = new Integer(consoleParameters.deprecatedParameter_getValue(MANAGE_RATING_DATABASE_ADD_USER_OLD, MANAGE_RATING_DATABASE_ADD_USER));
            if (changeableDatasetLoader.getUsersDataset().getAllID().contains(idUser)) {
                throw new UserAlreadyExists(idUser);
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
        } catch (UserAlreadyExists ex) {
            ERROR_CODES.MANAGE_RATING_DATABASE_USER_ALREADY_EXISTS.exit(ex);
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
