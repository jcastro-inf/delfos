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
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.item.Item;
import delfos.dataset.changeable.ChangeableDatasetLoader;
import static delfos.main.managers.database.DatabaseManager.ENTITY_NAME;
import static delfos.main.managers.database.DatabaseManager.MANAGE_RATING_DATABASE_ADD_ITEM_FEATURES;
import static delfos.main.managers.database.DatabaseManager.MANAGE_RATING_DATABASE_FEATURES;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class AddItemFeatures extends DatabaseCaseUseSubManager {

    public static final AddItemFeatures instance = new AddItemFeatures();

    public static AddItemFeatures getInstance() {
        return instance;
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.isParameterDefined(MANAGE_RATING_DATABASE_ADD_ITEM_FEATURES);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters, ChangeableDatasetLoader changeableDatasetLoader) {

        int idItem;
        try {
            idItem = new Integer(consoleParameters.getValue(MANAGE_RATING_DATABASE_ADD_ITEM_FEATURES));
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

        Map<String, String> featuresToAdd = extractFeaturesFromConsole(consoleParameters);
        String newName = extractNameFromConsole(consoleParameters);

        //Obtener las caracteristicas que se desean añadir al usuario.
        addItemFeatures(featuresToAdd, newName, changeableDatasetLoader, item);

    }

    private String extractNameFromConsole(ConsoleParameters consoleParameters) {

        String newName = consoleParameters.getValues(MANAGE_RATING_DATABASE_FEATURES)
                .stream()
                .filter(parameterString -> parameterString.contains(":"))
                .filter(parameterString -> parameterString.split(":")[0].equals(ENTITY_NAME))
                .findFirst()
                .map(newNameParameter -> newNameParameter.split(":")[1])
                .orElse(null);

        return newName;
    }

    public Map<String, String> extractFeaturesFromConsole(ConsoleParameters consoleParameters) {
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

        if (featuresToAdd.containsKey(ENTITY_NAME)) {
            featuresToAdd.remove(ENTITY_NAME);
        }

        return featuresToAdd;
    }

    public void addItemFeatures(Map<String, String> featuresToAdd, String newName, ChangeableDatasetLoader changeableDatasetLoader, Item item) throws RuntimeException {

        //Añado las características.
        try {
            Map<Feature, Object> newEntityFeatures = changeableDatasetLoader.getChangeableContentDataset()
                    .parseEntityFeaturesAndAddToExisting(item.getId(), featuresToAdd);

            if (newName == null) {
                newName = item.getName();
            } else {
                Global.showInfoMessage("Item id=" + item.getId() + " oldName='" + item.getName() + "'  newName='" + newName + "'.\n");
            }

            changeableDatasetLoader.getChangeableContentDataset().addItem(new Item(item.getId(), newName, newEntityFeatures));
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
            throw new IllegalArgumentException(ex);
        } catch (EntityNotFound ex) {
            ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            throw new IllegalStateException(ex);
        }
    }
}
