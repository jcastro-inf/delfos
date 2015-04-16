package delfos.group.grs.cww.centrality;

import delfos.factories.Factory;
import delfos.group.grs.cww.centrality.definitions.AritmethicMeanConnectionWeightCentrality;
import delfos.group.grs.cww.centrality.definitions.ClosenessCentrality;
import delfos.group.grs.cww.centrality.definitions.GeometricMeanConnectionWeightCentrality;

/**
 *
* @author Jorge Castro Gallardo
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
