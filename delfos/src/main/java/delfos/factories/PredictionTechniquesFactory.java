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

import delfos.rs.collaborativefiltering.predictiontechniques.ItemAverageAdjustment;
import delfos.rs.collaborativefiltering.predictiontechniques.PredictionTechnique;
import delfos.rs.collaborativefiltering.predictiontechniques.WeightedSum;

/**
 * Clase que almacena las clases que implementan una técnica de predicción.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 (18-02-2013) Restringidas las clases que se indican a clases que
 * extienden {@link PredictionTechnique}
 * @version 2.0 9-Mayo-2013 Ahora la clase hereda de {@link Factory}.
 */
public class PredictionTechniquesFactory extends Factory<PredictionTechnique> {

    private static final PredictionTechniquesFactory instance;

    static {
        instance = new PredictionTechniquesFactory();
        instance.addClass(WeightedSum.class);
        instance.addClass(ItemAverageAdjustment.class);
    }

    private PredictionTechniquesFactory() {
    }

    public static PredictionTechniquesFactory getInstance() {
        return instance;
    }
}
