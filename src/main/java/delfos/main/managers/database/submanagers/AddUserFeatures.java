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
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.user.User;
import delfos.dataset.changeable.ChangeableDatasetLoader;
import static delfos.main.managers.database.DatabaseManager.ENTITY_NAME;
import static delfos.main.managers.database.DatabaseManager.MANAGE_RATING_DATABASE_ADD_USER_FEATURES;
import static delfos.main.managers.database.DatabaseManager.MANAGE_RATING_DATABASE_FEATURES;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class AddUserFeatures extends DatabaseCaseUseSubManager {

    public static final AddUserFeatures instance = new AddUserFeatures();

    public static AddUserFeatures getInstance() {
        return instance;
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.isParameterDefined(MANAGE_RATING_DATABASE_ADD_USER_FEATURES);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters, DatasetLoader datasetLoader) {
        ChangeableDatasetLoader changeableDatasetLoader = viewDatasetLoaderAsChangeable(datasetLoader);

        int idUser;
        try {
            idUser = new Integer(consoleParameters.getValue(MANAGE_RATING_DATABASE_ADD_USER_FEATURES));
        } catch (NumberFormatException ex) {
            ERROR_CODES.USER_ID_NOT_RECOGNISED.exit(ex);
            throw new IllegalArgumentException(ex);
        } catch (UndefinedParameterException ex) {
            ERROR_CODES.MANAGE_RATING_DATABASE_USER_NOT_DEFINED.exit(ex);
            throw new IllegalArgumentException(ex);
        }

        User user;
        try {
            user = changeableDatasetLoader.getUsersDataset().getUser(idUser);
        } catch (UserNotFound ex) {
            ERROR_CODES.USER_NOT_FOUND.exit(ex);
            throw new IllegalStateException(ex);
        }

        Map<String, String> featuresToAdd = extractFeaturesFromConsole(consoleParameters);
        String newName = extractNameFromConsole(consoleParameters);

        //Obtener las caracteristicas que se desean añadir al usuario.
        addUserFeatures(changeableDatasetLoader, user, newName, featuresToAdd);
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
            ERROR_CODES.MANAGE_RATING_DATABASE_USER_NOT_DEFINED.exit(ex);
            throw new IllegalArgumentException(ex);
        }

        if (featuresToAdd.containsKey(ENTITY_NAME)) {
            featuresToAdd.remove(ENTITY_NAME);
        }

        return featuresToAdd;
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

    public void addUserFeatures(ChangeableDatasetLoader changeableDatasetLoader, User user, String newName, Map<String, String> featuresToAdd) throws RuntimeException {

        //Añado las características.
        try {
            Map<Feature, Object> newEntityFeatures = changeableDatasetLoader.getChangeableUsersDataset()
                    .parseEntityFeaturesAndAddToExisting(user.getId(), featuresToAdd);

            if (newName == null) {
                newName = user.getName();
            } else {
                Global.showInfoMessage("User id=" + user.getId() + " oldName='" + user.getName() + "'  newName='" + newName + "'.\n");
            }

            changeableDatasetLoader.getChangeableUsersDataset().addUser(new User(user.getId(), newName, newEntityFeatures));
        } catch (CannotLoadUsersDataset ex) {
            ERROR_CODES.CANNOT_LOAD_USERS_DATASET.exit(ex);
            throw new IllegalArgumentException(ex);
        } catch (EntityNotFound ex) {
            ERROR_CODES.USER_NOT_FOUND.exit(ex);
            throw new IllegalStateException(ex);
        }
    }

}
