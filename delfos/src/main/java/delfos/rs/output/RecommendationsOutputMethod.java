package delfos.rs.output;

import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.rs.recommendation.Recommendations;

/**
 * Interfaz que sirve para definir la semántica de un método de salida de las
 * recomendaciones.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 28-oct-2013
 * @version 1.1 15-Noviembre-2013
 */
public abstract class RecommendationsOutputMethod extends ParameterOwnerAdapter {

    /**
     * Regula el tamaño de la lista de recomendaciones
     */
    public static final Parameter NUMBER_OF_RECOMMENDATIONS = new Parameter("NUMBER_OF_RECOMMENDATIONS", new IntegerParameter(-1, 1000000, -1), "Size of recommendation list returned by this library");

    /**
     * Escribe las recomendaciones indicadas por parámetro.
     *
     * @param recommendations Recomendaciones a escribir
     */
    public abstract void writeRecommendations(Recommendations recommendations);

    public RecommendationsOutputMethod() {
        super();
        addParameter(NUMBER_OF_RECOMMENDATIONS);
    }

    public int getNumberOfRecommendations() {
        Integer numberOfRecommendations = (Integer) getParameterValue(NUMBER_OF_RECOMMENDATIONS);
        return numberOfRecommendations;
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.RECOMMENDATIONS_OUTPUT_METHOD;
    }
}
