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

import delfos.dataset.basic.features.EntityWithFeatures;

/**
 * Excepción que se lanza al intentar buscar una entidad que no existe en la
 * colección {@link CollectionOfEntitiesWithFeatures}.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 4-Octubre-2013
 */
public class EntityNotFound extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final int id;
    private final Class<?> entityClass;

    /**
     * Crea la excepción a partir del identificador de la entidad que no se
     * encuentra.
     *
     * @param entityClass
     * @param id Identificador de la entidad no encontrada.
     */
    public EntityNotFound(Class<?> entityClass, int id) {
        super("Entitiy of class " + entityClass.getName() + " '" + id + "' not found");
        this.id = id;
        this.entityClass = entityClass;
    }

    public EntityNotFound(Class<?> entityClass, int id, Throwable cause) {
        super("Entitiy '" + id + "' not found", cause);
        this.id = id;
        this.entityClass = entityClass;
    }

    public EntityNotFound(Class<?> entityClass, int id, String msg) {
        super(msg);
        this.id = id;
        this.entityClass = entityClass;
    }

    public EntityNotFound(Class<?> entityClass, int id, Throwable cause, String msg) {
        super(msg, cause);
        this.id = id;
        this.entityClass = entityClass;
    }

    public int getIdEntity() {
        return id;
    }

    /**
     * Comprueba el tipo de entidad que generó la excepción. Si no es la
     * correcta, se lanza una {@link IllegalStateException}
     *
     * @param classToCompare Clase de la que debe ser la entidad para que no
     * lanze error.
     */
    public void isA(Class<? extends EntityWithFeatures> classToCompare) {
        if (!entityClass.isAssignableFrom(classToCompare)) {
            throw new IllegalStateException("This error entity is a " + entityClass + " not a " + classToCompare);
        }
    }
}
