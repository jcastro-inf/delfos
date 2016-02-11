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
package delfos.rs;

import java.io.Serializable;

/**
 * Interfaz que define los métodos comunes a todos los periles de productos que
 * los sistemas de recomendación utilicen
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 9-Octubre-2013
 */
public interface ItemProfile extends Serializable {

    /**
     * Obtienen el identificador del producto al que pertenece este perfil.
     *
     * @return Identificador del producto al que pertenece este perfil.
     */
    public int getId();
}
