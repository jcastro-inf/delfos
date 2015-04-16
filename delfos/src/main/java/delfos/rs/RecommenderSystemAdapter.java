package delfos.rs;

import delfos.common.parameters.ParameterOwnerType;
import delfos.rs.contentbased.ContentBasedRecommender;

/**
 * Interfaz que establece los métodos necesarios para implementar un sistema de
 * recomendación genérico
 * <p>
 * <b>Nota:</b> Los sistemas de recomendación basados en contenido deben heredar
 * de la clase <code>{@link ContentBasedRecommender}</code>.
 *
 *
 * @param <RecommenderSystemModel> Clase que almacena el modelo de recomendación
 * del sistema.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * @version 1.0 (19 Octubre 2011)
 * @version 2.0 26-Mayo-2013 Ahora los datasets se pasan por parámetro en cada
 * método.
 */
public abstract class RecommenderSystemAdapter<RecommenderSystemModel>
        extends GenericRecommenderSystemAdapter<RecommenderSystemModel>
        implements RecommenderSystem<RecommenderSystemModel> {

    /**
     * Constructor por defecto de un sistema de recomendación a usuarios
     * individuales
     */
    protected RecommenderSystemAdapter() {
        super();
    }

    @Override
    public final ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.RECOMMENDER_SYSTEM;
    }
}
