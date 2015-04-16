package delfos.rs.hybridtechniques;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.rs.collaborativefiltering.CollaborativeRecommender;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.contentbased.ContentBasedRecommender;
import delfos.rs.contentbased.vsm.booleanvsm.tfidf.TfIdfCBRS;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.RecommenderSystem;
import delfos.rs.RecommenderSystemBuildingProgressListener;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.RecommenderSystemParameterRestriction;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 25-Noviembre-2013
 */
public class HybridAlternatingListRS extends HybridRecommender<HybridRecommenderSystemModel> {

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
    public HybridRecommenderSystemModel build(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset {
        RecommenderSystem<Object> firstTechnique = (RecommenderSystem<Object>) getParameterValue(FIRST_TECHNIQUE);
        RecommenderSystemBuildingProgressListener firstTechniqueListener = new RecommenderSystemBuildingProgressListener() {
            @Override
            public void buildingProgressChanged(String actualJob, int percent, long remainingSeconds) {
                HybridAlternatingListRS.this.fireBuildingProgressChangedEvent(actualJob, percent / 2 + 50, -1);
            }
        };
        firstTechnique.addBuildingProgressListener(firstTechniqueListener);
        Object firstTechniqueModel = firstTechnique.build(datasetLoader);

        RecommenderSystem<Object> secondTechnique = (RecommenderSystem<Object>) getParameterValue(SECOND_TECHNIQUE);
        RecommenderSystemBuildingProgressListener secondTechniqueListener = new RecommenderSystemBuildingProgressListener() {
            @Override
            public void buildingProgressChanged(String actualJob, int percent, long remainingSeconds) {
                HybridAlternatingListRS.this.fireBuildingProgressChangedEvent(actualJob, percent / 2, -1);
            }
        };
        secondTechnique.addBuildingProgressListener(secondTechniqueListener);
        Object secondTechniqueModel = secondTechnique.build(datasetLoader);

        return new HybridRecommenderSystemModel(firstTechniqueModel, secondTechniqueModel);
    }

    @Override
    public List<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, HybridRecommenderSystemModel model, Integer idUser, Collection<Integer> idItemList) throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, ItemNotFound, NotEnoughtUserInformation {

        RecommenderSystem<Object> firstTechnique = (RecommenderSystem<Object>) getParameterValue(FIRST_TECHNIQUE);

        RecommenderSystem<Object> secondTechnique = (RecommenderSystem<Object>) getParameterValue(SECOND_TECHNIQUE);

        List<Recommendation> firstTechniqueList = firstTechnique.recommendOnly(datasetLoader, model.getModel(0), idUser, idItemList);
        List<Recommendation> secondTechniqueList = secondTechnique.recommendOnly(datasetLoader, model.getModel(1), idUser, idItemList);

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
    private List<Recommendation> joinRecommendationLists(List<Recommendation> l1, List<Recommendation> l2) {
        final float numItems;
        {
            Set<Integer> allItems = new TreeSet<Integer>();
            for (Recommendation recommendation : l1) {
                allItems.add(recommendation.getIdItem());
            }
            for (Recommendation recommendation : l2) {
                allItems.add(recommendation.getIdItem());
            }
            numItems = allItems.size();
        }

        Set<Integer> alreadyAddedItems = new TreeSet<Integer>();
        List<Recommendation> ret = new ArrayList<Recommendation>();

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
