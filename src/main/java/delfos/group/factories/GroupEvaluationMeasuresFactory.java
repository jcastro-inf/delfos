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
import delfos.group.results.groupevaluationmeasures.AreaUnderRoc;
import delfos.group.results.groupevaluationmeasures.Coverage;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.MAE;
import delfos.group.results.groupevaluationmeasures.NDCG;
import delfos.group.results.groupevaluationmeasures.NMAE;
import delfos.group.results.groupevaluationmeasures.NRMSE;
import delfos.group.results.groupevaluationmeasures.NumberOfRecommendations;
import delfos.group.results.groupevaluationmeasures.NumberOfRequests;
import delfos.group.results.groupevaluationmeasures.RMSE;
import delfos.group.results.groupevaluationmeasures.diversity.ils.IntraListSimilarity;
import delfos.group.results.groupevaluationmeasures.diversity.ils.IntraListSimilarity_02;
import delfos.group.results.groupevaluationmeasures.diversity.ils.IntraListSimilarity_03;
import delfos.group.results.groupevaluationmeasures.diversity.ils.IntraListSimilarity_04;
import delfos.group.results.groupevaluationmeasures.diversity.ils.IntraListSimilarity_05;
import delfos.group.results.groupevaluationmeasures.diversity.ils.IntraListSimilarity_06;
import delfos.group.results.groupevaluationmeasures.diversity.ils.IntraListSimilarity_07;
import delfos.group.results.groupevaluationmeasures.diversity.ils.IntraListSimilarity_08;
import delfos.group.results.groupevaluationmeasures.diversity.ils.IntraListSimilarity_09;
import delfos.group.results.groupevaluationmeasures.diversity.ils.IntraListSimilarity_10;
import delfos.group.results.groupevaluationmeasures.precisionrecall.PRSpaceGroups;
import delfos.group.results.groupevaluationmeasures.precisionrecall.Precision_01;
import delfos.group.results.groupevaluationmeasures.precisionrecall.Precision_02;
import delfos.group.results.groupevaluationmeasures.precisionrecall.Precision_03;
import delfos.group.results.groupevaluationmeasures.precisionrecall.Precision_04;
import delfos.group.results.groupevaluationmeasures.precisionrecall.Precision_05;
import delfos.group.results.groupevaluationmeasures.precisionrecall.Precision_06;
import delfos.group.results.groupevaluationmeasures.precisionrecall.Precision_07;
import delfos.group.results.groupevaluationmeasures.precisionrecall.Precision_08;
import delfos.group.results.groupevaluationmeasures.precisionrecall.Precision_09;
import delfos.group.results.groupevaluationmeasures.precisionrecall.Precision_10;
import delfos.group.results.groupevaluationmeasures.printers.PrintGroupRatingsToPlainText;
import delfos.group.results.groupevaluationmeasures.printers.PrintGroups;
import delfos.group.results.groupevaluationmeasures.printers.PrintTestSet;
import delfos.group.results.groupevaluationmeasures.printers.neighbor.PrintNeighborsToXML;
import delfos.group.results.groupevaluationmeasures.time.GroupModelBuildTime;
import delfos.group.results.groupevaluationmeasures.time.GroupRecommendationTime;
import delfos.group.results.groupevaluationmeasures.time.ModelBuildTime;

/**
 * Clase que conoce todas las medidas de evaluación a grupos y permite
 * recuperarlas para su uso en los casos de estudio.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.1 26-Jan-2013
 */
public class GroupEvaluationMeasuresFactory extends Factory<GroupEvaluationMeasure> {

    private final static GroupEvaluationMeasuresFactory instance;

    static {
        instance = new GroupEvaluationMeasuresFactory();

        //Medidas relacionadas con la cobertura.
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

        //Diversidad
        instance.addClass(IntraListSimilarity.class);
        instance.addClass(IntraListSimilarity_02.class);
        instance.addClass(IntraListSimilarity_03.class);
        instance.addClass(IntraListSimilarity_04.class);
        instance.addClass(IntraListSimilarity_05.class);
        instance.addClass(IntraListSimilarity_06.class);
        instance.addClass(IntraListSimilarity_07.class);
        instance.addClass(IntraListSimilarity_08.class);
        instance.addClass(IntraListSimilarity_09.class);
        instance.addClass(IntraListSimilarity_10.class);

        //Precision
        instance.addClass(IntraListSimilarity.class);
        instance.addClass(Precision_01.class);
        instance.addClass(Precision_02.class);
        instance.addClass(Precision_03.class);
        instance.addClass(Precision_04.class);
        instance.addClass(Precision_05.class);
        instance.addClass(Precision_06.class);
        instance.addClass(Precision_07.class);
        instance.addClass(Precision_08.class);
        instance.addClass(Precision_09.class);
        instance.addClass(Precision_10.class);

        //Time measures
        instance.addClass(ModelBuildTime.class);
        instance.addClass(GroupRecommendationTime.class);
        instance.addClass(GroupModelBuildTime.class);
    }

    private GroupEvaluationMeasuresFactory() {
    }

    public static GroupEvaluationMeasuresFactory getInstance() {
        return instance;
    }
}
