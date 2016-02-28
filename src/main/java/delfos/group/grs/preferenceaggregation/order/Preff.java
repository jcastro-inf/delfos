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
package delfos.group.grs.preferenceaggregation.order;

/**
 * Interfaz que define el método para consultar la probabilidad de que una
 * alternativa preceda a otra teniendo en cuenta la información que se ha
 * indicado a la instanciación concreta de esta interfaz.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
    public double preff(E e1, E e2);
}
