package delfos.main.managers.database.submanagers;

import delfos.ConsoleParameters;
import delfos.Constants;
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.users.UserAlreadyExists;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.user.User;
import delfos.dataset.changeable.ChangeableDatasetLoader;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author jcastro
 */
public class AddUserFeatures implements DatabaseManagerCaseUseManager {

    /**
     * Parámetro para especificar que se use el modo de añadir características a
     * un usuario.
     */
    @Deprecated
    public static final String MANAGE_RATING_DATABASE_ADD_USER_FEATURES_OLD = "-addUserFeatures";
    /**
     * Parámetro para especificar que se use el modo de añadir características a
     * un usuario.
     */
    public static final String MANAGE_RATING_DATABASE_ADD_USER_FEATURES = "-add-user-features";

    /**
     * Parámetro para especificar las características que se añaden en los modos
     * {@link Constants#MANAGE_RATING_DATABASE_ADD_USER_FEATURES} y
     * {@link Constants#MANAGE_RATING_DATABASE_ADD_ITEM_FEATURES}
     */
    public static final String MANAGE_RATING_DATABASE_FEATURES = "-features";
    /**
     * Cadena que denota el nombre de una entidad {@link EntityWithFeatures}.
     * Por ejemplo, en una base de datos se utilizará esta cadena como la
     * columna que contiene el nombre de cada producto. (usuario, producto,
     * etc.).
     */
    public static final String ENTITY_NAME = "name";

    public static final AddUserFeatures instance = new AddUserFeatures();

    public static AddUserFeatures getInstance() {
        return instance;
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.deprecatedParameter_isDefined(MANAGE_RATING_DATABASE_ADD_USER_FEATURES_OLD, MANAGE_RATING_DATABASE_ADD_USER_FEATURES);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters, ChangeableDatasetLoader changeableDatasetLoader) {

        int idUser;
        try {
            idUser = new Integer(consoleParameters.deprecatedParameter_getValue(MANAGE_RATING_DATABASE_ADD_USER_FEATURES_OLD, MANAGE_RATING_DATABASE_ADD_USER_FEATURES));
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

        //Obtener las caracteristicas que se desean añadir al usuario.
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

        //Extraigo el nombre de los parámetros, ya que tiene un tratamiento especial.
        String newName = null;
        if (featuresToAdd.containsKey(ENTITY_NAME)) {
            newName = featuresToAdd.remove(ENTITY_NAME);
        }

        //Añado las características.
        try {
            Map<Feature, Object> newEntityFeatures = changeableDatasetLoader.getChangeableUsersDataset().parseEntityFeaturesAndAddToExisting(idUser, featuresToAdd);

            if (newName == null) {
                newName = user.getName();
            } else {
                Global.showMessage("User id=" + idUser + " oldName='" + user.getName() + "'  newName='" + newName + "'.\n");
            }

            changeableDatasetLoader.getChangeableUsersDataset().addUser(new User(user.getId(), newName, newEntityFeatures));
        } catch (CannotLoadUsersDataset ex) {
            ERROR_CODES.CANNOT_LOAD_USERS_DATASET.exit(ex);
            throw new IllegalArgumentException(ex);
        } catch (UserAlreadyExists ex) {
            ERROR_CODES.USER_NOT_FOUND.exit(ex);
            throw new IllegalArgumentException(ex);
        } catch (EntityNotFound ex) {
            ERROR_CODES.USER_NOT_FOUND.exit(ex);
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
