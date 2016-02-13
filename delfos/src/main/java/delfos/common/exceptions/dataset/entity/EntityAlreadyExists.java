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
package delfos.common.exceptions.dataset.entity;

/**
 * Excepción que se lanza al intentar añadir una entidad que ya existe (con
 * mismo identificador) a un {@link CollectionOfEntitiesWithFeatures}.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 4-Octubre-2013
 */
public class EntityAlreadyExists extends Error {

    private static final long serialVersionUID = 1L;
    private final int id;

    /**
     * Crea la excepción a partir del identificador de la entidad que se repite
     *
     * @param id Identificador de la entidad.
     */
    public EntityAlreadyExists(int id) {
        super("Entitiy '" + id + "' not found");
        this.id = id;
    }

    public EntityAlreadyExists(int id, Throwable cause) {
        super("Entitiy '" + id + "' not found", cause);
        this.id = id;
    }

    public int getIdEntity() {
        return id;
    }
}
