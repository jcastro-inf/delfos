package delfos.rs.output;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.ObjectParameter;
import delfos.rs.output.sort.SortBy;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;

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
                Collections.sort(topNrecommendations);
                break;
            case SORT_BY_ID_ITEM:
                Collections.sort(
                        topNrecommendations,
                        (Recommendation o1, Recommendation o2) -> Integer.compare(o1.getIdItem(), o2.getIdItem()));
                break;
            case SORT_BY_NO_SORT:
                break;
            default:
                throw new IllegalStateException("Not implemented yet: " + sortBy);
        }

        if (getNumberOfRecommendations() > 0) {
            topNrecommendations = topNrecommendations.subList(0, Math.min(topNrecommendations.size(), getNumberOfRecommendations()));
        }

        System.out.println("Target '" + idTarget + "' recommendations:");
        for (Recommendation r : topNrecommendations) {
            System.out.println("\t" + r.getIdItem() + "," + r.getPreference());
        }
    }
}
