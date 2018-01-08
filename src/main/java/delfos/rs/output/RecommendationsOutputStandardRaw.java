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
package delfos.rs.output;

import delfos.common.Global;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.ObjectParameter;
import delfos.rs.output.sort.SortBy;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Escribe las recomendaciones en la salida estándar.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 28-Octubre-2013
 */
public class RecommendationsOutputStandardRaw extends RecommendationsOutputMethod {

    private static final long serialVersionUID = 1L;

    public static final DecimalFormat format = new DecimalFormat("0.000");
    public static final Parameter SORT_BY = new Parameter("SORT_BY", new ObjectParameter(SortBy.values(), SortBy.SORT_BY_NO_SORT));

    /**
     * Constructor por defecto, que añade los parámetros de este método de salida de recomendaciones.
     */
    public RecommendationsOutputStandardRaw() {
        super();
        addParameter(SORT_BY);
    }

    public RecommendationsOutputStandardRaw(SortBy sortBy) {
        this();
        setParameterValue(SORT_BY, sortBy);
    }

    public RecommendationsOutputStandardRaw(int numberOfRecommendations) {
        this();
        setParameterValue(NUMBER_OF_RECOMMENDATIONS, numberOfRecommendations);
        setParameterValue(SORT_BY, SortBy.SORT_BY_PREFERENCE);
    }

    @Override
    public void writeRecommendations(Recommendations recommendations) {
        String idTarget = recommendations.getTargetIdentifier();

        List<Recommendation> recommendationsSorted = new ArrayList<>(recommendations.getRecommendations());

        SortBy sortBy = (SortBy) getParameterValue(SORT_BY);

        switch (sortBy) {
            case SORT_BY_PREFERENCE:
                Collections.sort(recommendationsSorted, Recommendation.BY_PREFERENCE_DESC);
                break;
            case SORT_BY_ID_ITEM:
                Collections.sort(recommendationsSorted, Recommendation.BY_ID);
                break;
            case SORT_BY_NO_SORT:
                break;
            default:
                throw new IllegalStateException("Not implemented yet: " + sortBy);
        }

        List<Recommendation> topRecommendations = getNumberOfRecommendations() <= 0
                ? recommendationsSorted
                : recommendationsSorted.stream().limit(getNumberOfRecommendations()).collect(Collectors.toList());

        Global.showln("Target '" + idTarget + "' recommendations:");
        for (Recommendation r : topRecommendations) {
            String prediction;

            if (Double.isFinite(r.getPreference().doubleValue())) {
                prediction = format.format(r.getPreference().doubleValue());
            } else {
                prediction = "NaN";
            }
            Global.showln("\t" + r.getItem().getId() + "," + r.getItem().getName() + "," + prediction);
        }
    }

    public RecommendationsOutputStandardRaw setShowSortedByPreference(){
        setParameterValue(SORT_BY,SortBy.SORT_BY_PREFERENCE);

        return this;
    }

    public RecommendationsOutputStandardRaw setTopN(int n){
        setParameterValue(NUMBER_OF_RECOMMENDATIONS, n);

        return this;
    }

    public RecommendationsOutputStandardRaw unsetTopN(){
        setParameterValue(NUMBER_OF_RECOMMENDATIONS,0);
        return this;
    }
}

