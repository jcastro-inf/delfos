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
package delfos.rs.collaborativefiltering.svd;

import delfos.common.exceptions.ratings.NotEnoughtItemInformation;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.user.User;
import delfos.rs.collaborativefiltering.als.Bias;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Almacena el modelo de un sistema de recomendación basado en Descomposición en valores singulares.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknow date.
 * @version 1.1 08-Julio-2013 Completada la documentación de clase.
 */
public class TryThisAtHomeSVDModel implements Serializable {

    private static final long serialVersionUID = 108L;
    /**
     * Matriz para guardar los valores que describen a cada usuario. Es una matriz de vectores, cada vector indica el
     * perfil del usuario i
     */
    private List<List<Double>> _userFeatures;
    /**
     * Matriz para guardar los valores que describen a cada producto. Es una matriz de vectores en la que cada vector
     * indica el perfil del producto i
     */
    private List<List<Double>> _itemFeatures;
    /**
     * Mapa que almacena para cada id de usuario (clave en el mapa) el indice que le corresponde en la matriz
     * {@link TryThisAtHomeSVD#_userFeatures} (valor en el mapa)
     */
    private Map<Integer, Integer> _itemsIndex;
    /**
     * Mapa que almacena para cada id de producto (clave en el mapa) el indice que le corresponde en la matriz
     * {@link TryThisAtHomeSVD#_itemFeatures} (valor en el mapa)
     */
    private Map<Integer, Integer> _usersIndex;
    private Bias bias;

    /**
     * Crea el modelo a partir de las matrices de características para los usuarios y productos.
     *
     * @param userFeatures Matriz de características de los usuarios.
     * @param usersIndex Índice que indica en qué fila de la matriz de características de los usuarios están las
     * características de un usuario.
     * @param itemFeatures Matriz de características de los productos.
     * @param itemsIndex Índice que indica en qué fila de la matriz de características de los productos están las
     * características de un producto.
     */
    public TryThisAtHomeSVDModel(List<List<Double>> userFeatures, TreeMap<Integer, Integer> usersIndex, List<List<Double>> itemFeatures, TreeMap<Integer, Integer> itemsIndex) {

        if (userFeatures.size() != usersIndex.size()) {
            throw new IllegalArgumentException("The feature matrix and the user index do not have the same size.");
        }
        if (itemFeatures.size() != itemsIndex.size()) {
            throw new IllegalArgumentException("The feature matrix and the item index do not have the same size.");
        }

        if (userFeatures.get(0).size() != itemFeatures.get(0).size()) {
            throw new IllegalArgumentException("The model have different number of features for users and items.");
        }

        this._userFeatures = userFeatures;
        this._usersIndex = usersIndex;

        this._itemFeatures = itemFeatures;
        this._itemsIndex = itemsIndex;
    }

    public TryThisAtHomeSVDModel(Map<User, List<Double>> userFeatures, Map<Item, List<Double>> itemFeatures, Bias bias) {

        List<User> usersSorted = userFeatures.keySet().stream().sorted().collect(Collectors.toList());
        List<Item> itemsSorted = itemFeatures.keySet().stream().sorted().collect(Collectors.toList());

        Map<Integer, Integer> usersIndex = IntStream.range(0, usersSorted.size()).boxed().collect(Collectors.toMap(
                i -> usersSorted.get(i).getId(),
                i -> i));

        Map<Integer, Integer> itemsIndex = IntStream.range(0, itemsSorted.size()).boxed().collect(Collectors.toMap(
                index -> itemsSorted.get(index).getId(),
                index -> index));

        List<List<Double>> _userFeatures = IntStream.range(0, usersIndex.size()).boxed()
                .map(index -> new ArrayList<Double>())
                .collect(Collectors.toCollection(ArrayList::new));

        userFeatures.entrySet().parallelStream().forEach(entry -> {
            User user = entry.getKey();
            List<Double> featureVector = entry.getValue();
            Integer userIndex = usersIndex.get(user.getId());
            _userFeatures.set(userIndex, featureVector);
        });

        List<List<Double>> _itemFeatures = IntStream.range(0, itemsIndex.size()).boxed()
                .map(index -> new ArrayList<Double>())
                .collect(Collectors.toCollection(ArrayList::new));

        itemFeatures.entrySet().parallelStream().forEach(entry -> {
            Item item = entry.getKey();
            List<Double> featureVector = entry.getValue();
            Integer index = itemsIndex.get(item.getId());
            _itemFeatures.set(index, featureVector);
        });

        if (userFeatures.size() != usersIndex.size()) {
            throw new IllegalArgumentException("The feature matrix and the user index do not have the same size.");
        }
        if (itemFeatures.size() != itemsIndex.size()) {
            throw new IllegalArgumentException("The feature matrix and the item index do not have the same size.");
        }

        this._userFeatures = _userFeatures;
        this._usersIndex = usersIndex;

        this._itemFeatures = _itemFeatures;
        this._itemsIndex = itemsIndex;
        this.bias = bias;

    }

