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

import delfos.rs.GenericRecommenderSystem;
import delfos.rs.RecommenderSystem;
import delfos.rs.bias.PredictUserItemBias;
import delfos.rs.bufferedrecommenders.RecommenderSystem_bufferedRecommendations;
import delfos.rs.bufferedrecommenders.RecommenderSystem_cacheRecommendationModel;
import delfos.rs.bufferedrecommenders.RecommenderSystem_fixedFilePersistence;
import delfos.rs.collaborativefiltering.CollaborativeRecommender;
import delfos.rs.collaborativefiltering.Recommender_DatasetProperties;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.collaborativefiltering.knn.memorybased.nwr.KnnMemoryBasedNWR;
import delfos.rs.collaborativefiltering.knn.modelbased.KnnModelBasedCFRS;
import delfos.rs.collaborativefiltering.knn.modelbased.nwr.KnnModelBased_NWR;
import delfos.rs.collaborativefiltering.svd.SVDFoldingIn;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVD;
import delfos.rs.contentbased.ContentBasedRecommender;
import delfos.rs.contentbased.interestlms.InterestLMSPredictor;
import delfos.rs.contentbased.vsm.booleanvsm.basic.BasicBooleanCBRS;
import delfos.rs.contentbased.vsm.booleanvsm.symeonidis2007.Symeonidis2007FeatureWeighted;
import delfos.rs.contentbased.vsm.booleanvsm.tfidf.TfIdfCBRS;
import delfos.rs.contentbased.vsm.multivalued.BasicMultivaluedCBRS;
import delfos.rs.contentbased.vsm.multivalued.entropydependence.EntropyDependenceCBRS;
import delfos.rs.hybridtechniques.ContentWeightCollaborative;
import delfos.rs.hybridtechniques.HybridAlternatingListRS;
import delfos.rs.hybridtechniques.HybridRecommender;
import delfos.rs.nonpersonalised.meanrating.arithmeticmean.MeanRatingRS;
import delfos.rs.nonpersonalised.meanrating.wilsonscoreonterval.WilsonScoreLowerBound;
import delfos.rs.nonpersonalised.mostpopular.MostPopularRS;
import delfos.rs.nonpersonalised.positiveminusnegativerating.PositiveMinusNegativeRating;
import delfos.rs.nonpersonalised.positiveratingspercent.PositiveRatingPercent;
import delfos.rs.nonpersonalised.randomrecommender.RandomRecommender;
import delfos.rs.trustbased.HybridUserItemTrustBased;
import delfos.rs.trustbased.similaritymodification.TrustModificationKnnMemory;
import java.util.ArrayList;
import java.util.List;

/**
 * Factoría de sistemas de recomendación. Conoce todos los sistemas de
 * recomendación de esta biblioteca y es capaz de recuperar por nombre o tipo
 * cualquiera de ellos.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 (1 de Marzo de 2013)
 * @version 2.0 9-Mayo-2013 Ahora la clase hereda de {@link Factory}.
 */
public class RecommenderSystemsFactory extends Factory<GenericRecommenderSystem> {

    protected static final RecommenderSystemsFactory instance;

    public static RecommenderSystemsFactory getInstance() {
        return instance;
    }

    /**
     * Inicialización de la factoría, asignando los sistemas de recomendación
     * que la biblioteca esta biblioteca incorpora por defecto.
     */
    static {
        instance = new RecommenderSystemsFactory();

        instance.addClass(EntropyDependenceCBRS.class);

        //Sistemas de test
        instance.addClass(Recommender_DatasetProperties.class);
        instance.addClass(RandomRecommender.class);

        //Sistemas no personalizados
        instance.addClass(MostPopularRS.class);
        instance.addClass(MeanRatingRS.class);
        instance.addClass(PositiveMinusNegativeRating.class);
        instance.addClass(PositiveRatingPercent.class);
        instance.addClass(WilsonScoreLowerBound.class);

        //Sistemas colaborativos
        instance.addClass(KnnMemoryBasedCFRS.class);
        instance.addClass(KnnModelBasedCFRS.class);
        instance.addClass(TryThisAtHomeSVD.class);

        instance.addClass(KnnMemoryBasedNWR.class);
        //instance.addClass_oldName(KnnMemoryBasedNWR.class, "KnnMemoryBasedCFRS_NeighborsWithRatings");

        instance.addClass(KnnModelBased_NWR.class);
        //instance.addClass_oldName(KnnModelBased_NWR.class, "KnnModelBasedCFRS_NeighborsWithRatings");

        //Sistemas basados en contenido
        instance.addClass(BasicBooleanCBRS.class);
        instance.addClass(TfIdfCBRS.class);
        instance.addClass(Symeonidis2007FeatureWeighted.class);
        instance.addClass(BasicMultivaluedCBRS.class);
        instance.addClass(HybridUserItemTrustBased.class);

        //Sistemas hibridos
        instance.addClass(ContentWeightCollaborative.class);
        instance.addClass(HybridAlternatingListRS.class);

        //DuineFrameworkAlgorithms
        instance.addClass(InterestLMSPredictor.class);

        //Sistemas especiales
        instance.addClass(RecommenderSystem_fixedFilePersistence.class);
        instance.addClass(RecommenderSystem_bufferedRecommendations.class);
        instance.addClass(RecommenderSystem_cacheRecommendationModel.class);

        //Sistemas incrementales
        instance.addClass(SVDFoldingIn.class);

        //Sistemas basados en confianza
        instance.addClass(TrustModificationKnnMemory.class);

        //RS completos, cobertura = 1.
        instance.addClass(PredictUserItemBias.class);
    }

