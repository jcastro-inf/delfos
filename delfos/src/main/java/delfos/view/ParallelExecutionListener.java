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
package delfos.view;

/**
 * Interfaz que define el método invocado cuando se termine la ejecución deun
 * caso de estudio {@link CaseStudy.CaseStudy} concreto.
 *
 *
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 *
 * @deprecated Esta interfaz no se usa debido a que otras interfaces hacen la
 * misma funcion de observador
 */
@Deprecated
public interface ParallelExecutionListener {

    /**
     * Indica al listener que la ejecución del caso de estudio ha finalizado.
     *
     * @param error Indica si ha habido errores en la ejecución.
     */
    public void executionFinished(boolean error);
}
