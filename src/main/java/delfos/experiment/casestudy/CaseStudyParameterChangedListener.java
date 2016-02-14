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
package delfos.experiment.casestudy;

/**
 * Interfaz que deben implementar todos los listener que necesiten ser
 * notificados de las modificaciones de las propiedades de un {@link CaseStudy}.
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public interface CaseStudyParameterChangedListener {

    /**
     * Método que se invoca cuando el caso de estudio sufre un cambio en alguno 
     * de sus parámetros. La clase que implemente este método será la encargada
     * de chequear qué parámetro ha sido modificado.
     * @param cs Caso de estudio que ha sido modificado
     */
    public void caseStudyParameterChanged(CaseStudy cs);
}
