package delfos.rs.collaborativefiltering.knn;

import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.BooleanParameter;
import delfos.common.parameters.restriction.FloatParameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.rs.collaborativefiltering.CollaborativeRecommender;
import delfos.rs.collaborativefiltering.predictiontechniques.PredictionTechnique;
import delfos.rs.collaborativefiltering.predictiontechniques.WeightedSum;
import delfos.similaritymeasures.CollaborativeSimilarityMeasure;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;

public abstract class KnnCollaborativeRecommender<RecommendationModel>
        extends CollaborativeRecommender<RecommendationModel> {

    /**
     * Parámetro para almacenar el número de vecinos que se tienen en cuenta
     * para la predicción de la valoración. Si no se modifica, su valor por
     * defecto es 20.
     */
    public static final Parameter NEIGHBORHOOD_SIZE = new Parameter("Neighborhood_size", new IntegerParameter(1, 999999, 20));
    /**
     * Parámetro para indicar la medida de similitud que el sistema de
     * recomendación utiliza para el cálculo de los vecinos más cercanos. Si no
     * se modifica, su valor por defecto es la suma ponderada
     * ({@link CosineCoefficient})
     */
    public static final Parameter SIMILARITY_MEASURE = new Parameter(
            "SIMILARITY_MEASURE",
            new ParameterOwnerRestriction(
                    CollaborativeSimilarityMeasure.class,
                    new PearsonCorrelationCoefficient()
            )
    );
    /**
     * Parámetro para indicar la técnica de predicción que el sistema de
     * recomendación utiliza para la predicción de las valoraciones. Si no se
     * modifica, su valor por defecto es la suma ponderada ({@link WeightedSum})
     */
    public static final Parameter PREDICTION_TECHNIQUE = new Parameter(
            "Prediction_technique",
            new ParameterOwnerRestriction(PredictionTechnique.class, new WeightedSum()));
    /**
     * Parámetro que indica si se usa o no la mejora de frecuencia inversa. Por
     * defecto no se aplica la mejora de frecuencia inversa
     */
    public static final Parameter INVERSE_FREQUENCY = new Parameter("Inverse_frequency", new BooleanParameter(false));
    /**
     * Parámetro que almacena el valor de la mejora de ampliación de casos. Si
     * el valor de este parámetro es 1, no aplica cambios, ya que esta mejora
     * eleva la similitud calculada al valor de ampliación de casos
     */
    public static final Parameter CASE_AMPLIFICATION = new Parameter("Case_amplification", new FloatParameter(1.0f, 10.0f, 1.0f));
    /**
     * Indica si se imputa un valor por defecto a las valoraciones no conocidas
     * de los usuarios. Esta mejora sirve para poder calcular la similitud
     * incluso cuando las valoraciones de los usuarios no se solapan, es decir,
     * no han valorado ningún producto en común. Por defecto no se aplica esta
     * mejora.
     *
     * @see KnnMemoryBasedCFRS#defaultRatingValue
     */
    public static final Parameter DEFAULT_RATING = new Parameter("Default_rating", new BooleanParameter(false));
    /**
     * Indica el valor que se imputa a las valoraciones no conocidas en caso de
     * que se haya activado la mejora de valoración por defecto.
     *
     * @see KnnMemoryBasedCFRS#defaultRatingValue
     */
    public static final Parameter DEFAULT_RATING_VALUE = new Parameter("Default_rating_value", new IntegerParameter(1, 5, 3));
    /**
     * Parámetro que indica si se utiliza la mejora de factor de relevancia.
     * Esta mejora modifica las similitudes de manera que penaliza las
     * similitudes calculadas con un número bajo de valoraciones en común. Por
     * defecto no se utiliza esta mejora.
     *
     * @see KnnModelBasedCFRS#relevanceFactorValue
     */
    public static final Parameter RELEVANCE_FACTOR = new Parameter("Relevance_factor", new BooleanParameter(true));
    /**
     * Almacena el valor del factor de relevancia aplicado si el parámetro
     * {@link KnnModelBasedCFRS#relevanceFactor} indica que se debe usar factor
     * de relevancia (true). El valor por defecto del factor de relevancia es
     * 50.
     *
     * @see KnnModelBasedCFRS#relevanceFactor
     */
    public static final Parameter RELEVANCE_FACTOR_VALUE = new Parameter("Relevance_factor_value", new IntegerParameter(1, 9999, 20));
}
