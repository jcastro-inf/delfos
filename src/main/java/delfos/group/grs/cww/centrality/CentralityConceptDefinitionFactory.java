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
package delfos.group.grs.cww.centrality;

import delfos.factories.Factory;
import delfos.group.grs.cww.centrality.definitions.AritmethicMeanConnectionWeightCentrality;
import delfos.group.grs.cww.centrality.definitions.ClosenessCentrality;
import delfos.group.grs.cww.centrality.definitions.GeometricMeanConnectionWeightCentrality;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 04-mar-2014
 */
public class CentralityConceptDefinitionFactory extends Factory<CentralityConceptDefinition> {

    private static final CentralityConceptDefinitionFactory instance;

    static {
        instance = new CentralityConceptDefinitionFactory();

        instance.addClass(AritmethicMeanConnectionWeightCentrality.class);
        instance.addClass(ClosenessCentrality.class);
        instance.addClass(GeometricMeanConnectionWeightCentrality.class);
//        instance.addClass(BetweennessCentrality.class);
//        instance.addClass(EigenvectorCentrality.class);
//        instance.addClass(KatzCentralityAndPageRank.class);
    }

    private CentralityConceptDefinitionFactory() {
    }

    public static final CentralityConceptDefinitionFactory getInstance() {
        return instance;
    }

}
