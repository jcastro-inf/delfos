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

import delfos.experiment.validation.predictionprotocol.AllButOne;
import delfos.experiment.validation.predictionprotocol.GivenN;
import delfos.experiment.validation.predictionprotocol.NoPredictionProtocol;
import delfos.experiment.validation.predictionprotocol.PredictN;
import delfos.experiment.validation.predictionprotocol.PredictionProtocol;

/**
 * Clase que implementa el patrón factoría para las validaciones de predicciones
 * para algoritmos colaborativos . Permite ver las técnicas que hay
 * implementadas, obtener una técnica concreta.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.1 21-Jan-2013
 * @version 1.0 Unknow date
 * @version 2.0 9-Mayo-2013 Ahora la clase hereda de {@link Factory}.
 */
public class PredictionProtocolFactory extends Factory<PredictionProtocol> {

    private static final PredictionProtocolFactory instance;

    public static PredictionProtocolFactory getInstance() {
        return instance;
    }

    static {
        instance = new PredictionProtocolFactory();
        instance.addClass(NoPredictionProtocol.class);
        instance.addClass(GivenN.class);
        instance.addClass(PredictN.class);
        instance.addClass(AllButOne.class);
    }

    private PredictionProtocolFactory() {
    }
}
