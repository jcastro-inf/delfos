package delfos.rs.hybridtechniques;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import delfos.common.aggregationoperators.AggregationOperator;
import delfos.common.aggregationoperators.Mean;
import delfos.common.aggregationoperators.TwoValuesAggregator;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.common.parameters.restriction.RecommenderSystemParameterRestriction;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.rs.RecommenderSystem;
import delfos.rs.RecommendationModelBuildingProgressListener;
import delfos.rs.collaborativefiltering.CollaborativeRecommender;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.contentbased.ContentBasedRecommender;
import delfos.rs.contentbased.vsm.booleanvsm.tfidf.TfIdfCBRS;
import delfos.rs.recommendation.Recommendation;

/**
 * Sistema de recomendación híbrido que utiliza como fuente un sistema de
 * recomendación colaborativo y otro basado en contenido.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.1 21-Jan-2013
 * @version 1.0 Unknow date
 */
public class ContentWeightCollaborative extends HybridRecommender<HybridRecommendationModel> {

    private static final long serialVersionUID = -3387516993124229948L;

    public static final Parameter AGGREGATION_OPERATOR = new Parameter(
            "AGGREGATION_OPERATOR",
            new ParameterOwnerRestriction(AggregationOperator.class, new Mean()));

    static {
        @SuppressWarnings({"rawtypes", "unchecked"})
        Class<? extends RecommenderSystem>[] values = (Class<? extends RecommenderSystem>[]) new Class[1];
        values[0] = CollaborativeRecommender.class;
        COLLABORATIVE_TECHNIQUE = new Parameter("Collaborative_technique", new RecommenderSystemParameterRestriction(new KnnMemoryBasedCFRS(), values));

        values = (Class<? extends RecommenderSystem<Object>>[]) new Class[1];
        values[0] = ContentBasedRecommender.class;
        CONTENT_BASED_TECHNIQUE = new Parameter("Content_based_technique", new RecommenderSystemParameterRestriction(new TfIdfCBRS(), values));
    }
    public static final Parameter COLLABORATIVE_TECHNIQUE;
    public static final Parameter CONTENT_BASED_TECHNIQUE;

    public ContentWeightCollaborative() {
        addParameter(AGGREGATION_OPERATOR);
        addParameter(COLLABORATIVE_TECHNIQUE);
        addParameter(CONTENT_BASED_TECHNIQUE);

    }

    @Override
    public HybridRecommendationModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset {
        ContentBasedRecommender<Object, Object> contentBasedAlgorithm = (ContentBasedRecommender<Object, Object>) getParameterValue(CONTENT_BASED_TECHNIQUE);

        RecommendationModelBuildingProgressListener contentBasedListener = (String actualJob, int percent, long remainingSeconds) -> {
            ContentWeightCollaborative.this.fireBuildingProgressChangedEvent(actualJob, percent / 2, -1);
        };
        contentBasedAlgorithm.addRecommendationModelBuildingProgressListener(contentBasedListener);
        Object contentBasedModel = contentBasedAlgorithm.buildRecommendationModel(datasetLoader);

        CollaborativeRecommender collaborativeFilteringTechnique = (CollaborativeRecommender) getParameterValue(COLLABORATIVE_TECHNIQUE);
        RecommendationModelBuildingProgressListener collaborativeListener = (String actualJob, int percent, long remainingSeconds) -> {
            ContentWeightCollaborative.this.fireBuildingProgressChangedEvent(actualJob, percent / 2 + 50, -1);
        };
        collaborativeFilteringTechnique.addRecommendationModelBuildingProgressListener(collaborativeListener);
        Object collaborativeModel = collaborativeFilteringTechnique.buildRecommendationModel(datasetLoader);

        return new HybridRecommendationModel(contentBasedModel, collaborativeModel);
    }

    @Override
    public Collection<Recommendation> recommendToUser(DatasetLoader<? extends Rating> datasetLoader, HybridRecommendationModel model, Integer idUser, java.util.Set<Integer> candidateItems) throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, ItemNotFound, NotEnoughtUserInformation {

        ContentBasedRecommender<Object, Object> contentBasedAlgorithm = (ContentBasedRecommender<Object, Object>) getParameterValue(CONTENT_BASED_TECHNIQUE);
        CollaborativeRecommender<Object> collaborativeFilteringTechnique = (CollaborativeRecommender<Object>) getParameterValue(COLLABORATIVE_TECHNIQUE);

        Collection<Recommendation> content = contentBasedAlgorithm.recommendToUser(datasetLoader, model.getModel(0), idUser, candidateItems);
        Collection<Recommendation> collaborative = collaborativeFilteringTechnique.recommendToUser(datasetLoader, model.getModel(1), idUser, candidateItems);

        return joinRecommendationLists(content, collaborative);
    }

    @Override
    protected List<RecommenderSystem<Object>> getHybridizedRecommenderSystems() {
        ContentBasedRecommender<Object, Object> contentBasedAlgorithm = (ContentBasedRecommender<Object, Object>) getParameterValue(CONTENT_BASED_TECHNIQUE);
        CollaborativeRecommender<Object> collaborativeFilteringTechnique = (CollaborativeRecommender<Object>) getParameterValue(COLLABORATIVE_TECHNIQUE);
        List<RecommenderSystem<Object>> ret = new LinkedList<>();

        ret.add(contentBasedAlgorithm);
        ret.add(collaborativeFilteringTechnique);
        return ret;
    }

    /**
     * Une dos listas de recomendación utilizando el operador definido.
     *
     * @param l1 Lista de recomendaciones.
     * @param l2 Lista de recomendaciones.
     * @return Lista de recomendaciones unida.
     */
    private Collection<Recommendation> joinRecommendationLists(Collection<Recommendation> l1, Collection<Recommendation> l2) {
        int size = Math.max(l1.size(), l2.size());

        class rank implements Comparable<rank> {

            public rank(int idItem) {
                this.idItem = idItem;
            }
            final int idItem;
            float content = 0;
            float collaborative = 0;
            float valorCombinado = 0;

            @Override
            public int compareTo(rank o) {
                if (valorCombinado < o.valorCombinado) {
                    return 1;
                } else {
                    if (valorCombinado > o.valorCombinado) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            }

            @Override
            public String toString() {
                return idItem + "->" + valorCombinado;
            }
        }
        TwoValuesAggregator aggregationOperator = (TwoValuesAggregator) getParameterValue(AGGREGATION_OPERATOR);

        Map<Integer, rank> mapa = new TreeMap<>();
        int i = 0;
        for (Recommendation r : l1) {
            rank ranking = new rank(r.getIdItem());
            ranking.content = 1 - (((float) i) / size);
            mapa.put(r.getIdItem(), ranking);
            i++;
        }

        i = 0;
        for (Recommendation r : l2) {
            rank ranking;
            if (mapa.containsKey(r.getIdItem())) {
                ranking = mapa.get(r.getIdItem());
            } else {
                ranking = new rank(r.getIdItem());
            }
            ranking.collaborative = 1 - (((float) i) / size);
            mapa.put(r.getIdItem(), ranking);
            i++;
        }

        List<rank> finalList = new LinkedList<>(mapa.values());

        finalList.stream().forEach((r) -> {
            r.valorCombinado = aggregationOperator.aggregateTwoValues(r.collaborative, r.content);
        });

        Collections.sort(finalList);

        Collection<Recommendation> ret = new LinkedList<>();

        finalList.stream().filter((r) -> (r.valorCombinado != 0)).forEach((r) -> {
            ret.add(new Recommendation(r.idItem, r.valorCombinado));
        });
        return ret;
    }
}
