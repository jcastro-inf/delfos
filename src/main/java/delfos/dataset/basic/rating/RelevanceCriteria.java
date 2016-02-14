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
package delfos.dataset.basic.rating;

import java.util.Objects;

/**
 * Clase que encapsula el criterio de relevancia que usará el sistema de
 * recomendación, las medidas de evaluación y cualquier otro componente de la
 * librería de recomendación para discernir si a un usuario le gusta un producto
 * o no
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class RelevanceCriteria {

    public static final RelevanceCriteria DEFAULT_RELEVANCE_CRITERIA = new RelevanceCriteria(4);

    private final Number threshold;

    /**
     * Constructor que asigna un el valor fijo 4 de preferencia por encima del
     * cual los productos se consideran relevantes para el usuario
     */
    public RelevanceCriteria() {
        threshold = 4;
    }

    /**
     * Constructor que asigna un valor fijo de preferencia por encima del cual
     * los productos se consideran relevantes para el usuario
     *
     * @param threshold Umbral de relevancia
     */
    public RelevanceCriteria(Number threshold) {
        if (threshold == null) {
            throw new IllegalArgumentException("The threshold cannot be null.");
        }
        this.threshold = threshold;
    }

    /**
     * Devuelve true si la valoración del producto es relevantes
     *
     * @param rating valoración del producto
     * @return true si es relevante, false si no lo es
     */
    public boolean isRelevant(Number rating) {
        if (rating == null) {
            throw new IllegalArgumentException("The rating cannot be null.");
        }
        return rating.floatValue() >= threshold.floatValue();
    }

    /**
     * Devuelve true si la valoración del producto es relevantes
     *
     * @param rating valoración del producto
     * @return true si es relevante, false si no lo es
     */
    public boolean isRelevant(Rating rating) {
        if (rating == null) {
            throw new IllegalArgumentException("The rating cannot be null.");
        }
        return isRelevant(rating.getRatingValue());
    }

    /**
     * Devuelve el umbral de relevancia que está usando actualmente el criterio
     * de relevancia
     *
     * @return número que representa el umbral de relevancia
     */
    public Number getThreshold() {
        return threshold;
    }

    @Override
    public String toString() {
        return "threshold=" + threshold.doubleValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RelevanceCriteria) {
            RelevanceCriteria relevanceCriteria = (RelevanceCriteria) obj;
            return this.threshold.equals(relevanceCriteria.threshold);
        } else {
            return super.equals(obj); //To change body of generated methods, choose Tools | Templates.
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.threshold);
        return hash;
    }

}
