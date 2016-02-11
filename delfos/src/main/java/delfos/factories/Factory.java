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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.parameters.ParameterOwner;

/**
 * Clase que define el comportamiento general de una factoría. Las factorías que
 * hereden de esta se deben implementar como singleton, es decir, implementando
 * el método:
 *
 * <p>
 * public static Factory getInstance();
 *
 * <p>
 * Además deben añadir las clases que crean en su fragmento de inicialización de
 * clase.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 09-May-2013
 * @param <TypeForCreation>
 */

public class Factory<TypeForCreation extends ParameterOwner> {

    /**
     * Diccionario de todas las clases que implementan la clase genérica
     * indicada, indexados por nombre de la clase que lo implementa.
     */
    protected final Map<String, Class<? extends TypeForCreation>> allClasses = new TreeMap<>();

    /**
     * Indica a la biblioteca de recomendación que hay una nueva clase y la
     * añade a la lista de clases conocidas de este tipo. Le añade también el
     * alias descrito en el parámetro oldName, para compatibilidad hacia atrás.
     *
     * @param classObject Clase del tipo que almacena esta factoría.
     * @param oldName Clase del tipo que almacena esta factoría.
     */
    @SuppressWarnings("unchecked")
    public void addClass_oldName(Class<? extends TypeForCreation> classObject, String oldName) {
        if (!allClasses.containsKey(oldName)) {
            allClasses.put(oldName, classObject);
        } else {
            Global.showWarning("The class " + classObject.getName() + " was already known by the factory (" + this.getClass().getSimpleName() + ")");
            throw new IllegalArgumentException("Name collision in a factory!");
        }
        if (ParameterOwner.class.isAssignableFrom(classObject)) {
            ParameterOwnerFactory.getInstance().addClass((Class<ParameterOwner>) classObject);
        }
    }

    /**
     * Indica a la biblioteca de recomendación que hay una nueva clase y la
     * añade a la lista de clases conocidas de este tipo.
     *
     * @param clase Clase del tipo que almacena esta factoría.
     */
    @SuppressWarnings("unchecked") //TODO: eliminar este supressWarning haciendo que el código autocompruebe el cast.
    public void addClass(Class<? extends TypeForCreation> clase) {
        if (!allClasses.containsKey(clase.getSimpleName())) {
            allClasses.put(clase.getSimpleName(), clase);
        } else {
            if (Global.isInfoPrinted()) {
                Global.showWarning("The class " + clase.getName() + " was already known by the factory (" + this.getClass().getSimpleName() + ")");
            }
        }
        if (ParameterOwner.class.isAssignableFrom(clase)) {
            ParameterOwnerFactory.getInstance().addClass((Class<ParameterOwner>) clase);
        }
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
    public TypeForCreation getClassByName(String className) {

        if (allClasses.isEmpty()) {
            ERROR_CODES.NO_INSTANCES_IN_FACTORY.exit(new IllegalStateException("Never added a instance to this factory: " + this.getClass().getName()));
        }

        if (className == null) {
            throw new IllegalArgumentException("The argument className cannot be null.");
        }

        if (className.equals("")) {
            throw new IllegalArgumentException("The argument className cannot be empty.");
        }

        Class<? extends TypeForCreation> claseCoincidente = allClasses.get(className);

        if (claseCoincidente != null) {
            try {
                return claseCoincidente.newInstance();
            } catch (Throwable ex) {
                exceptionInCreation(claseCoincidente, ex);
            }
        }

        return null;
    }

    public List<Class<? extends TypeForCreation>> getAllClassesClass() {
        return new LinkedList<>(allClasses.values());
    }

    /**
     * Obtiene una instancia de cada clase conocida por esta factoría, creada
     * con el constructor por defecto.
     *
     * @return Lista con una instancia de cada clase que la factoría conoce.
     */
    public List<TypeForCreation> getAllClasses() {
        if (allClasses.isEmpty()) {
            ERROR_CODES.NO_INSTANCES_IN_FACTORY.exit(new IllegalStateException("Never added a instance to this factory: " + this.getClass().getName()));
        }
        List<TypeForCreation> ret = new ArrayList<>();

        allClasses.values().stream().forEach((c) -> {
            try {
                ret.add(c.newInstance());
            } catch (Throwable ex) {
                exceptionInCreation(c, ex);
            }
        });
        if (ret.isEmpty()) {
            ERROR_CODES.NO_INSTANCES_IN_FACTORY.exit(new IllegalStateException("There not exists any instances to return for this method!"));
        }
        return ret;
    }

    public <Type extends TypeForCreation> List<Type> getAllClasses(Class<Type> type) {
        ArrayList<Type> ret = new ArrayList<>();
        allClasses.values().stream()
                .filter((c) -> (type.isAssignableFrom(c)))
                .forEach((c) -> {
                    try {
                        Type recommender = (Type) c.newInstance();
                        ret.add(recommender);
                    } catch (IllegalAccessException | InstantiationException ex) {
                        exceptionInCreation(c, ex);
                    }
                });
        return ret;
    }

    /**
     * Imprime la clase que no se pudo crear y el error que describe la causa.
     *
     * @param clase Clase que no se puede crear.
     * @param ex Causa del error.
     */
    protected static void exceptionInCreation(Class<?> clase, Throwable ex) {
        Global.showWarning(clase.getSimpleName() + ": " + ex.getMessage());
        Global.showError(ex);
    }
}
