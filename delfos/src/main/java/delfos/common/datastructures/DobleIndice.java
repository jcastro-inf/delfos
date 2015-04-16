package delfos.common.datastructures;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 * Clase que se usa en el cálculo del coeficiente de contingencia. Almacena
 * pares de datos y los indexa según los valores de ambos componentes. Provee
 * las operaciones necesarias para saber cuántas veces está una determinada
 * componente de los pares
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2103
 *
 * @deprecated Utilizar la clase que implementa el índice de forma genérica:
 * {@link DoubleIndex}.
 */
@Deprecated
public class DobleIndice {

    private final TreeMap<String, Integer> distinctValues = new TreeMap<String, Integer>();
    private final TreeMap<String, Integer> distinctRatings = new TreeMap<String, Integer>();
    private final TreeMap<String, Integer> distinctPairs = new TreeMap<String, Integer>();
    /**
     * Número de pares de valores incluidos en el índice.
     */
    private int n = 0;
    private final List<String> valuesList = new LinkedList<String>();
    private final List<String> ratingsList = new LinkedList<String>();
    private final List<String> pairsList = new LinkedList<String>();

    /**
     * Añade un valor al índice.
     *
     * @param value Valor nuevo.
     * @param rating Valoración del valor.
     */
    public void add(String value, String rating) {
        if (distinctValues.containsKey(value)) {
            int num = distinctValues.get(value);
            num++;
            distinctValues.put(value, num);
        } else {
            distinctValues.put(value, 1);
            valuesList.add(value);
        }

        if (distinctRatings.containsKey(rating)) {
            int num = distinctRatings.get(rating);
            num++;
            distinctRatings.put(rating, num);
        } else {
            distinctRatings.put(rating, 1);
            ratingsList.add(rating);
        }

        String claveConjunta = value + "." + rating;
        if (distinctPairs.containsKey(claveConjunta)) {
            int num = distinctPairs.get(claveConjunta);
            num++;
            distinctPairs.put(claveConjunta, num);
        } else {
            distinctPairs.put(claveConjunta, 1);
            pairsList.add(claveConjunta);
        }

        n++;
    }

    /**
     * Devuelve el número de valores en el índice.
     *
     * @return Número de valores.
     */
    public int size() {
        return n;
    }

    /**
     * Devuelve el numero de valores distintos en el índice.
     *
     * @return Número de valores distintos.
     */
    public int numValoresDistintos() {
        return distinctValues.size();
    }

    /**
     * Devuelve el numero de ocurrencias del valor.
     *
     * @param value Valor para el que se busca su frecuencia.
     * @return Frecuencia del valor.
     */
    public int freqValor(String value) {
        return distinctValues.get(value);
    }

    ;

    /**
     * Devuelve la probabilidad teórica de ocurrencia del valor.
     *
     * @param value
     * @return
     */
    public float probValor(String value) {
        return ((float) freqValor(value)) / n;
    }

    /**
     * Devuelve el numero de puntuaciones distintas.
     *
     * @return Devuelve el número de valoraciones distintas.
     */
    public int numPuntuacionesDistintas() {
        return distinctRatings.size();
    }

    /**
     * Devuelve la frecuencia del par indicado.
     *
     * @param value Valor del par.
     * @param rating Valoración del par.
     * @return
     */
    public Integer freqPar(String value, String rating) {
        if (distinctPairs.containsKey(value + "." + rating)) {
            return distinctPairs.get(value + "." + rating);
        } else {
            return 0;
        }

    }

    /**
     *
     * @param valor
     * @param puntuacion
     * @return
     */
    public Float probPar(String valor, String puntuacion) {
        return ((float) freqPar(valor, puntuacion)) / n;
    }

    /**
     * devuelve el numero de veces que el usuario ha puntuado con el valor dado
     *
     * @param puntuacion
     * @return
     */
    public int freqPuntuacion(String puntuacion) {
        return distinctRatings.get(puntuacion);
    }

    /**
     * Devuelve la probabilidad teórica de que un item tenga la valoración dada
     *
     * @param puntuacion Puntuación para la que se busca la probabilidad.
     * @return Probabilidad de que un item tenga la valoración dada.
     */
    public float probPuntuacion(String puntuacion) {
        return ((float) freqPuntuacion(puntuacion)) / n;
    }

    /**
     * Devuelve la lista de pares.
     *
     * @return
     */
    public List<String> getListaPares() {
        return new LinkedList<String>(pairsList);
    }

    /**
     * Devuelve las puntuaciones.
     *
     * @return Lista de puntuaciones.
     */
    public List<String> getListaPuntuaciones() {
        return Collections.unmodifiableList(ratingsList);
    }

    /**
     * Devuelve los valores.
     *
     * @return Lista de valores.
     */
    public List<String> getListaValores() {
        return Collections.unmodifiableList(valuesList);
    }
}