    protected RecommenderSystemsFactory() {
    }

    /**
     * 1
     * Devuelve los sistemas de recomendación que no son colaborativos, es
     * decir, los sistemas de recomendación que no usan predicción de
     * valoraciones.
     *
     * @return Lista con una instancia de cada sistema de recomendación no
     * predictivo.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<RecommenderSystem<Object>> getNotCollaborativeRecommenderSystems() {
        ArrayList<RecommenderSystem<Object>> ret = new ArrayList<>();
        allClasses.values().stream().filter((c) -> (!CollaborativeRecommender.class.isAssignableFrom(c) && RecommenderSystem.class.isAssignableFrom(c))).forEach((c) -> {
            try {
                RecommenderSystem recommender = (RecommenderSystem) c.newInstance();
                ret.add(recommender);
            } catch (IllegalAccessException | InstantiationException ex) {
                exceptionInCreation(c, ex);
            }
        });
        return ret;
    }

    /**
     * Obtiene una lista de sistemas de recomendación que conoce la factoría.
     *
     * @return Lista de sistemas recomendación colaborativos
     */
    public List<CollaborativeRecommender> getAllCollaborativeFilteringRecommender() {
        ArrayList<CollaborativeRecommender> ret = new ArrayList<>();
        for (Class<? extends GenericRecommenderSystem> c : allClasses.values()) {
            try {
                if (CollaborativeRecommender.class.isAssignableFrom(c) && RecommenderSystem.class.isAssignableFrom(c)) {
                    RecommenderSystem recommender = (RecommenderSystem) c.newInstance();
                    ret.add((CollaborativeRecommender) recommender);
                }
            } catch (IllegalAccessException | InstantiationException ex) {
                exceptionInCreation(c, ex);
            }
        }
        return ret;
    }

    /**
     * Obtiene una lista de sistemas de recomendación basados en contenido que
     * conoce la factoría.
     *
     * @return Lista de sistemas recomendación basados en contenido
     */
    public List<ContentBasedRecommender> getAllContentBasedRecommender() {
        ArrayList<ContentBasedRecommender> ret = new ArrayList<>();
        for (Class<? extends GenericRecommenderSystem> c : allClasses.values()) {
            try {
                if (ContentBasedRecommender.class.isAssignableFrom(c) && RecommenderSystem.class.isAssignableFrom(c)) {
                    RecommenderSystem recommender = (RecommenderSystem) c.newInstance();
                    ret.add((ContentBasedRecommender) recommender);
                }
            } catch (IllegalAccessException | InstantiationException ex) {
                exceptionInCreation(c, ex);
            }
        }
        return ret;
    }

    /**
     * Devuelve los sistemas de recomendación basados en conocimiento puros, es
     * decir, los sistemas de recomendación basados en conocimiento que no se
     * componen de otros sistemas de recomendación.
     *
     * @return Lista de sistemas de recomendación basados en conocimiento
     */
    public List<RecommenderSystem> getAllPureKnowledgeBasedRecommender() {
        ArrayList<RecommenderSystem> ret = new ArrayList<>();
        for (Class<? extends GenericRecommenderSystem> c : allClasses.values()) {
            try {
                if (!HybridRecommender.class.isAssignableFrom(c) && RecommenderSystem.class.isAssignableFrom(c)) {
                    RecommenderSystem recommender = (RecommenderSystem) c.newInstance();
                    ret.add(recommender);
                }
            } catch (IllegalAccessException | InstantiationException ex) {
                exceptionInCreation(c, ex);
            }
        }
        return ret;
    }

    public List<RecommenderSystem> getRecommenderSystems() {
        ArrayList<RecommenderSystem> ret = new ArrayList<>();
        for (Class<? extends GenericRecommenderSystem> c : allClasses.values()) {
            try {
                if (RecommenderSystem.class.isAssignableFrom(c)) {
                    RecommenderSystem recommender = (RecommenderSystem) c.newInstance();
                    ret.add(recommender);
                }
            } catch (IllegalAccessException | InstantiationException ex) {
                exceptionInCreation(c, ex);
            }
        }
        return ret;
    }
}
