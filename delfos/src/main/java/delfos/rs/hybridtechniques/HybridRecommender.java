package delfos.rs.hybridtechniques;

import java.util.List;
import delfos.rs.RecommenderSystem;
import delfos.rs.RecommenderSystemAdapter;

/**
 * Interfaz que define la funcionalidad que debe ofrecer un sistema de
 * recomendación híbrido, es decir, que combina el funcionamiento de varios
 * sistemas de recomendación básicos.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.1 21-Jan-2013
 * @version 1.0 Unknow date
 * @param <RecommenderSystemModel>
 */
public abstract class HybridRecommender<RecommenderSystemModel> extends RecommenderSystemAdapter<RecommenderSystemModel> {

    /**
     * Devuelve los sistemas de recomendación que hibrida este sistema.
     *
     * @return Lista de sistemas de recomendación hibridados
     */
    protected abstract List<RecommenderSystem<Object>> getHybridizedRecommenderSystems();

    @Override
    public final boolean isRatingPredictorRS() {
        return false;
    }
}
