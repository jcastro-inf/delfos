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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

/**
 * Clase que se usa para almacenar un índice doble de valores. Sirve para
 * recuperar los valores de manera eficiente, ya que los valores (pares) que se
 * añaden a esta colección están doblemente indexados. Clase que se usa en el
 * cálculo del coeficiente de contingencia. Almacena pares de datos y los indexa
 * según los valores de ambos componentes. Provee las operaciones necesarias
 * para saber cuántas veces está una determinada componente de los pares
 *
 * @param <TypeOne> Tipo 1. Debe implementar la interfaz {@link Comparable}
 * @param <TypeTwo> Tipo 2. Debe implementar la interfaz {@link Comparable}
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 12-Diciembre-2103
 */
public class DoubleMapping<TypeOne, TypeTwo> {

    private final TreeMap<TypeOne, TypeTwo> correspondenceByTypeOne = new TreeMap<TypeOne, TypeTwo>();
    private final TreeMap<TypeTwo, TypeOne> correspondenceByTypeTwo = new TreeMap<TypeTwo, TypeOne>();

    public boolean containsType1Value(TypeOne value) {
        return correspondenceByTypeOne.containsKey(value);
    }

    public boolean containsType2Value(TypeTwo value) {
        return correspondenceByTypeTwo.containsKey(value);
    }

    /**
     * Añade un valor al índice.
     *
     * @param value1 Valor nuevo.
     * @param value2 Valoración del valor.
     */
    public void add(TypeOne value1, TypeTwo value2) {
        if (containsType1Value(value1)) {
            throw new IllegalArgumentException("The value1 ('" + value1 + "') has been already defined!!");
        }
        if (containsType2Value(value2)) {
            throw new IllegalArgumentException("The value2 ('" + value2 + "') has been already defined!!");
        }

        correspondenceByTypeOne.put(value1, value2);
        correspondenceByTypeTwo.put(value2, value1);
    }

    public TypeTwo typeOneToTypeTwo(TypeOne value) {
        return correspondenceByTypeOne.get(value);
    }

    public TypeOne typeTwoToTypeOne(TypeTwo value) {
        return correspondenceByTypeTwo.get(value);
    }

    /**
     * Devuelve el número de valores en el índice.
     *
     * @return Número de valores.
     */
    public int size() {
        return correspondenceByTypeOne.size();
    }

    /**
     * Comprueba si el índice está vacío.
     *
     * @return true si está vacío, false si contiene algún valor.
     */
    public boolean isEmpty() {
        return correspondenceByTypeOne.isEmpty();
    }

    /**
     * Devuelve un itelador de la lista de valores de tipo 1.
     *
     * @return Iterador de la lista de valores de tipo 1.
     */
    public Iterator<TypeOne> iteratorType1Values() {
        return new LinkedList<TypeOne>(correspondenceByTypeOne.keySet()).listIterator();
    }

    /**
     * Devuelve un itelador de la lista de valores de tipo 1.
     *
     * @return Iterador de la lista de valores de tipo 1.
     */
    public Iterator<TypeTwo> iteratorType2Values() {
        return new LinkedList<TypeTwo>(correspondenceByTypeTwo.keySet()).listIterator();
    }

    /**
     * Elimina todas las ocurrencias del indice, dejándolo vacío.
     */
    public void clear() {
        correspondenceByTypeOne.clear();
        correspondenceByTypeTwo.clear();
    }
}
