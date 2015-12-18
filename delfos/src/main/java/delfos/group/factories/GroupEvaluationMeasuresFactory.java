package delfos.group.factories;

import delfos.factories.Factory;
import delfos.group.results.groupevaluationmeasures.AreaUnderRoc;
import delfos.group.results.groupevaluationmeasures.Coverage;
import delfos.group.results.groupevaluationmeasures.GroupAverageNumberOfRecommendations;
import delfos.group.results.groupevaluationmeasures.GroupAverageNumberOfRequests;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.printers.PrintTestSet;
import delfos.group.results.groupevaluationmeasures.MAE;
import delfos.group.results.groupevaluationmeasures.NDCG;
import delfos.group.results.groupevaluationmeasures.NMAE;
import delfos.group.results.groupevaluationmeasures.NRMSE;
import delfos.group.results.groupevaluationmeasures.NumberOfRecommendations;
import delfos.group.results.groupevaluationmeasures.NumberOfRequests;
import delfos.group.results.groupevaluationmeasures.printers.PrintGroupRatingsToPlainText;
import delfos.group.results.groupevaluationmeasures.printers.PrintGroups;
import delfos.group.results.groupevaluationmeasures.printers.PrintNeighborsToXML;
import delfos.group.results.groupevaluationmeasures.RMSE;
import delfos.group.results.groupevaluationmeasures.precisionrecall.PRSpaceGroups;

/**
 * Clase que conoce todas las medidas de evaluación a grupos y permite
 * recuperarlas para su uso en los casos de estudio.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.1 26-Jan-2013
 */
public class GroupEvaluationMeasuresFactory extends Factory<GroupEvaluationMeasure> {

    private final static GroupEvaluationMeasuresFactory instance;

    static {
        instance = new GroupEvaluationMeasuresFactory();

        //Medidas relacionadas con la cobertura.
        instance.addClass(GroupAverageNumberOfRecommendations.class);
        instance.addClass(GroupAverageNumberOfRequests.class);
        instance.addClass(Coverage.class);
        instance.addClass(NumberOfRecommendations.class);
        instance.addClass(NumberOfRequests.class);

        //Grupos evaluados
        instance.addClass(PrintGroups.class);
        instance.addClass(PrintNeighborsToXML.class);
        instance.addClass(PrintGroupRatingsToPlainText.class);

        //Decision making measures
        instance.addClass(AreaUnderRoc.class);
        instance.addClass(PRSpaceGroups.class);

        //Error measures
        instance.addClass(MAE.class);
        instance.addClass(RMSE.class);
        instance.addClass(NMAE.class);
        instance.addClass(NRMSE.class);

        //TODO: Rank measures
        instance.addClass(NDCG.class);

        //Histogram measures
        instance.addClass(PrintTestSet.class);
    }

    private GroupEvaluationMeasuresFactory() {
    }

    public static GroupEvaluationMeasuresFactory getInstance() {
        return instance;
    }
}
