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
import static delfos.main.managers.database.DatabaseManager.MANAGE_RATING_DATABASE_ADD_RATING;
import static delfos.main.managers.database.DatabaseManager.MANAGE_RATING_DATABASE_ID_ITEM;
import static delfos.main.managers.database.DatabaseManager.MANAGE_RATING_DATABASE_ID_USER;
import static delfos.main.managers.database.DatabaseManager.MANAGE_RATING_DATABASE_RATING_VALUE;

/**
 *
 * @author jcastro
 */
public class AddRating extends DatabaseCaseUseSubManager {

    private static final AddRating instance = new AddRating();

    public static AddRating getInstance() {
        return instance;
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.isFlagDefined(MANAGE_RATING_DATABASE_ADD_RATING);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters, ChangeableDatasetLoader changeableDatasetLoader) {

        int idUser, idItem;
        Number ratingValue;

        try {
            idItem = new Integer(consoleParameters.getValue(MANAGE_RATING_DATABASE_ID_ITEM));

            idUser = new Integer(consoleParameters.getValue(MANAGE_RATING_DATABASE_ID_USER));

            String ratingValueString = consoleParameters.getValue(MANAGE_RATING_DATABASE_RATING_VALUE);
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
