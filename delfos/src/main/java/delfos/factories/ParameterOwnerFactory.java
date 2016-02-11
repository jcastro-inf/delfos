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
package delfos.factories;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.parameters.ParameterOwner;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Factoría de todos los elementos de tipo ParameterOwner
 *
 * @author Jorge Castro Gallardo
 * @version 1.0 15-May-2013
 */
public class ParameterOwnerFactory {

    private static final ParameterOwnerFactory instance = new ParameterOwnerFactory();

    public static ParameterOwnerFactory getInstance() {
        return instance;
    }

    private ParameterOwnerFactory() {
    }
    /**
     * Diccionario de todas las clases que implementan la clase genérica
     * indicada, indexados por nombre de la clase que lo implementa.
     */
    protected final Collection<Class<? extends ParameterOwner>> allClasses = new LinkedList<Class<? extends ParameterOwner>>();

    /**
     * Indica a la biblioteca de recomendación que hay una nueva clase y la
     * añade a la lista de clases conocidas de este tipo.
     *
     * @param clase Clase del tipo que almacena esta factoría.
     */
    public void addClass(Class<? extends ParameterOwner> clase) {
        allClasses.add(clase);
    }

    /**
     * Busca la clase de entre las conocidas por la biblioteca, la crea y
     * devuelve.Si el nombre no coincide con ninguna de las clases conocidas,
     * devuelve null.
     *
     * @param className Nombre de la clase a buscar.
     * @return Instancia de la clase, creada con el constructor por defecto. Si
     * el nombre no coincide con ninguna de las clases conocidas, devuelve null.
     */
    public ParameterOwner getClassByName(String className) {
        if (allClasses.isEmpty()) {
            ERROR_CODES.NO_INSTANCES_IN_FACTORY.exit(new IllegalStateException("Never added a instance to this factory: " + this.getClass().getName()));
        }

        if (className == null) {
            throw new IllegalArgumentException("The argument className cannot be null.");
        }

        if (className.equals("")) {
            throw new IllegalArgumentException("The argument className cannot be empty.");
        }

        Class<? extends ParameterOwner> claseCoincidente = null;

        //Busco la clase que tiene el nombre igual
        for (Class<? extends ParameterOwner> c : allClasses) {
            if (c.getSimpleName().equals(className)) {
                claseCoincidente = c;
                break;
            }
        }

        if (claseCoincidente != null) {
            try {
                return claseCoincidente.newInstance();
            } catch (Throwable ex) {
                exceptionInCreation(claseCoincidente, ex);
            }
        }

        return null;
    }

    /**
     * Obtiene una instancia de cada clase conocida por esta factoría, creada
     * con el constructor por defecto.
     *
     * @return Lista con una instancia de cada clase que la factoría conoce.
     */
    public List<ParameterOwner> getAllClasses() {
        if (allClasses.isEmpty()) {
            ERROR_CODES.NO_INSTANCES_IN_FACTORY.exit(new IllegalStateException("Never added a instance to this factory: " + this.getClass().getName()));
        }
        List<ParameterOwner> ret = Collections.synchronizedList(new ArrayList<>());

        for (Class<? extends ParameterOwner> c : allClasses) {
            try {
                ret.add(c.newInstance());
            } catch (Throwable ex) {
                exceptionInCreation(c, ex);
            }
        }
        if (ret.isEmpty()) {
            ERROR_CODES.NO_INSTANCES_IN_FACTORY.exit(new IllegalStateException("There not exists any instances to return for this method!"));
        }
        return ret;
    }

    /**
     * Imprime la clase que no se pudo crear y el error que describe la causa.
     *
     * @param clase Clase que no se puede crear.
     * @param ex Causa del error.
     */
    protected static void exceptionInCreation(Class<?> clase, Throwable ex) {
        IllegalStateException ise = new IllegalStateException(clase.getSimpleName() + ": " + ex.getMessage());
        Global.showError(ise);
        Global.showError(ex);
    }
}
