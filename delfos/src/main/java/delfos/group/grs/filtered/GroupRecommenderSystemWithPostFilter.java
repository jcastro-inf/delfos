package delfos.group.grs.filtered;

import delfos.common.Global;
import delfos.common.aggregationoperators.AggregationOperator;
import delfos.common.aggregationoperators.Mean;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.MultiThreadExecutionManager;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterListener;
import delfos.common.parameters.restriction.ObjectParameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.common.parameters.restriction.RecommenderSystemParameterRestriction;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.util.DatasetPrinterDeprecated;
import delfos.experiment.casestudy.parallel.SingleUserRecommendationTask;
import delfos.experiment.casestudy.parallel.SingleUserRecommendationTaskExecutor;
import delfos.factories.AggregationOperatorFactory;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.SingleRecommendationModel;
import delfos.group.grs.filtered.filters.GroupRatingsFilter;
import delfos.group.grs.filtered.filters.OutliersRatingsFilter;
import delfos.rs.RecommenderSystem;
import delfos.rs.RecommendationModelBuildingProgressListener;
import delfos.rs.collaborativefiltering.knn.modelbased.KnnModelBasedCFRS;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementa la unión de un sistema de recomendación a individuos con un
 * algoritmo de filtrado de las predicciones de los usuarios. Finalmente agrega
 * con una función de agregación.
 *
 * @author Jorge Castro Gallardo
 * @version 1.0 22-May-2013
 */
public class GroupRecommenderSystemWithPostFilter extends GroupRecommenderSystemAdapter<SingleRecommendationModel, GroupOfUsers> {

    private static final long serialVersionUID = 1L;
    /**
     * Sistema de recomendación a individuos que utiliza.
     */
    public static final Parameter RECOMMENDER_SYSTEM = new Parameter(
            "recommenderSystem",
            new RecommenderSystemParameterRestriction(new KnnModelBasedCFRS(), RecommenderSystem.class),
            "Sistema de recomendación a grupos que utiliza.");
    /**
     * Filtro de valoraciones de grupo que se aplica antes de realizar la
     * recomendación.
     */
    public static final Parameter FILTER = new Parameter(
            "filter",
            new ParameterOwnerRestriction(GroupRatingsFilter.class, new OutliersRatingsFilter(0.5, 0.2)),
            "Filtro que se aplica a las predicciones antes de agregarlas.");
    /**
     * Especifica la técnica de agregación para agregar los ratings de los
     * usuarios y formar el perfil del grupo.
     */
    public static final Parameter AGGREGATION_OPPERATOR = new Parameter(
            "aggregationOperator",
            new ObjectParameter(AggregationOperatorFactory.getInstance().getAllAggregationOperators(), new Mean()),
            "Especifica la técnica de agregación para agregar los ratings de "
            + "los usuarios y formar el perfil del grupo.");

    public GroupRecommenderSystemWithPostFilter() {
        super();
        addParameter(FILTER);
        addParameter(AGGREGATION_OPPERATOR);
        addParameter(RECOMMENDER_SYSTEM);

        ParameterListener keepMaintainRecommender = new ParameterListener() {
            private RecommenderSystem rs = null;
            private RecommendationModelBuildingProgressListener bpl = new RecommendationModelBuildingProgressListener() {
                @Override
                public void buildingProgressChanged(String actualJob, int percent, long remainingTime) {
                    fireBuildingProgressChangedEvent(actualJob, percent, remainingTime);
                }
            };

            @Override
            public void parameterChanged() {
                if (rs == null) {
                    rs = GroupRecommenderSystemWithPostFilter.this.getRecommenderSystem();
                    rs.addRecommendationModelBuildingProgressListener(bpl);
                }
                if (getRecommenderSystem() == rs) {
                    //Es el mismo, no hacer nada.
                } else {
                    rs.removeRecommendationModelBuildingProgressListener(bpl);
                    rs = getRecommenderSystem();
                    rs.addRecommendationModelBuildingProgressListener(bpl);
                }
            }
        };

        this.addParammeterListener(keepMaintainRecommender);
    }

    public GroupRecommenderSystemWithPostFilter(RecommenderSystem rs, GroupRatingsFilter filter, AggregationOperator aggregationOperator) {
        this();

        setParameterValue(RECOMMENDER_SYSTEM, rs);
        setParameterValue(FILTER, filter);
        setParameterValue(AGGREGATION_OPPERATOR, aggregationOperator);
    }

    @Override
    public boolean isRatingPredictorRS() {
        return getRecommenderSystem().isRatingPredictorRS();
    }

