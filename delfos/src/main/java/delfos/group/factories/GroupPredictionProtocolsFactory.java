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
package delfos.group.factories;

import delfos.factories.Factory;
import delfos.group.experiment.validation.predictionvalidation.AllButOne;
import delfos.group.experiment.validation.predictionvalidation.CrossFoldPredictionProtocol;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.experiment.validation.predictionvalidation.HoldOutPrediction;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;

/**
 * Conoce las técnicas de generación de grupos que se utilizarán en los casos de
 * estudio para evaluar sistemas de recomendación a grupos.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 16-Jan-2013
 * @version 9-Enero-2013 Adaptado a la nueva declaración de las factorías.
 */
public class GroupPredictionProtocolsFactory extends Factory<GroupPredictionProtocol> {

    protected static final GroupPredictionProtocolsFactory instance;

    public static GroupPredictionProtocolsFactory getInstance() {
        return instance;
    }

    static {
        instance = new GroupPredictionProtocolsFactory();

        instance.addClass(AllButOne.class);
        instance.addClass(CrossFoldPredictionProtocol.class);
        instance.addClass(HoldOutPrediction.class);
        instance.addClass(NoPredictionProtocol.class);
    }

    protected GroupPredictionProtocolsFactory() {
    }
}
