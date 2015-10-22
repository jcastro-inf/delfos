package delfos.rs.output;

import delfos.common.Global;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.ObjectParameter;
import delfos.rs.output.sort.SortBy;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Escribe las recomendaciones en la salida estándar.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 28-Octubre-2013
 */
public class RecommendationsOutputStandardRaw extends RecommendationsOutputMethod {

    private static final long serialVersionUID = 1L;

    public static final Parameter SORT_BY = new Parameter("SORT_BY", new ObjectParameter(SortBy.values(), SortBy.SORT_BY_NO_SORT));

    /**
     * Constructor por defecto, que añade los parámetros de este método de
     * salida de recomendaciones.
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

        List<Recommendation> topNrecommendations = new ArrayList<>(recommendations.getRecommendations());

        SortBy sortBy = (SortBy) getParameterValue(SORT_BY);

        switch (sortBy) {
            case SORT_BY_PREFERENCE:
                Collections.sort(topNrecommendations, Recommendation.BY_PREFERENCE_DESC);
                break;
            case SORT_BY_ID_ITEM:
                Collections.sort(topNrecommendations, Recommendation.BY_ID);
                break;
            case SORT_BY_NO_SORT:
                break;
            default:
                throw new IllegalStateException("Not implemented yet: " + sortBy);
        }

        if (getNumberOfRecommendations() > 0) {
            topNrecommendations = topNrecommendations.subList(0, Math.min(topNrecommendations.size(), getNumberOfRecommendations()));
        }

        Global.showln("Target '" + idTarget + "' recommendations:");
        for (Recommendation r : topNrecommendations) {
            Global.showln("\t" + r.getItem() + "," + r.getItem().getName() + "," + r.getPreference());
        }
    }
}
