package delfos.main.managers.database.submanagers;

import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.items.ItemAlreadyExists;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.item.Item;
import delfos.dataset.changeable.ChangeableDatasetLoader;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author jcastro
 */
public class AddItemFeatures extends DatabaseCaseUseSubManager {

    /**
     * Parámetro para especificar que se use el modo de añadir características a
     * un producto.
     */
    @Deprecated
    public static final String MANAGE_RATING_DATABASE_ADD_ITEM_FEATURES_OLD = "-addItemFeatures";
    /**
     * Parámetro para especificar que se use el modo de añadir características a
     * un producto.
     */
    public static final String MANAGE_RATING_DATABASE_ADD_ITEM_FEATURES = "-add-item-features";

    /**
     * Parámetro para especificar las características que se añaden en los modos
     * {@link AddUserFeatures} y {@link AddItemFeatures}.
     */
    public static final String MANAGE_RATING_DATABASE_FEATURES = "-features";
    /**
     * Cadena que denota el nombre de una entidad {@link EntityWithFeatures}.
     * Por ejemplo, en una base de datos se utilizará esta cadena como la
     * columna que contiene el nombre de cada producto. (usuario, producto,
     * etc.).
     */
    public static final String ENTITY_NAME = "name";

    public static final AddItemFeatures instance = new AddItemFeatures();

    public static AddItemFeatures getInstance() {
        return instance;
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.deprecatedParameter_isDefined(MANAGE_RATING_DATABASE_ADD_ITEM_FEATURES_OLD, MANAGE_RATING_DATABASE_ADD_ITEM_FEATURES);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters, ChangeableDatasetLoader changeableDatasetLoader) {

        int idItem;
        try {
            idItem = new Integer(consoleParameters.deprecatedParameter_getValue(MANAGE_RATING_DATABASE_ADD_ITEM_FEATURES_OLD, MANAGE_RATING_DATABASE_ADD_ITEM_FEATURES));
        } catch (NumberFormatException ex) {
            ERROR_CODES.ITEM_ID_NOT_RECOGNISED.exit(ex);
            throw new IllegalArgumentException(ex);
        } catch (UndefinedParameterException ex) {
            ERROR_CODES.MANAGE_RATING_DATABASE_ITEM_NOT_DEFINED.exit(ex);
            throw new IllegalArgumentException(ex);
        }

        Item item;
        try {
            item = changeableDatasetLoader.getContentDataset().getItem(idItem);
        } catch (ItemNotFound ex) {
            ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            throw new IllegalArgumentException(ex);
        }

        //Obtener las caracteristicas que se desean añadir al producto.
        Map<String, String> featuresToAdd = new TreeMap<>();
        try {
            List<String> featuresList = consoleParameters.getValues(MANAGE_RATING_DATABASE_FEATURES);

            {
                int i = 0;
                for (String thisFeature : featuresList) {

                    if (!thisFeature.contains(":")) {
                        IllegalArgumentException ex = new IllegalArgumentException("The feature syntax must be 'featureNameWithTypeSuffix:value' and this was '" + thisFeature + "'");
                        ERROR_CODES.MANAGE_RATING_DATABASE_WRONG_FEATURES_SYNTAX.exit(ex);
                    }

                    //Leo cada característica, la creo y ya y tal.
                    String featureName_extended = thisFeature.substring(0, thisFeature.indexOf(":"));
                    String featureValue = thisFeature.substring(thisFeature.indexOf(":") + 1);

                    featuresToAdd.put(featureName_extended, featureValue);
                    i++;
                }
            }
        } catch (UndefinedParameterException ex) {
            ERROR_CODES.MANAGE_RATING_DATABASE_ITEM_NOT_DEFINED.exit(ex);
            throw new IllegalArgumentException(ex);
        }

        //Extraigo el nombre de los parámetros, ya que tiene un tratamiento especial.
        String newName = null;
        if (featuresToAdd.containsKey(ENTITY_NAME)) {
            newName = featuresToAdd.remove(ENTITY_NAME);
        }

        //Añado las características.
        try {

            Map<Feature, Object> newEntityFeatures = changeableDatasetLoader.getChangeableContentDataset().parseEntityFeaturesAndAddToExisting(idItem, featuresToAdd);

            if (newName == null) {
                newName = item.getName();
            } else {
                Global.showInfoMessage("Item id=" + idItem + " oldName='" + item.getName() + "'  newName='" + newName + "'.\n");
            }

            changeableDatasetLoader.getChangeableContentDataset().addItem(new Item(item.getId(), newName, newEntityFeatures));
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
            throw new IllegalArgumentException(ex);
        } catch (ItemAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
            throw new IllegalArgumentException(ex);
        } catch (ItemNotFound ex) {
            ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            throw new IllegalStateException(ex);
        } catch (EntityNotFound ex) {
            ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            throw new IllegalStateException(ex);
        }
    }
}
