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
package delfos.common.exceptions.ratings;

/**
 * Excepción que se lanza cuando no se tiene información suficiente sobre un
 * producto. Los siguientes ejemplos ilustran los posibles usos de esta
 * excepción:
 *
 * <p>
 * <p>
 * Cuando un producto no tiene valoraciones suficientes para calcular su perfil.
 * <p>
 * <p>
 *
* @author Jorge Castro Gallardo
 *
 * @version 9-abril-2014
 */
public class NotEnoughtItemInformation extends Exception {

    private static final long serialVersionUID = 1L;

    public NotEnoughtItemInformation(String message) {
        super(message);
    }

    public NotEnoughtItemInformation(int idItem, String svd_recommendation_model_does_not_contain) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
