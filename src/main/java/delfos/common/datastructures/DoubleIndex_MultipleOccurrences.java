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
import java.util.List;
import java.util.TreeMap;

/**
 * Clase que se usa para almacenar un índice doble de valores. Sirve para
 * recuperar los valores de manera eficiente, ya que los valores (pares) que se
 * añaden a esta colección están doblemente indexados. Clase que se usa en el
 * cálculo del coeficiente de contingencia. Almacena pares de datos y los indexa
 * según los valores de ambos componentes. Provee las operaciones necesarias
 * para saber cuántas veces está una determinada componente de los pares
 *
 * @param <Type1> Tipo 1. Debe implementar la interfaz {@link Comparable}
 * @param <Type2> Tipo 2. Debe implementar la interfaz {@link Comparable}
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2103
 * @version 1.2 23-Julio-2103 Corrección de la clase, daba fallo en ejecución
 * por no implementar el comparador de pares.
 */
public class DoubleIndex_MultipleOccurrences<Type1, Type2> implements Iterable<DoubleIndex_MultipleOccurrences.PairOnIndex> {

    /**
     * Clase que almacena un par de valores, para ser almacenados en el indice
     * doble.
     *
     * @param <Type1> Tipo 1.
     * @param <Type2> Tipo 2.
     */
    public class PairOnIndex<Type1, Type2> implements Comparable<PairOnIndex> {

        private final Type1 value1;
        private final Type2 value2;

        /**
         * Constructor de un par a partir de los dos valores que contiene.
         *
         * @param value1 Valor del tipo 1.
         * @param value2 Valor del tipo 2.
         */
        public PairOnIndex(Type1 value1, Type2 value2) {
            this.value1 = value1;
            this.value2 = value2;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DoubleIndex_MultipleOccurrences.PairOnIndex) {
                PairOnIndex pair = (PairOnIndex) obj;
                return value1.equals(pair.value1) && value2.equals(pair.value2);
            } else {
                return super.equals(obj);
            }
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 83 * hash + (this.value1 != null ? this.value1.hashCode() : 0);
            hash = 83 * hash + (this.value2 != null ? this.value2.hashCode() : 0);
            return hash;
        }

