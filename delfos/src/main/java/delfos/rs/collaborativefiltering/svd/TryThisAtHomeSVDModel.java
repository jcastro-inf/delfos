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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.common.Global;
import delfos.common.exceptions.ratings.NotEnoughtItemInformation;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;

/**
 * Almacena el modelo de un sistema de recomendación basado en Descomposición en
 * valores singulares.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2).
 *
 * @version 1.0 Unknow date.
 * @version 1.1 08-Julio-2013 Completada la documentación de clase.
 */
public class TryThisAtHomeSVDModel implements Serializable {

    private static final long serialVersionUID = 108L;
    /**
     * Matriz para guardar los valores que describen a cada usuario. Es una
     * matriz de vectores, cada vector indica el perfil del usuario i
     */
    private ArrayList<ArrayList<Double>> _userFeatures;
    /**
     * Matriz para guardar los valores que describen a cada producto. Es una
     * matriz de vectores en la que cada vector indica el perfil del producto i
     */
    private ArrayList<ArrayList<Double>> _itemFeatures;
    /**
     * Mapa que almacena para cada id de usuario (clave en el mapa) el indice
     * que le corresponde en la matriz {@link TryThisAtHomeSVD#_userFeatures}
     * (valor en el mapa)
     */
    private TreeMap<Integer, Integer> _itemsIndex;
    /**
     * Mapa que almacena para cada id de producto (clave en el mapa) el indice
     * que le corresponde en la matriz {@link TryThisAtHomeSVD#_itemFeatures}
     * (valor en el mapa)
     */
    private TreeMap<Integer, Integer> _usersIndex;

    /**
     * Crea el modelo a partir de las matrices de características para los
     * usuarios y productos.
     *
     * @param userFeatures Matriz de características de los usuarios.
     * @param usersIndex Índice que indica en qué fila de la matriz de
     * características de los usuarios están las características de un usuario.
     * @param itemFeatures Matriz de características de los productos.
     * @param itemsIndex Índice que indica en qué fila de la matriz de
     * características de los productos están las características de un
     * producto.
     */
    public TryThisAtHomeSVDModel(ArrayList<ArrayList<Double>> userFeatures, TreeMap<Integer, Integer> usersIndex, ArrayList<ArrayList<Double>> itemFeatures, TreeMap<Integer, Integer> itemsIndex) {

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

    /**
     * Índice que indica en qué fila de la matriz de características de los
     * usuarios están las características de un usuario.
     *
     * @return
     */
    public TreeMap<Integer, Integer> getUsersIndex() {
        return _usersIndex;
    }

    /**
     * Índice que indica en qué fila de la matriz de características de los
     * productos están las características de un producto.
     *
     * @return
     */
    public TreeMap<Integer, Integer> getItemsIndex() {
        return _itemsIndex;
    }

    /**
     * Matriz de características de los productos.
     *
     * @return
     */
    public ArrayList<ArrayList<Double>> getAllItemFeatures() {
        return _itemFeatures;
    }

    /**
     * Matriz de características de los usuarios.
     *
     * @return
     */
    public ArrayList<ArrayList<Double>> getAllUserFeatures() {
        return _userFeatures;
    }

    /**
     * Crea un modelo ampliando un modelo existente con el vector de
     * características de un usuario dado.
     *
     * @param model Modelo a ampliar.
     * @param idUser Id del usuario que se desea agregar al modelo.
     * @param newUserFeatures Vector de características del usuario a agregar.
     * @return Modelo ampliado.
     */
    public static TryThisAtHomeSVDModel addUser(TryThisAtHomeSVDModel model, int idUser, ArrayList<Double> newUserFeatures) {
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

        ArrayList<ArrayList<Double>> userFeatures = new ArrayList<>(model._userFeatures);
        TreeMap<Integer, Integer> usersIndex = new TreeMap<>(model._usersIndex);
        int idUserIndex = userFeatures.size();
        usersIndex.put(idUser, idUserIndex);
        userFeatures.add(newUserFeatures);
        ArrayList<ArrayList<Double>> itemFeatures = new ArrayList<>(model._itemFeatures);
        TreeMap<Integer, Integer> itemsIndex = new TreeMap<>(model._itemsIndex);

        return new TryThisAtHomeSVDModel(userFeatures, usersIndex, itemFeatures, itemsIndex);
    }

    public ArrayList<Double> getUserFeatures(int idUser) {
        return _userFeatures.get(_usersIndex.get(idUser));
    }

    public ArrayList<Double> getItemFeatures(int idItem) {
        return _itemFeatures.get(_itemsIndex.get(idItem));
    }

    private final Set<Integer> itemsWarned = new TreeSet<>();

    public void warningItemNotInModel(int idItem, String message, NotEnoughtItemInformation ex) {
        if (!itemsWarned.contains(idItem)) {
            Global.showWarning(message);
            itemsWarned.add(idItem);
        }
    }

    private final Set<Integer> usersWarned = new TreeSet<>();

    public void warningUserNotInModel(int idUser, String message, NotEnoughtUserInformation ex) {
        if (!usersWarned.contains(idUser)) {
            Global.showWarning(message);
            usersWarned.add(idUser);
        }
    }
}
