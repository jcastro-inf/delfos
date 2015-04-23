package delfos.rs.hybridtechniques;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.RecommenderSystemParameterRestriction;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.rs.RecommenderSystem;
import delfos.rs.RecommendationModelBuildingProgressListener;
import delfos.rs.collaborativefiltering.CollaborativeRecommender;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.contentbased.ContentBasedRecommender;
import delfos.rs.contentbased.vsm.booleanvsm.tfidf.TfIdfCBRS;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 25-Noviembre-2013
 */
public class HybridAlternatingListRS extends HybridRecommender<HybridRecommendationModel> {

    private static final long serialVersionUID = -3387516993124229948L;

    static {
        @SuppressWarnings({"rawtypes", "unchecked"})
        Class<? extends RecommenderSystem>[] values = (Class<? extends RecommenderSystem>[]) new Class[1];
        values[0] = RecommenderSystem.class;
        FIRST_TECHNIQUE = new Parameter("Collaborative_technique", new RecommenderSystemParameterRestriction(new KnnMemoryBasedCFRS(), values));

        values = (Class<? extends RecommenderSystem<Object>>[]) new Class[1];
        values[0] = RecommenderSystem.class;
        SECOND_TECHNIQUE = new Parameter("Content_based_technique", new RecommenderSystemParameterRestriction(new TfIdfCBRS(), values));
    }
    public static final Parameter FIRST_TECHNIQUE;
    public static final Parameter SECOND_TECHNIQUE;

    public HybridAlternatingListRS() {
        addParameter(FIRST_TECHNIQUE);
        addParameter(SECOND_TECHNIQUE);

    }

    @Override
    public HybridRecommendationModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset {
        RecommenderSystem<Object> firstTechnique = (RecommenderSystem<Object>) getParameterValue(FIRST_TECHNIQUE);
        RecommendationModelBuildingProgressListener firstTechniqueListener = new RecommendationModelBuildingProgressListener() {
            @Override
            public void buildingProgressChanged(String actualJob, int percent, long remainingSeconds) {
                HybridAlternatingListRS.this.fireBuildingProgressChangedEvent(actualJob, percent / 2 + 50, -1);
            }
        };
        firstTechnique.addRecommendationModelBuildingProgressListener(firstTechniqueListener);
        Object firstTechniqueModel = firstTechnique.buildRecommendationModel(datasetLoader);

        RecommenderSystem<Object> secondTechnique = (RecommenderSystem<Object>) getParameterValue(SECOND_TECHNIQUE);
        RecommendationModelBuildingProgressListener secondTechniqueListener = new RecommendationModelBuildingProgressListener() {
            @Override
            public void buildingProgressChanged(String actualJob, int percent, long remainingSeconds) {
                HybridAlternatingListRS.this.fireBuildingProgressChangedEvent(actualJob, percent / 2, -1);
            }
        };
        secondTechnique.addRecommendationModelBuildingProgressListener(secondTechniqueListener);
        Object secondTechniqueModel = secondTechnique.buildRecommendationModel(datasetLoader);

        return new HybridRecommendationModel(firstTechniqueModel, secondTechniqueModel);
    }

    @Override
    public Collection<Recommendation> recommendToUser(DatasetLoader<? extends Rating> datasetLoader, HybridRecommendationModel model, Integer idUser, java.util.Set<Integer> candidateItems) throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, ItemNotFound, NotEnoughtUserInformation {

        RecommenderSystem<Object> firstTechnique = (RecommenderSystem<Object>) getParameterValue(FIRST_TECHNIQUE);

        RecommenderSystem<Object> secondTechnique = (RecommenderSystem<Object>) getParameterValue(SECOND_TECHNIQUE);

        Collection<Recommendation> firstTechniqueList = firstTechnique.recommendToUser(datasetLoader, model.getModel(0), idUser, candidateItems);
        Collection<Recommendation> secondTechniqueList = secondTechnique.recommendToUser(datasetLoader, model.getModel(1), idUser, candidateItems);

        return joinRecommendationLists(firstTechniqueList, secondTechniqueList);
    }

    @Override
    protected List<RecommenderSystem<Object>> getHybridizedRecommenderSystems() {
        ContentBasedRecommender<Object, Object> contentBasedAlgorithm = (ContentBasedRecommender<Object, Object>) getParameterValue(SECOND_TECHNIQUE);
        CollaborativeRecommender<Object> collaborativeFilteringTechnique = (CollaborativeRecommender<Object>) getParameterValue(FIRST_TECHNIQUE);
        List<RecommenderSystem<Object>> ret = new LinkedList<RecommenderSystem<Object>>();

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
        final float numItems;
        {
            Set<Integer> allItems = new TreeSet<>();
            for (Recommendation recommendation : l1) {
                allItems.add(recommendation.getIdItem());
            }
            l2.stream().forEach((recommendation) -> {
                allItems.add(recommendation.getIdItem());
            });
            numItems = allItems.size();
        }

        Set<Integer> alreadyAddedItems = new TreeSet<>();
        Collection<Recommendation> ret = new ArrayList<>();

        int i = 0;
        Iterator<Recommendation> l1Iterator = l1.iterator();
        Iterator<Recommendation> l2Iterator = l2.iterator();

        while (l1Iterator.hasNext() && l2Iterator.hasNext()) {

            float rank = numItems - i;

            if (i % 2 == 0) {
                //l1
                if (l1Iterator.hasNext()) {
                    Recommendation recommendation = l1Iterator.next();
                    if (alreadyAddedItems.contains(recommendation.getIdItem())) {
                        continue;
                    } else {
                        alreadyAddedItems.add(recommendation.getIdItem());
                    }
                    ret.add(new Recommendation(recommendation.getIdItem(), rank / numItems));
                } else {
                    if (l2Iterator.hasNext()) {
                        Recommendation recommendation = l2Iterator.next();
                        if (alreadyAddedItems.contains(recommendation.getIdItem())) {
                            continue;
                        } else {
                            alreadyAddedItems.add(recommendation.getIdItem());
                        }
                        ret.add(new Recommendation(recommendation.getIdItem(), rank / numItems));
                    } else {
                        break;
                    }
                }

            } else {
                //l2
                if (l2Iterator.hasNext()) {
                    Recommendation recommendation = l2Iterator.next();
                    if (alreadyAddedItems.contains(recommendation.getIdItem())) {
                        continue;
                    } else {
                        alreadyAddedItems.add(recommendation.getIdItem());
                    }
                    ret.add(new Recommendation(recommendation.getIdItem(), rank / numItems));
                } else {
                    if (l1Iterator.hasNext()) {
                        Recommendation recommendation = l1Iterator.next();
                        if (alreadyAddedItems.contains(recommendation.getIdItem())) {
                            continue;
                        } else {
                            alreadyAddedItems.add(recommendation.getIdItem());
                        }
                        ret.add(new Recommendation(recommendation.getIdItem(), rank / numItems));
                    } else {
                        break;
                    }
                }
            }

            i++;

        }

        return ret;
    }
}
