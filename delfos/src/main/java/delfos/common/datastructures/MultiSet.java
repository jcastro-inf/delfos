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
package delfos.common.datastructures;

import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * Estructura de diccionario que almacena el número de apariciones de las
 * claves. Es un multiconjunto.
 *
 * @param <E> Tipo de los objetos que se guardan en el multiset.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.1 26-Jan-2013 Se generaliza la clase.
 * @version 1.0 Unknow date
 */
public class MultiSet<E> {

    private TreeMap<E, Integer> multiplicidad;
    private int n;

    /**
     * Crea un multiset vacío.
     */
    public MultiSet() {
        n = 0;
        multiplicidad = new TreeMap<E, Integer>();
    }

    /**
     * Se añade la clave al diccionario con multiplicidad 1. Si ya existía se
     * aumenta su multiplicidad en 1
     *
     * @param clave Clave que se desea añadir al diccionario
     */
    public void add(E clave) {
        if (clave != null) {
            if (multiplicidad.containsKey(clave)) {
                int num = multiplicidad.get(clave);
                num++;
                multiplicidad.put(clave, num);
            } else {
                multiplicidad.put(clave, 1);
            }
            n++;
        }
    }

    /**
     * Devuelve true si la clave se encuentra en el diccionario.
     *
     * @param clave clave a buscar
     * @return true si está , false si no está en el diccionario
     */
    public boolean contains(E clave) {
        return multiplicidad.containsKey(clave);
    }

    /**
     * Devuelve el número de apariciones de una clave en el diccionario. Si la
     * clave no está, en lugar de provocar un error devuelve 0 (ya que realmente
     * no tiene ninguna aparición en el diccionario)
     *
     * @param clave
     * @return numero de apariciones de la clave en el diccionario
     * (multiplicidad)
     */
    public int getFreq(E clave) {
        if (multiplicidad.containsKey(clave)) {
            return multiplicidad.get(clave);
        } else {
            return 0;
        }
    }

    /**
     * Devuelve todas las claves que tienen al menos una aparición en el
     * diccionario
     *
     * @return Conjunto de claves con frecuencia distinta de cero
     */
    public Set<E> keySet() {
        return multiplicidad.keySet();
    }

    /**
     * Devuelve el número total de objetos que se han añadido al diccionario. Si
     * se añade dos veces el mismo objeto, entonces el valor devuelto por este
     * método incrementará en dos unidades.
     *
     * @return número de valores añadidos al diccionario
     * @see MultiSet#add(java.lang.Object)
     */
    public int getN() {
        return n;
    }

    /**
     * Devuelve el contenido del multiset en una cadena amigable para el
     * usuario.
     *
     * @return Cadena que representa el contenido del multiset.
     */
    public String printContent() {
        StringBuilder ret = new StringBuilder();
        ret.append("Content of multiset: (key --> occurrences\n");
        for (Entry<E, Integer> entry : multiplicidad.entrySet()) {
            E clave = entry.getKey();
            int apariciones = entry.getValue();

            ret.append(clave.toString());
            ret.append(" --> ");
            ret.append(apariciones);
            ret.append("\n");
        }

        return ret.toString();
    }

    /**
     * Vacia todas las ocurrencias de un valor dado.
     *
     * @param value Valor para el que se borran las ocurrencias.
     */
    public void removeAllOccurrences(E value) {
        if (multiplicidad.containsKey(value)) {
            int eliminar = getFreq(value);
            n -= eliminar;
            multiplicidad.remove(value);
        } else {
            throw new IllegalArgumentException("Multimap doesn't contains key = '" + value + "'");
        }
    }

    /**
     * Comprueba si el multiset está vacío.
     *
     * @return true si está vacío, false si contiene algún valor.
     */
    public boolean isEmpty() {
        return n == 0;
    }
}
