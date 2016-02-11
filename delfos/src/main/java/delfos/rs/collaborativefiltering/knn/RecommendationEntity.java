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
package delfos.rs.collaborativefiltering.knn;

/**
 * Entidades de recomendación. Una entidad de recomendación indica el tipo de
 * objeto al que se refiere cierta información, si es a un usuario o a un
 * producto.
 *
* @author Jorge Castro Gallardo
 * 
 * @version 1.0 Unknown date.
 * @version 1.1 25-Abril-2013.
 */
public enum RecommendationEntity {

    /**
     * Entidad de recomendación de usuario.
     */
    USER("User"),
    /**
     * Entidad de recomendación producto.
     */
    ITEM("Item");

    private RecommendationEntity(String name) {
        this.name = name;
    }
    private final String name;

    @Override
    public String toString() {
        return name;
    }
}
