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

import delfos.results.evaluationmeasures.Coverage;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.results.evaluationmeasures.NumberOfRecommendations;
import delfos.results.evaluationmeasures.ndcg.NDCG;
import delfos.results.evaluationmeasures.ndcg.NDCG_01;
import delfos.results.evaluationmeasures.ndcg.NDCG_02;
import delfos.results.evaluationmeasures.ndcg.NDCG_03;
import delfos.results.evaluationmeasures.ndcg.NDCG_04;
import delfos.results.evaluationmeasures.ndcg.NDCG_05;
import delfos.results.evaluationmeasures.ndcg.NDCG_06;
import delfos.results.evaluationmeasures.ndcg.NDCG_07;
import delfos.results.evaluationmeasures.ndcg.NDCG_08;
import delfos.results.evaluationmeasures.ndcg.NDCG_09;
import delfos.results.evaluationmeasures.ndcg.NDCG_10;
import delfos.results.evaluationmeasures.prediction.PredicitonErrorHistogram;
import delfos.results.evaluationmeasures.prediction.list.HalfLifeUtility;
import delfos.results.evaluationmeasures.prspace.PRSpace;
import delfos.results.evaluationmeasures.prspace.precision.Precision_01;
import delfos.results.evaluationmeasures.prspace.precision.Precision_02;
import delfos.results.evaluationmeasures.prspace.precision.Precision_03;
import delfos.results.evaluationmeasures.prspace.precision.Precision_04;
import delfos.results.evaluationmeasures.prspace.precision.Precision_05;
import delfos.results.evaluationmeasures.prspace.precision.Precision_06;
import delfos.results.evaluationmeasures.prspace.precision.Precision_07;
import delfos.results.evaluationmeasures.prspace.precision.Precision_08;
import delfos.results.evaluationmeasures.prspace.precision.Precision_09;
import delfos.results.evaluationmeasures.prspace.precision.Precision_10;
import delfos.results.evaluationmeasures.prspace.recall.Recall_01;
import delfos.results.evaluationmeasures.prspace.recall.Recall_02;
import delfos.results.evaluationmeasures.prspace.recall.Recall_03;
import delfos.results.evaluationmeasures.prspace.recall.Recall_04;
import delfos.results.evaluationmeasures.prspace.recall.Recall_05;
import delfos.results.evaluationmeasures.prspace.recall.Recall_06;
import delfos.results.evaluationmeasures.prspace.recall.Recall_07;
import delfos.results.evaluationmeasures.prspace.recall.Recall_08;
import delfos.results.evaluationmeasures.prspace.recall.Recall_09;
import delfos.results.evaluationmeasures.prspace.recall.Recall_10;
import delfos.results.evaluationmeasures.ratingprediction.FScoreCollaborative;
import delfos.results.evaluationmeasures.ratingprediction.MAE;
import delfos.results.evaluationmeasures.ratingprediction.NMAE;
import delfos.results.evaluationmeasures.ratingprediction.NRMSE;
import delfos.results.evaluationmeasures.ratingprediction.PrecisionCollaborative;
import delfos.results.evaluationmeasures.ratingprediction.RMSE;
import delfos.results.evaluationmeasures.ratingprediction.RecallCollaborative;
import delfos.results.evaluationmeasures.roccurve.AreaUnderROC;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Clase que permite recuperar todas las medidas de evaluación que la biblioteca conoce.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 * @version 2.0 9-Mayo-2013 Ahora la clase hereda de {@link Factory}.
 */
public class EvaluationMeasuresFactory extends Factory<EvaluationMeasure> {

    private static final EvaluationMeasuresFactory instance;

    public static EvaluationMeasuresFactory getInstance() {
        return instance;
    }

    static {
        instance = new EvaluationMeasuresFactory();
        instance.addClass(AreaUnderROC.class);
        instance.addClass(Coverage.class);
        instance.addClass(NumberOfRecommendations.class);
        instance.addClass(PRSpace.class);
        instance.addClass(MAE.class);
        instance.addClass(RMSE.class);
        instance.addClass(FScoreCollaborative.class);
        instance.addClass(PrecisionCollaborative.class);
        instance.addClass(RecallCollaborative.class);
        instance.addClass(NDCG.class);

        instance.addClass(NMAE.class);
        instance.addClass(NRMSE.class);

        instance.addClass(HalfLifeUtility.class);
        instance.addClass(PredicitonErrorHistogram.class);

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

        instance.addClass(Recall_01.class);
        instance.addClass(Recall_02.class);
        instance.addClass(Recall_03.class);
        instance.addClass(Recall_04.class);
        instance.addClass(Recall_05.class);
        instance.addClass(Recall_06.class);
        instance.addClass(Recall_07.class);
        instance.addClass(Recall_08.class);
        instance.addClass(Recall_09.class);
        instance.addClass(Recall_10.class);

        instance.addClass(NDCG_01.class);
        instance.addClass(NDCG_02.class);
        instance.addClass(NDCG_03.class);
        instance.addClass(NDCG_04.class);
        instance.addClass(NDCG_05.class);
        instance.addClass(NDCG_06.class);
        instance.addClass(NDCG_07.class);
        instance.addClass(NDCG_08.class);
        instance.addClass(NDCG_09.class);
        instance.addClass(NDCG_10.class);

    }

    private EvaluationMeasuresFactory() {
    }

    /**
     * Devuelve todas las medidas de evaluación que no necesitan que los valoraciones de preferencia indicados para las
     * recomendaciones sean predicciones de valoraciones.
     *
     * @return Colección de medidas de evaluación.
     */
    public Collection<EvaluationMeasure> getAllContentBasedEvaluationMeasures() {
        List<EvaluationMeasure> ret = getAllClasses();

        for (Iterator<EvaluationMeasure> it = ret.iterator(); it.hasNext();) {
            EvaluationMeasure evaluationMeasure = it.next();
            if (evaluationMeasure.usesRatingPrediction()) {
                it.remove();
            }
        }
        return ret;
    }
}