    @Override
    public SingleRecommendationModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        Object build = getRecommenderSystem().buildRecommendationModel(datasetLoader);
        return new SingleRecommendationModel(build);
    }

    @Override
    public GroupOfUsers buildGroupModel(DatasetLoader<? extends Rating> datasetLoader, SingleRecommendationModel RecommendationModel, GroupOfUsers groupOfUsers) throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        return new GroupOfUsers(groupOfUsers.getIdMembers());
    }

    @Override
    public Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, SingleRecommendationModel RecommendationModel, GroupOfUsers groupModel, GroupOfUsers groupOfUsers, java.util.Set<Integer> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {

        List<SingleUserRecommendationTask> tasks = new LinkedList<SingleUserRecommendationTask>();
        for (int idUser : groupOfUsers) {
            tasks.add(new SingleUserRecommendationTask(getRecommenderSystem(), datasetLoader, RecommendationModel.getRecommendationModel(), idUser, candidateItems));
        }

        MultiThreadExecutionManager<SingleUserRecommendationTask> executionManager = new MultiThreadExecutionManager<>(
                "Prediction of each member",
                tasks,
                SingleUserRecommendationTaskExecutor.class);
        executionManager.run();

        Map<Integer, Map<Integer, Number>> listsWithoutFilter = new TreeMap<Integer, Map<Integer, Number>>();
        for (SingleUserRecommendationTask task : executionManager.getAllFinishedTasks()) {
            Collection<Recommendation> recommendations = task.getRecommendationList();
            if (recommendations == null) {
                throw new UserNotFound(task.getIdUser());
            }
            Map<Integer, Number> predictions = new TreeMap<Integer, Number>();
            for (Recommendation r : recommendations) {
                predictions.put(r.getIdItem(), r.getPreference());
            }
            listsWithoutFilter.put(task.getIdUser(), predictions);
        }

        Map<Integer, Map<Integer, Number>> filteredLists = filterLists(getFilter(), listsWithoutFilter);

        Collection<Recommendation> ret = aggregateLists(getAggregationOperator(), filteredLists);

        {
            //Muestro las listas individuales y agregadas, para depuración.
            Map<Integer, Map<Integer, Number>> all = new TreeMap<Integer, Map<Integer, Number>>();
            all.putAll(listsWithoutFilter);

            Map<Integer, Number> aggregateListNotFiltered = new TreeMap<Integer, Number>();
            Collection<Recommendation> retNoFilter = aggregateLists(getAggregationOperator(), listsWithoutFilter);
            for (Recommendation r : retNoFilter) {
                aggregateListNotFiltered.put(r.getIdItem(), r.getPreference());
            }
            all.put(88888888, aggregateListNotFiltered);

            Map<Integer, Number> aggregateListFiltered = new TreeMap<Integer, Number>();
            for (Recommendation r : ret) {
                aggregateListFiltered.put(r.getIdItem(), r.getPreference());
            }
            all.put(99999999, aggregateListFiltered);

            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage("=============== DEBUG of post filter =========");
                DatasetPrinterDeprecated.printCompactRatingTable(all);
                Global.showInfoMessage("==============================================");
            }
        }

        return ret;
    }

    public static Map<Integer, Map<Integer, Number>> filterLists(GroupRatingsFilter filter, Map<Integer, Map<Integer, Number>> toFilter) {
        Map<Integer, Map<Integer, Number>> filtered = filter.getFilteredRatings(toFilter);
        return filtered;
    }

    public static Collection<Recommendation> aggregateLists(AggregationOperator aggregationOperator, Map<Integer, Map<Integer, Number>> groupUtilityList) {

        //Reordeno las predicciones.
        Map<Integer, Collection<Number>> prediction_byItem = new TreeMap<Integer, Collection<Number>>();
        for (int idUser : groupUtilityList.keySet()) {
            for (int idItem : groupUtilityList.get(idUser).keySet()) {
                Number preference = groupUtilityList.get(idUser).get(idItem);

                if (!prediction_byItem.containsKey(idItem)) {
                    prediction_byItem.put(idItem, new LinkedList<Number>());
                }

                prediction_byItem.get(idItem).add(preference);
            }
        }

        //agrego las predicciones de cada item.
        ArrayList<Recommendation> ret = new ArrayList<>();
        for (int idItem : prediction_byItem.keySet()) {
            Collection<Number> predictionsThisItem = prediction_byItem.get(idItem);

            if (prediction_byItem.isEmpty()) {
                continue;
            }

            float aggregateValue = aggregationOperator.aggregateValues(predictionsThisItem);
            ret.add(new Recommendation(idItem, aggregateValue));
        }

        //Ordeno las recomendaciones.
        Collections.sort(ret);

        return ret;
    }

    private RecommenderSystem getRecommenderSystem() {
        return (RecommenderSystem) getParameterValue(RECOMMENDER_SYSTEM);
    }

    private GroupRatingsFilter getFilter() {
        return (GroupRatingsFilter) getParameterValue(FILTER);
    }

    private AggregationOperator getAggregationOperator() {
        return (AggregationOperator) getParameterValue(AGGREGATION_OPPERATOR);
    }
}