    /**
     * Índice que indica en qué fila de la matriz de características de los usuarios están las características de un
     * usuario.
     *
     * @return
     */
    public Map<Integer, Integer> getUsersIndex() {
        return _usersIndex;
    }

    /**
     * Índice que indica en qué fila de la matriz de características de los productos están las características de un
     * producto.
     *
     * @return
     */
    public Map<Integer, Integer> getItemsIndex() {
        return _itemsIndex;
    }

    /**
     * Matriz de características de los productos.
     *
     * @return
     */
    public List<List<Double>> getAllItemFeatures() {
        return _itemFeatures;
    }

    /**
     * Matriz de características de los usuarios.
     *
     * @return
     */
    public List<List<Double>> getAllUserFeatures() {
        return _userFeatures;
    }

    /**
     * Crea un modelo ampliando un modelo existente con el vector de características de un usuario dado.
     *
     * @param model Modelo a ampliar.
     * @param idUser Id del usuario que se desea agregar al modelo.
     * @param newUserFeatures Vector de características del usuario a agregar.
     * @return Modelo ampliado.
     */
    public static TryThisAtHomeSVDModel addUser(TryThisAtHomeSVDModel model, int idUser, List<Double> newUserFeatures) {
        if (model == null) {
            throw new IllegalArgumentException("The model cannot be null.");
        }

        if (newUserFeatures == null) {
            throw new IllegalArgumentException("The new user feature vector is null.");
        }

        if (model.getUsersIndex().containsKey(idUser)) {
            throw new IllegalArgumentException("The new user ID already exists in the former model.");
        }

        if (model._userFeatures.get(0).size() != newUserFeatures.size()) {
            throw new IllegalArgumentException("The number of features is different for the new user ( " + model._userFeatures.get(0).size() + " != " + newUserFeatures.size() + " )");
        }

        List<List<Double>> userFeatures = new ArrayList<>(model._userFeatures);
        TreeMap<Integer, Integer> usersIndex = new TreeMap<>(model._usersIndex);
        int idUserIndex = userFeatures.size();
        usersIndex.put(idUser, idUserIndex);
        userFeatures.add(newUserFeatures);
        List<List<Double>> itemFeatures = new ArrayList<>(model._itemFeatures);
        TreeMap<Integer, Integer> itemsIndex = new TreeMap<>(model._itemsIndex);

        return new TryThisAtHomeSVDModel(userFeatures, usersIndex, itemFeatures, itemsIndex);
    }

    public List<Double> getUserFeatures(int idUser) {
        return _userFeatures.get(_usersIndex.get(idUser));
    }

    public List<Double> getItemFeatures(int idItem) {
        return _itemFeatures.get(_itemsIndex.get(idItem));
    }

    private final Set<Integer> itemsWarned = new TreeSet<>();

    public void warningItemNotInModel(int idItem, String message, NotEnoughtItemInformation ex) {
        if (!itemsWarned.contains(idItem)) {
            //Global.showWarning(message);
            itemsWarned.add(idItem);
        }
    }

    private final Set<Integer> usersWarned = new TreeSet<>();

    public void warningUserNotInModel(int idUser, String message, NotEnoughtUserInformation ex) {
        if (!usersWarned.contains(idUser)) {
            //Global.showWarning(message);
            usersWarned.add(idUser);
        }
    }

    public double predict(User user, Item item) {
        List<Double> userVector = getUserFeatures(user.getId());
        List<Double> itemVector = getItemFeatures(item.getId());

        return IntStream.range(0, userVector.size())
                .mapToDouble(index -> userVector.get(index) * itemVector.get(index))
                .sum();
    }

    public double predictRating(User user, Item item) {

        double predict = predict(user, item);

        if (bias == null) {
            return predict;
        } else {
            return bias.restoreBias(user, item, predict);
        }
    }

}
