package delfos.dataset.basic.user;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import delfos.dataset.basic.features.EntityWithFeatures;
import static delfos.dataset.basic.features.EntityWithFeaturesDefault.checkFeatureAndFeatureValuesArrays;
import delfos.dataset.basic.features.Feature;

/**
 * Objeto que representa a un usuario del sistema de recomendación.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 24-jul-2013
 */
public class User implements Comparable<Object>, EntityWithFeatures {

    public static User ANONYMOUS_USER = new User(0, "User_Anonymous");

    private final int idUser;
    private final Map<Feature, Object> featuresValues = new TreeMap<>();
    private final String name;

    /**
     * Crea un usuario sin características. Se crea con nombre "User [idUser]".
     *
     * @param idUser Id del usuario que se crea.
     */
    public User(int idUser) {
        this(idUser, "User " + idUser);
    }

    /**
     * Crea un usuario sin características. Se crea con nombre "User [idUser]".
     *
     * @param idUser Id del usuario que se crea.
     * @param name
     */
    public User(int idUser, String name) {
        this.idUser = idUser;
        this.name = name;
    }

    /**
     * Crea un usuario a partir de sus características.
     *
     * @param idUser
     * @param name
     * @param featureValues Mapa de (característica,valor). El tipo de la
     * característica se infiere según si el valor
     */
    public User(int idUser, String name, Map<Feature, Object> featureValues) {
        this(idUser, name);

        for (Map.Entry<Feature, Object> entry : featureValues.entrySet()) {
            Feature feature = entry.getKey();
            Object featureValue = entry.getValue();

            if (featureValue == null && feature.getType().isSkipNullValues()) {
                continue;
            }

            if (featureValue == null) {
                throw new IllegalArgumentException("The feature '" + feature + "' has a null value.");
            }
        }

        featureValues.keySet().stream().forEach((feature) -> {
            Object featureValue = featureValues.get(feature);
            featuresValues.put(feature, featureValue);
        });
    }

    /**
     * Constructor de un usuario de la base de datos para almacenarlo en memoria
     *
     * @param idUser identificador del usuario que se almacena.
     * @param name Nombre del usuario-
     * @param features características relevantes del usuario
     * @param values vector de valores correspondientes a las características en
     * el vector <code>features</code>
     */
    public User(int idUser, String name, Feature[] features, Object[] values) {
        this(idUser, name);

        checkFeatureAndFeatureValuesArrays(features, values);

        for (int i = 0; i < features.length; i++) {
            Feature feature = features[i];
            if (values[i] != null) {
                Object featureValue = values[i];

                if (!feature.getType().isValueCorrect(featureValue)) {
                    throw new IllegalArgumentException("The feature value '" + featureValue + "' for feature '" + featureValue + "' does not match");
                }

                this.featuresValues.put(features[i], values[i]);
            }
        }
    }

    /**
     * Devuelve el valor que el usuario tiene para una característica dado
     *
     * @param feature característica que se desea consultar
     * @return devuelve un objeto con el valor de la característica. Si la
     * característica es nominal, es de tipo <code>{@link String}</code>; si es
     * numérico, devuelve un <code>{@link Float}</code>
     */
    @Override
    public Object getFeatureValue(Feature feature) {
        if (featuresValues.containsKey(feature)) {
            return featuresValues.get(feature);
        } else {
            //No está definido el valor de la característica feature para el usuario idUser
            return null;
        }
    }

    /**
     * Devuelve las características que están definidas para este usuario.
     *
     * @return conjunto de características del usuario
     */
    @Override
    public Set<Feature> getFeatures() {
        return featuresValues.keySet();
    }

    /**
     * Devuelve el identificador del usuario al que pertenece el contenido
     * almacenado en este objeto.
     *
     * @return identificador del usuario
     */
    @Override
    public Integer getId() {
        return idUser;
    }

    /**
     * Devuelve el nombre del usuario.
     *
     * @return Nombre del usuario.
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof User) {
            User user = (User) o;
            return this.name.compareTo(user.name);
        } else {
            throw new IllegalArgumentException("Cannot compare a user with a " + o.getClass());
        }
    }

    public String getTargetId() {
        if (this == ANONYMOUS_USER) {
            return ANONYMOUS_USER.name;
        } else {
            return USER_ID_TARGET_PREFIX + getId();
        }
    }
    public static final String USER_ID_TARGET_PREFIX = "User_";

    public static String getTargetId(int idUser) {
        return new User(idUser).getTargetId();
    }

    public static User parseIdTarget(String idTarget) {
        if (idTarget.startsWith(USER_ID_TARGET_PREFIX)) {

            if (idTarget.equals(ANONYMOUS_USER.name)) {
                return ANONYMOUS_USER;
            }

            String idUser = idTarget.replace(USER_ID_TARGET_PREFIX, "");
            int idUserInt = Integer.parseInt(idUser);
            return new User(idUserInt);
        } else {
            throw new IllegalArgumentException("Not a user idTarget '" + idTarget + "'");
        }
    }
}