        @Override
        public int compareTo(PairOnIndex o) {
            Comparable oValue1 = (Comparable) o.value1;
            Comparable thisValue1 = (Comparable) this.value1;

            int compareTo = thisValue1.compareTo(oValue1);
            if (compareTo != 0) {
                return compareTo;
            } else {
                Comparable oValue2 = (Comparable) o.value2;
                Comparable thisValue2 = (Comparable) this.value2;
                return thisValue2.compareTo(oValue2);
            }
        }
    }
    private final TreeMap<Type1, Integer> distinctValues1 = new TreeMap<Type1, Integer>();
    private final TreeMap<Type2, Integer> distinctValues2 = new TreeMap<Type2, Integer>();
    private final TreeMap<PairOnIndex, Integer> distinctPairs = new TreeMap<PairOnIndex, Integer>();
    private final List<PairOnIndex> listPairs = new LinkedList<PairOnIndex>();
    private final List<Type1> listValues1 = new LinkedList<Type1>();
    private final List<Type2> listValues2 = new LinkedList<Type2>();

    /**
     * Añade un valor al índice.
     *
     * @param value1 Valor nuevo.
     * @param value2 Valoración del valor.
     */
    public void add(Type1 value1, Type2 value2) {
        listValues1.add(value1);
        listValues2.add(value2);
        if (distinctValues1.containsKey(value1)) {
            int num = distinctValues1.get(value1);
            num++;
            distinctValues1.put(value1, num);
        } else {
            distinctValues1.put(value1, 1);
        }

        if (distinctValues2.containsKey(value2)) {
            int num = distinctValues2.get(value2);
            num++;
            distinctValues2.put(value2, num);
        } else {
            distinctValues2.put(value2, 1);
        }

        PairOnIndex pair = new PairOnIndex(value1, value2);

        if (distinctPairs.containsKey(pair)) {
            int num = distinctPairs.get(pair);
            num++;
            distinctPairs.put(pair, num);
        } else {
            distinctPairs.put(pair, 1);
        }
        listPairs.add(pair);
    }

    /**
     * Devuelve el número de valores en el índice.
     *
     * @return Número de valores.
     */
    public int size() {
        return listPairs.size();
    }

    /**
     * Devuelve el numero de valores distintos en el índice.
     *
     * @return Número de valores distintos.
     */
    public int numDistinctType1Values() {
        return distinctValues1.size();
    }

    /**
     * Devuelve el numero de ocurrencias del valor del tipo 1.
     *
     * @param value1 Valor del tipo 1.
     * @return Número de ocurrencias del valor del tipo 1.
     */
    public int frequencyOfType1Value(Type1 value1) {
        if (distinctValues1.containsKey(value1)) {
            Integer frequency = distinctValues1.get(value1);
            return frequency;
        } else {
            throw new IllegalArgumentException("The value " + value1 + " is not a valid key");
        }
    }

    /**
     * Devuelve la probabilidad teórica de ocurrencia del valor.
     *
     * @param value1 Valor del tipo 1.
     * @return Probabilidad teórica de ocurrencia del valor.
     */
    public double probabilityOfType1Value(Type1 value1) {
        return ((double) frequencyOfType1Value(value1)) / size();
    }

    /**
     * Devuelve el número de valores distintos del tipo 2.
     *
     * @return Devuelve el número de valoraciones distintas.
     */
    public int numDistinctType2Values() {
        return distinctValues2.size();
    }

    /**
     * Devuelve el número de ocurrencias del par indicado.
     *
     * @param value1 Valor del tipo1.
     * @param value2 Valor del tipo2.
     * @return Frecuencia del par indicado.
     */
    public Integer frequencyOfPair(Type1 value1, Type2 value2) {
        PairOnIndex pair = new PairOnIndex(value1, value2);
        if (distinctPairs.containsKey(pair)) {
            return distinctPairs.get(pair);
        } else {
            return 0;
        }

    }

    /**
     * Devuelve la probabilidad teórica del par indicado.
     *
     * @param value1 Valor del tipo1.
     * @param value2 Valor del tipo2.
     * @return Frecuencia del par indicado.
     */
    public Double probabilityOfPair(Type1 value1, Type2 value2) {
        return ((double) frequencyOfPair(value1, value2)) / size();
    }

    /**
     * Devuelve el número de ocurrencias del valor del tipo 2.
     *
     * @param value2 Valor del tipo 2.
     * @return Frecuencia del valor del tipo 2.
     */
    public int frequencyOfType2Value(Type2 value2) {
        return distinctValues2.get(value2);
    }

    /**
     * Devuelve la probabilidad de que un item tenga la valoración dada
     *
     * @param type2
     * @return Probabilidad de que un item tenga la valoración dada
     */
    public double probPuntuacion(Type2 type2) {
        return ((double) frequencyOfType2Value(type2)) / size();
    }

    /**
     * Comprueba si el índice está vacío.
     *
     * @return true si está vacío, false si contiene algún valor.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Iterator<PairOnIndex> iterator() {
        return listPairs.listIterator();
    }

    /**
     * Devuelve un itelador de la lista de valores de tipo 1.
     *
     * @return Iterador de la lista de valores de tipo 1.
     */
    public Iterator<Type1> iteratorType1Values() {
        return new LinkedList<Type1>(listValues1).listIterator();
    }

    /**
     * Devuelve un itelador de la lista de valores de tipo 1.
     *
     * @return Iterador de la lista de valores de tipo 1.
     */
    public Iterator<Type2> iteratorType2Values() {
        return new LinkedList<Type2>(listValues2).listIterator();
    }

    /**
     * Elimina todas las ocurrencias del indice, dejándolo vacío.
     */
    public void clear() {
        distinctPairs.clear();
        distinctValues1.clear();
        distinctValues2.clear();
        listPairs.clear();
    }
}
