package delfos.group.grs.preferenceaggregation.order;

/**
 * Interfaz que define el método para consultar la probabilidad de que una
 * alternativa preceda a otra teniendo en cuenta la información que se ha
 * indicado a la instanciación concreta de esta interfaz.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 (01/12/2012)
 */
public interface Preff<E> {

    /**
     * Devuelve la probabilidad de que la alternativa e1 preceda a la e2, es
     * decir, que la alternativa 1 sea preferida sobre la alternativa 2.
     *
     * @param e1 Alternativa 1.
     * @param e2 Alternativa 2.
     * @return Probabilidad de que la alternativa 1 preceda a la alternativa 2.
     * Como devuelve una probabilidad, el valor está comprendido entre 0 y 1.
     */
    public float preff(E e1, E e2);
}
