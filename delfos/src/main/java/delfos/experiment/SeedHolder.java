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
package delfos.experiment;

import delfos.Constants;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.LongParameter;

/**
 * Interfaz que establece los métodos que una clase que utilice valores
 * aleatorios debe utilizar para que sus muestras sean 'reliable'. Una de las
 * propiedades que debe garantizar es que si se realizan dos muestreos con la
 * misma semilla y los mismos datos, el resultado es idéntico.
 *
 *
 * Es altamente recomendable añadir el siguiente código al constructor de una
 * clase que implemente el {@link SeedHolder}:
 *
 * addParammeterListener(new ParameterListener() { private long valorAnterior =
 * (Long) getParameterValue(SEED); {@literal @}Override public void
 * parameterChanged() { long newValue = (Long) getParameterValue(SEED); if
 * (valorAnterior != newValue) { Global.showWarning("Reset " + getName() + " to
 * SEED = " + newValue + "\n"); valorAnterior = newValue;
 * setSeedValue(newValue); } } });
 *
* @author Jorge Castro Gallardo
 * @version 1.0 (08-01-2012)
 */
public interface SeedHolder {

    /**
     * Parámetro para almacenar el valor de la semilla utilizada para esta
     * técnica de validación.
     */
    public static final Parameter SEED = new Parameter("seed", new LongParameter(Long.MIN_VALUE, Long.MAX_VALUE, Constants.getCurrentTimeMillis()));

    /**
     * Establece el nuevo valor de la semilla y reinicia la secuencia de valores
     * aleatorios.
     *
     * @param seedValue Nuevo valor para la semilla.
     */
    public void setSeedValue(long seedValue);

    /**
     * Devuelve la semilla actual.
     *
     * @return Semilla utilizada
     */
    public long getSeedValue();
}
