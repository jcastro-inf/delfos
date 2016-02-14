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
package delfos.configfile.rs.single;

import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.recommendationcandidates.RecommendationCandidatesSelector;
import delfos.rs.GenericRecommenderSystem;
import delfos.rs.output.RecommendationsOutputMethod;
import delfos.rs.persistence.PersistenceMethod;

/**
 * Class used to store the information of a Recommender system. Includes the
 * algorithm, dataset and all the relevant configuration.
 *
 * @see
 * RecommenderSystemConfigurationFileParser#loadConfigFile(java.lang.String)
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
