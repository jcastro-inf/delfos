package delfos.configfile.rs.single;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.recommendationcandidates.RecommendationCandidatesSelector;
import delfos.rs.GenericRecommenderSystem;
import delfos.rs.output.RecommendationsOutputMethod;
import delfos.rs.persistence.PersistenceMethod;

/**
 * Clase utilizada para devolver los valores del método {@link ConfigFile#parseFile(java.lang.String)
 * }.
 *
 * @see ConfigFile#parseFile(java.lang.String)
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 26-Feb-2013
 */
public class RecommenderSystemConfiguration {

    /**
     * Sistema de recomendación recuperado del fichero de configuración.
     */
    public final GenericRecommenderSystem<? extends Object> recommenderSystem;
    /**
     * Cargador de dataset recuperado del fichero de configuración.
     */
    public final DatasetLoader<? extends Rating> datasetLoader;
    /**
     * Método de persistencia que se utiliza para almacenar/recuperar el modelo
     * del sistema de recomendación.
     */
    public final PersistenceMethod persistenceMethod;

    /**
     * Método por el que se devuelven las recomendaciones calculadas.
     */
    public final RecommendationsOutputMethod recommdendationsOutputMethod;

    /**
     * Técnica que se usa para comprobar qué productos son recomendables al
     * usuario.
     */
    public final RecommendationCandidatesSelector recommendationCandidatesSelector;

    /**
     * Criterio de relevancia que utiliza el sistema de recomendación.
     */
    public final RelevanceCriteria relevanceCriteria;

    /**
     * Constructor de la estructura.
     *
     * @param recommenderSystem Sistema de recomendación.
     * @param datasetLoader Cargador de dataset.
     * @param persistenceMethod Define cómo se escribe el modelo
     * @param recommendationCandidatesSelector Define cómo se eligen los
     * productos candidatos
     * @param recommdendationsOutputMethod Define cómo se escriben las
     * recomendaciones.
     * @param relevanceCriteria
     */
    public RecommenderSystemConfiguration(
            GenericRecommenderSystem<? extends Object> recommenderSystem,
            DatasetLoader<? extends Rating> datasetLoader,
            PersistenceMethod persistenceMethod,
            RecommendationCandidatesSelector recommendationCandidatesSelector,
            RecommendationsOutputMethod recommdendationsOutputMethod,
            RelevanceCriteria relevanceCriteria) {
        this.recommenderSystem = recommenderSystem;
        this.datasetLoader = datasetLoader;
        this.persistenceMethod = persistenceMethod;
        this.recommendationCandidatesSelector = recommendationCandidatesSelector;
        this.recommdendationsOutputMethod = recommdendationsOutputMethod;
        this.relevanceCriteria = relevanceCriteria;
    }
}
