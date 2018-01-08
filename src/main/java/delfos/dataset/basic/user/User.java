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
package delfos.dataset.basic.user;

import delfos.common.StringsOrderings;
import delfos.dataset.basic.features.EntityWithFeatures;
import static delfos.dataset.basic.features.EntityWithFeaturesDefault.checkFeatureAndFeatureValuesArrays;
import delfos.dataset.basic.features.Feature;
import java.io.Serializable;
import java.util.*;

/**
 * Objeto que representa a un usuario del sistema de recomendación.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 24-jul-2013
 */
public class    User implements Comparable<User>, EntityWithFeatures, Serializable {

    public static User ANONYMOUS_USER = new User(0, "User_Anonymous");

    private final long idUser;
    private final Map<Feature, Object> featuresValues = new TreeMap<>();
    private final String name;

    /**
     * Crea un usuario sin características. Se crea con nombre "User [idUser]".
     *
     * @param idUser Id del usuario que se crea.
     */
    public User(long idUser) {
        this(idUser, "User " + idUser);
    }

    /**
     * Crea un usuario sin características. Se crea con nombre "User [idUser]".
     *
     * @param idUser Id del usuario que se crea.
     * @param name
     */
    public User(long idUser, String name) {
        this.idUser = idUser;
        this.name = name;
    }

    /**
     * Crea un usuario a partir de sus características.
     *
     * @param idUser
     * @param name
     * @param featureValues Mapa de (característica,valor). El tipo de la característica se infiere según si el valor
     */
    public User(long idUser, String name, Map<Feature, Object> featureValues) {
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
     * @param values vector de valores correspondientes a las características en el vector <code>features</code>
     */
    public User(long idUser, String name, Feature[] features, Object[] values) {
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
     * @return devuelve un objeto con el valor de la característica. Si la característica es nominal, es de tipo
     * <code>{@link String}</code>; si es numérico, devuelve un <code>{@link Double}</code>
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
     * Devuelve el identificador del usuario al que pertenece el contenido almacenado en este objeto.
     *
     * @return identificador del usuario
     */
    @Override
    public long getId() {
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
        return Long.toString(getId());
    }

    @Override
    public int compareTo(User otherUser) {
        return BY_ID.compare(this, otherUser);
    }

    public String getTargetId() {
        if (this == ANONYMOUS_USER) {
            return ANONYMOUS_USER.name;
        } else {
            return USER_ID_TARGET_PREFIX + getId();
        }
    }
    public static final String USER_ID_TARGET_PREFIX = "User_";

    public static String getTargetId(long idUser) {
        return new User(idUser).getTargetId();
    }

    public static User parseIdTarget(String idTarget) {
        if (idTarget.startsWith(USER_ID_TARGET_PREFIX)) {

            if (idTarget.equals(ANONYMOUS_USER.name)) {
                return ANONYMOUS_USER;
            }

            String idUser = idTarget.replace(USER_ID_TARGET_PREFIX, "");
            long idUserInt = new Long(idUser);
            return new User(idUserInt);
        } else {
            throw new IllegalArgumentException("Not a user idTarget '" + idTarget + "'");
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + Long.hashCode(this.getId());
        hash = 71 * hash + Objects.hashCode(this.getName());
        return hash;
    }
}
