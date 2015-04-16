package delfos.main.managers.database.submanagers;

import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.items.ItemAlreadyExists;
import delfos.dataset.basic.item.Item;
import delfos.dataset.changeable.ChangeableDatasetLoader;

/**
 *
 * @author jcastro
 */
public class AddItem implements DatabaseManagerCaseUseManager {

    /**
     * Parametro para especificar que la biblioteca añada un producto a la base
     * de datos que está siendo administrada.
     */
    @Deprecated
    public static final String MANAGE_RATING_DATABASE_ADD_ITEM_OLD = "-addItem";
    /**
     * Parametro para especificar que la biblioteca añada un producto a la base
     * de datos que está siendo administrada.
     */
    public static final String MANAGE_RATING_DATABASE_ADD_ITEM = "-add-item";

    public static final AddItem instance = new AddItem();

    public static AddItem getInstance() {
        return instance;
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.deprecatedParameter_isDefined(MANAGE_RATING_DATABASE_ADD_ITEM_OLD, MANAGE_RATING_DATABASE_ADD_ITEM);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters, ChangeableDatasetLoader changeableDatasetLoader) {

        try {

            int idItem = new Integer(consoleParameters.deprecatedParameter_getValue(MANAGE_RATING_DATABASE_ADD_ITEM_OLD, MANAGE_RATING_DATABASE_ADD_ITEM));
            if (changeableDatasetLoader.getContentDataset().getAllID().contains(idItem)) {
                throw new ItemAlreadyExists(idItem);
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
        } catch (ItemAlreadyExists ex) {
            ERROR_CODES.MANAGE_RATING_DATABASE_ITEM_ALREADY_EXISTS.exit(ex);
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
