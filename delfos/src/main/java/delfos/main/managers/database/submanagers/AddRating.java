package delfos.main.managers.database.submanagers;

import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.RatingWithTimestamp;
import delfos.dataset.changeable.ChangeableDatasetLoader;

/**
 *
 * @author jcastro
 */
public class AddRating extends DatabaseCaseUseSubManager {

    /**
     * Parametro para especificar que la biblioteca añada un usuario a la base
     * de datos que está siendo administrada.
     */
    @Deprecated
    public static final String MANAGE_RATING_DATABASE_ADD_RATING_OLD = "-addRating";

    /**
     * Parametro para especificar que la biblioteca añada un usuario a la base
     * de datos que está siendo administrada.
     */
    public static final String MANAGE_RATING_DATABASE_ADD_RATING = "--add-rating";

    /**
     * Parametro para especificar a la biblioteca el usuario con el que se está
     * trabajando.
     */
    public static final String MANAGE_RATING_DATABASE_ID_USER_OLD = "-idUser";
    /**
     * Parametro para especificar a la biblioteca el producto con el que se está
     * trabajando.
     */
    public static final String MANAGE_RATING_DATABASE_ID_ITEM_OLD = "-idItem";
    /**
     * Parametro para especificar a la biblioteca el valor del rating que se
     * desea añadir.
     */
    public static final String MANAGE_RATING_DATABASE_RATING_VALUE_OLD = "-ratingValue";
    /**
     * Parametro para especificar a la biblioteca el usuario con el que se está
     * trabajando.
     */
    public static final String MANAGE_RATING_DATABASE_ID_USER = "-user";
    /**
     * Parametro para especificar a la biblioteca el producto con el que se está
     * trabajando.
     */
    public static final String MANAGE_RATING_DATABASE_ID_ITEM = "-item";
    /**
     * Parametro para especificar a la biblioteca el valor del rating que se
     * desea añadir.
     */
    public static final String MANAGE_RATING_DATABASE_RATING_VALUE = "-value";

    private static final AddRating instance = new AddRating();

    public static AddRating getInstance() {
        return instance;
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.deprecatedParameter_isDefined(MANAGE_RATING_DATABASE_ADD_RATING_OLD, MANAGE_RATING_DATABASE_ADD_RATING);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters, ChangeableDatasetLoader changeableDatasetLoader) {

        int idUser, idItem;
        Number ratingValue;

        try {
            idItem = new Integer(consoleParameters.deprecatedParameter_getValue(MANAGE_RATING_DATABASE_ID_ITEM_OLD, MANAGE_RATING_DATABASE_ID_ITEM));

            idUser = new Integer(consoleParameters.deprecatedParameter_getValue(MANAGE_RATING_DATABASE_ID_USER_OLD, MANAGE_RATING_DATABASE_ID_USER));

            String ratingValueString = consoleParameters.deprecatedParameter_getValue(MANAGE_RATING_DATABASE_RATING_VALUE_OLD, MANAGE_RATING_DATABASE_RATING_VALUE);
            ratingValue = new Double(ratingValueString);
        } catch (UndefinedParameterException ex) {
            switch (ex.getParameterMissing()) {
                case MANAGE_RATING_DATABASE_ID_ITEM:
                    ERROR_CODES.MANAGE_RATING_DATABASE_ITEM_NOT_DEFINED.exit(ex);
                    break;
                case MANAGE_RATING_DATABASE_ID_USER:
                    ERROR_CODES.MANAGE_RATING_DATABASE_USER_NOT_DEFINED.exit(ex);
                    break;
                case MANAGE_RATING_DATABASE_RATING_VALUE:
                    ERROR_CODES.MANAGE_RATING_DATABASE_RATINGS_VALUE_NOT_DEFINED.exit(ex);
                    break;
            }
            throw new IllegalArgumentException(ex);
        } catch (NumberFormatException ex) {
            ERROR_CODES.USER_ID_NOT_RECOGNISED.exit(ex);
            throw new IllegalArgumentException(ex);
        }

        try {
            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage("Adding rating (idUser=" + idUser + ", idItem=" + idItem + " ) --> " + ratingValue + "\n");
            }
            changeableDatasetLoader.getChangeableContentDataset().get(idItem);
            changeableDatasetLoader.getChangeableUsersDataset().get(idUser);
            changeableDatasetLoader.getChangeableRatingsDataset().addRating(idUser, idItem, new RatingWithTimestamp(idUser, idItem, ratingValue, System.currentTimeMillis()));
        } catch (CannotLoadRatingsDataset ex) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
            throw new IllegalArgumentException(ex);
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
            throw new IllegalArgumentException(ex);
        } catch (CannotLoadUsersDataset ex) {
            ERROR_CODES.CANNOT_LOAD_USERS_DATASET.exit(ex);
            throw new IllegalArgumentException(ex);
        } catch (UserNotFound ex) {
            ERROR_CODES.USER_NOT_FOUND.exit(ex);
            throw new IllegalArgumentException(ex);
        } catch (ItemNotFound ex) {
            ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            throw new IllegalArgumentException(ex);
        } catch (EntityNotFound ex) {
            ERROR_CODES.UNDEFINED_ERROR.exit(ex);
            throw new IllegalArgumentException(ex);
        }
    }
}
