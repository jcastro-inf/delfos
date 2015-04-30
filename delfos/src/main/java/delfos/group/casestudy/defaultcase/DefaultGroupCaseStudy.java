package delfos.group.casestudy.defaultcase;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import delfos.common.Chronometer;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadTrustDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.MultiThreadExecutionManager;
import delfos.common.parallelwork.Parallelisation;
import delfos.common.parallelwork.notblocking.MultiThreadExecutionManager_NotBlocking;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.TrustDatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.group.casestudy.GroupCaseStudy;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendation;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTask;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.RandomGroupRecommender;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupMeasureResult;
import delfos.group.results.grouprecomendationresults.GroupRecommendationResult;
import delfos.group.experiment.validation.validationtechniques.GroupValidationTechnique;
import delfos.group.experiment.validation.validationtechniques.HoldOutGroupRatedItems;
import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.experiment.validation.predictionvalidation.GroupRecommendationRequest;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;
import delfos.experiment.SeedHolder;
import delfos.rs.RecommenderSystemBuildingProgressListener_default;
import delfos.rs.recommendation.Recommendation;

/**
 * Clase para ejecutar un caso de estudio de sistemas de recomendación a grupos.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 20-Mayo-2013
 */
public class DefaultGroupCaseStudy extends GroupCaseStudy {

    private static final long serialVersionUID = 1L;
    // ================== VARIABLES QUE NO CAMBIAN EN LA EJECUCION =============
    protected final DatasetLoader<? extends Rating> datasetLoader;
    protected final GroupRecommenderSystem groupRecommenderSystem;
    protected final GroupFormationTechnique groupFormationTechnique;
    protected final int numEjecuciones;
    protected final Collection<GroupEvaluationMeasure> groupEvaluationMeasures;
    protected final RelevanceCriteria relevanceCriteria;
    protected final GroupPredictionProtocol groupPredictionProtocol;
    protected final GroupValidationTechnique groupValidationTechnique;
    // ================== VARIABLES QUE CAMBIAN EN LA EJECUCION ================
    protected int ejecucionActual;
    protected Map<GroupEvaluationMeasure, GroupMeasureResult>[][] executionsResult;
    protected Integer recommenderListSize = null;
    protected boolean finished = false;
    private long[][] buildTimes;//executionTimes[ejecucion][conjunto]
    private long[][] recommendationTimes;//executionTimes[ejecucion][conjunto]
    private long[][] groupBuildTime;

    /**
     * Crea un caso de estudio para experimentar con sistemas de recomendación a
     * grupos.
     *
     * @param datasetLoader
     * @param groupRecommenderSystem
     * @param groupFormationTechnique
     * @param groupValidationTechniqueValue
     * @param groupPredictionProtocol
     * @param evaluationMeasures
     * @param criteria
     * @param numEjecuciones
     */
    public DefaultGroupCaseStudy(
            DatasetLoader<? extends Rating> datasetLoader,
            GroupRecommenderSystem groupRecommenderSystem,
            GroupFormationTechnique groupFormationTechnique,
            GroupValidationTechnique groupValidationTechniqueValue,
            GroupPredictionProtocol groupPredictionProtocol,
            Collection<GroupEvaluationMeasure> evaluationMeasures,
            RelevanceCriteria criteria,
            int numEjecuciones) {

        this.datasetLoader = datasetLoader;
        this.groupRecommenderSystem = groupRecommenderSystem;
        this.groupFormationTechnique = groupFormationTechnique;
        this.numEjecuciones = numEjecuciones;
        this.groupEvaluationMeasures = evaluationMeasures;
        this.relevanceCriteria = criteria;
        this.groupPredictionProtocol = groupPredictionProtocol;
        this.groupValidationTechnique = groupValidationTechniqueValue;

        setAlias(groupRecommenderSystem.getAlias());
    }

    public DefaultGroupCaseStudy(DatasetLoader<? extends Rating> datasetLoader) {

        this(datasetLoader,
                new RandomGroupRecommender(),
                new FixedGroupSize_OnlyNGroups(2, 2), new HoldOutGroupRatedItems(), new NoPredictionProtocol(),
                GroupEvaluationMeasuresFactory.getInstance().getAllClasses(),
                new RelevanceCriteria(4), 1);
        setAlias(datasetLoader.getAlias());
    }

    @Override
    public GroupPredictionProtocol getGroupPredictionProtocol() {
        return groupPredictionProtocol;
    }

    @Override
    public void execute() throws CannotLoadContentDataset, CannotLoadRatingsDataset, UserNotFound, ItemNotFound {
        initGroupCaseStudy();

        MultiThreadExecutionManager_NotBlocking<DefaultGroupCaseStudyGroupEvaluationMeasures_Task> multiThreadExecutionManagerEvaluationMeasures
                = new MultiThreadExecutionManager_NotBlocking<>(
                        "DefaultCaseStudy.computeEvaluationMeasures()",
                        DefaultGroupCaseStudyGroupEvaluationMeasures_SingleTaskExecutor.class
                );
        multiThreadExecutionManagerEvaluationMeasures.runInBackground();

        int numVueltas = 1;
        int numParticiones = getGroupValidationTechnique().getNumberOfSplits();
        int maxVueltas = numParticiones * numEjecuciones;

        setNumVueltas(maxVueltas);

        executionsResult = new Map[numEjecuciones][numParticiones];
        for (int execution = 0; execution < numEjecuciones; execution++) {
            for (int split = 0; split < numParticiones; split++) {
                executionsResult[execution][split] = new TreeMap<>();
            }
        }
        groupRecommenderSystem.addRecommendationModelBuildingProgressListener(new RecommenderSystemBuildingProgressListener_default(System.out, 5000));
        initTimes(numEjecuciones, numParticiones);

        MeanIterative tiempoParticion = new MeanIterative();

        loadDataset(datasetLoader);

        int loopCount = 0;
        setExperimentProgress(getAlias() + " --> Running Case Study Group", 0, -1);
        for (ejecucionActual = 0; ejecucionActual < numEjecuciones; ejecucionActual++) {
            setNextSeedToSeedHolders(loopCount);

            Collection<GroupOfUsers> groups = groupFormationTechnique.shuffle(datasetLoader);
            PairOfTrainTestRatingsDataset[] pairsOfTrainTest = groupValidationTechnique.shuffle(datasetLoader, groups);

            for (int particionActual = 0; particionActual < pairsOfTrainTest.length; particionActual++) {

                setNextSeedToSeedHolders(loopCount);

                Chronometer cronometroTiempoParticion = new Chronometer();

                setVueltaActual(ejecucionActual * numParticiones + particionActual);

                setExperimentProgress(getAlias() + " --> Executing (ex " + (ejecucionActual + 1) + "/" + numEjecuciones + ") (split " + (particionActual + 1) + "/" + numParticiones + ")", ejecucionActual * numParticiones + particionActual, -1);
                setExecutionProgress(getAlias() + " --> Building Recommender System Model", 0, -1);

                DatasetLoader<? extends Rating> trainDatasetLoader = pairsOfTrainTest[particionActual].getTrainingDatasetLoader();
                DatasetLoader<? extends Rating> testDatasetLoader = pairsOfTrainTest[particionActual].getTestDatasetLoader();

                long totalBuildTime;
                Object groupRecommendationModel;
                {
                    Chronometer c = new Chronometer();
                    groupRecommendationModel = groupRecommenderSystem.buildRecommendationModel(trainDatasetLoader);
                    if (groupRecommendationModel == null) {
                        throw new IllegalStateException("The RecommendationModel cannot be null");
                    }

                    long spent = c.getTotalElapsed();
                    setBuildTime(ejecucionActual, particionActual, spent);
                    totalBuildTime = spent;
                }

                Global.showInfoMessage("----------------------- End of Build ----------------------------------" + "\n");

                Global.showInfoMessage("---------------- Init of group recommendation -------------------------" + "\n");
                long totalGroupBuildTime = 0;
                long totalGroupRecommendationTime = 0;

                List<SingleGroupRecommendationTask> taskRecommendGroup = new ArrayList<>(groups.size());
                for (GroupOfUsers groupOfUsers : groups) {
                    for (GroupRecommendationRequest groupRecommendationRequest : groupPredictionProtocol.getGroupRecommendationRequests(trainDatasetLoader, testDatasetLoader, groupOfUsers)) {

                        taskRecommendGroup.add(new SingleGroupRecommendationTask(
                                groupRecommenderSystem,
                                groupRecommendationRequest.predictionPhaseDatasetLoader,
                                groupRecommendationModel,
                                groupOfUsers,
                                groupRecommendationRequest.itemsToPredict)
                        );
                    }
                }

                MultiThreadExecutionManager<SingleGroupRecommendationTask> groupsModelsRecommendation = new MultiThreadExecutionManager<>(
                        getAlias() + " --> Group Recomendation",
                        taskRecommendGroup,
                        SingleGroupRecommendation.class);

                groupsModelsRecommendation.addExecutionProgressListener((String proceso, int percent, long remainingMiliSeconds) -> {
                    setExecutionProgress(getAlias() + " --> " + proceso + " " + percent + "%", (int) ((percent / 2.0f) + 50), remainingMiliSeconds);
                });
                groupsModelsRecommendation.run();

                Map<GroupOfUsers, Collection<Recommendation>> recomendacionesPorGrupo = new TreeMap<>();
                Map<GroupOfUsers, Collection<Integer>> solicitudesPorGrupo = new TreeMap<>();

                Collection<SingleGroupRecommendationTask> allFinishedTasks = groupsModelsRecommendation.getAllFinishedTasks();
                for (SingleGroupRecommendationTask task : allFinishedTasks) {
                    GroupOfUsers group = task.getGroup();
                    if (!recomendacionesPorGrupo.containsKey(group)) {
                        recomendacionesPorGrupo.put(group, new ArrayList<>());
                    }
                    if (!solicitudesPorGrupo.containsKey(group)) {
                        solicitudesPorGrupo.put(group, new ArrayList<>());
                    }

                    if (task.getRecommendations() == null) {
                        throw new IllegalStateException(getAlias() + " --> Cannot recommend to group a null recommendations, should be an empty list instead.");
                    }

                    if (task.getCandidateItems() == null) {
                        throw new IllegalStateException(getAlias() + " --> Cannot retrieve the group requests.");
                    }

                    recomendacionesPorGrupo.get(group).addAll(task.getRecommendations());
                    solicitudesPorGrupo.get(group).addAll(task.getCandidateItems());

                    totalGroupBuildTime += task.getBuildGroupModelTime();
                    totalGroupRecommendationTime += task.getRecommendationTime();
                }
                setExecutionProgress(getAlias() + " --> Recommendation process", 100, -1);

                if (groupRecommenderSystem instanceof Closeable) {
                    Closeable closeable = (Closeable) groupRecommenderSystem;
                    try {
                        closeable.close();
                    } catch (IOException ex) {
                        Logger.getLogger(DefaultGroupCaseStudy.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                //Se realiza el conteo del tiempo consumido en la ejecución
                setGroupBuildTime(ejecucionActual, particionActual, totalGroupBuildTime);
                setRecommendationTime(ejecucionActual, particionActual, totalGroupRecommendationTime);

                tiempoParticion.addValue(cronometroTiempoParticion.getTotalElapsed());

                long tiempoRestanteExperimento = (long) ((maxVueltas - numVueltas) * tiempoParticion.getMean());
                setExperimentProgress(getAlias() + " --> Executing (ex " + (ejecucionActual + 1) + "/" + numEjecuciones + ") (split " + (particionActual + 1) + "/" + numParticiones + ")", ejecucionActual * numParticiones + particionActual, tiempoRestanteExperimento);
                numVueltas++;

                GroupRecommendationResult groupRecommendationResult = new GroupRecommendationResult(groupValidationTechnique.getSeedValue(), totalBuildTime, totalGroupBuildTime, totalGroupRecommendationTime, solicitudesPorGrupo, recomendacionesPorGrupo, getAlias());

                multiThreadExecutionManagerEvaluationMeasures.addTask(
                        new DefaultGroupCaseStudyGroupEvaluationMeasures_Task(
                                ejecucionActual,
                                particionActual,
                                groupRecommendationResult,
                                pairsOfTrainTest[particionActual].test,
                                groupEvaluationMeasures,
                                relevanceCriteria));

                loopCount++;
            }
        }

        setExperimentProgress(getAlias() + " --> Waiting for evaluation measures values", 0, -1);

        try {
            multiThreadExecutionManagerEvaluationMeasures.waitUntilFinished();
        } catch (InterruptedException ex) {
            Logger.getLogger(DefaultGroupCaseStudy.class.getName()).log(Level.SEVERE, null, ex);
        }

        multiThreadExecutionManagerEvaluationMeasures.getAllFinishedTasks().stream().forEach((task) -> {
            task.groupEvaluationMeasuresResults.entrySet().stream().forEach((entry) -> {
                GroupEvaluationMeasure groupEvaluationMeasure = entry.getKey();
                GroupMeasureResult groupMeasureResult = entry.getValue();

                this.executionsResult[task.ejecucion][task.particion].put(groupEvaluationMeasure, groupMeasureResult);
            });
        });

        setExperimentProgress(getAlias() + " --> Finished evaluation measures values", 100, -1);
        //caseStudyProgressChangedFireEvent(100);

        finished = true;
        setExperimentProgress(getAlias() + " --> Experiment execution finished", 100, -1);
    }

    private void initGroupCaseStudy() {
        finished = false;

        setExperimentProgress(getAlias() + " --> Starting Case Study Group", 0, -1);
        setExecutionProgress(getAlias() + " --> Starting Case Study Group", 0, -1);

        if (Global.isVerboseAnnoying()) {
            Parallelisation.printSchedulingInfo(System.out);
        }
    }

    @Override
    public Collection<GroupEvaluationMeasure> getEvaluationMeasures() {
        return groupEvaluationMeasures;
    }

    @Override
    public GroupMeasureResult getMeasureResult(GroupEvaluationMeasure em, int execution, int split) {

        Map<GroupEvaluationMeasure, GroupMeasureResult> thisExecution_evaluationMeasuresValues = this.executionsResult[execution][split];

        if (thisExecution_evaluationMeasuresValues.containsKey(em)) {
            GroupMeasureResult groupMeasureResult = thisExecution_evaluationMeasuresValues.get(em);
            return groupMeasureResult;
        } else {
            throw new IllegalArgumentException("The evaluation measure " + em + " has no value.");
        }
    }

    @Override
    public int getNumExecutions() {
        return numEjecuciones;
    }

    @Override
    public GroupRecommenderSystem getGroupRecommenderSystem() {
        return groupRecommenderSystem;
    }

    /**
     * Método que devuelve el resultado agregado de una medida de evaluación.
     *
     * @param em Medida de evaluación
     * @return Resultado de la medida de evaluación
     */
    @Override
    public GroupMeasureResult getAggregateMeasureResult(GroupEvaluationMeasure em) {
        List<GroupMeasureResult> measureResult = new ArrayList<>();
        for (int execution = 0; execution < getNumExecutions(); execution++) {
            for (int split = 0; split < getGroupValidationTechnique().getNumberOfSplits(); split++) {
                measureResult.add(getMeasureResult(em, execution, split));
            }

        }
        return em.agregateResults(measureResult);
    }

    /**
     * Devuelve el tiempo de ejecución en milisegundos de una partición concreta
     * de una ejecución dada.
     *
     * @param execution Ejecución para la que se quiere conocer el tiempo.
     * @param split Partición de la ejecución indicada para la que se quiere
     * conocer el tiempo.
     * @return tiempo de ejecución en milisegundos.
     */
    @Override
    public long getBuildTime(int execution, int split) {
        return buildTimes[execution][split];
    }

    /**
     * Devuelve el tiempo de ejecución en milisegundos de una partición concreta
     * de una ejecución dada
     *
     * @param execution Ejecución para la que se quiere conocer el tiempo
     * @param split Partición de la ejecución indicada para la que se quiere
     * conocer el tiempo.
     * @return tiempo de ejecución en milisegundos
     */
    @Override
    public long getRecommendationTime(int execution, int split) {
        return recommendationTimes[execution][split];
    }

    protected void initTimes(int numEjecuciones, int numParticiones) {
        buildTimes = new long[numEjecuciones][numParticiones];
        groupBuildTime = new long[numEjecuciones][numParticiones];
        recommendationTimes = new long[numEjecuciones][numParticiones];
    }

    protected void setBuildTime(int numEjecuciones, int particionActual, long time) {
        buildTimes[numEjecuciones][particionActual] = time;
    }

    protected void setRecommendationTime(int numEjecuciones, int particionActual, long time) {
        recommendationTimes[numEjecuciones][particionActual] = time;
    }

    @Override
    public DatasetLoader<? extends Rating> getDatasetLoader() {
        return datasetLoader;
    }

    @Override
    public RelevanceCriteria getRelevanceCriteria() {
        return relevanceCriteria;
    }

    @Override
    public boolean isFinished() {
        return this.finished;
    }

    @Override
    public GroupFormationTechnique getGroupFormationTechnique() {
        return groupFormationTechnique;
    }

    @Override
    public void putResult(int ejecucion, int split, GroupEvaluationMeasure e, GroupMeasureResult groupMeasureResult) {
        executionsResult[ejecucion][split].put(e, groupMeasureResult);
    }

    @Override
    public GroupValidationTechnique getGroupValidationTechnique() {
        return groupValidationTechnique;
    }

    protected void setGroupBuildTime(int ejecucionActual, int splitActual, long totalGroupBuildTime) {
        groupBuildTime[ejecucionActual][splitActual] = totalGroupBuildTime;
    }

    @Override
    public long getGroupBuildTime(int ex, int split) {
        return groupBuildTime[ex][split];
    }

    @Override
    public boolean hasErrors() {
        return false;
    }

    @Override
    public long getAggregateBuildTime() {
        MeanIterative meanValue = new MeanIterative();
        for (long[] executionTimes : buildTimes) {
            for (long executionSplitTime : executionTimes) {
                meanValue.addValue(executionSplitTime);
            }
        }
        return (long) meanValue.getMean();
    }

    @Override
    public long getAggregateGroupBuildTime() {
        MeanIterative meanValue = new MeanIterative();
        for (long[] executionTimes : groupBuildTime) {
            for (long executionSplitTime : executionTimes) {
                meanValue.addValue(executionSplitTime);
            }
        }
        return (long) meanValue.getMean();
    }

    @Override
    public long getAggregateRecommendationTime() {
        MeanIterative meanValue = new MeanIterative();
        for (long[] executionTimes : recommendationTimes) {
            for (long executionSplitTime : executionTimes) {
                meanValue.addValue(executionSplitTime);
            }
        }
        return (long) meanValue.getMean();
    }

    /**
     * Devuelve la semilla usada en este caso de estudio.
     *
     * @return
     */
    @Override
    public long getSeedValue() {
        return (Long) getParameterValue(SEED);
    }

    @Override
    public void setSeedValue(long seedValue) {
        setParameterValue(SEED, seedValue);

        if (datasetLoader instanceof SeedHolder) {
            SeedHolder seedHolder = (SeedHolder) datasetLoader;
            seedHolder.setSeedValue(getSeedValue());
        }

        if (groupRecommenderSystem instanceof SeedHolder) {
            SeedHolder seedHolder = (SeedHolder) groupRecommenderSystem;
            seedHolder.setSeedValue(getSeedValue());
        }

        groupFormationTechnique.setSeedValue(getSeedValue());
        groupValidationTechnique.setSeedValue(getSeedValue());
        groupPredictionProtocol.setSeedValue(getSeedValue());
    }

    public void loadDataset(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadContentDataset, CannotLoadTrustDataset, CannotLoadRatingsDataset, CannotLoadUsersDataset {
        setExperimentProgress("Loading dataset", 0, -1);

        {
            setExperimentProgress("Loading ratings dataset", 1, -1);
            datasetLoader.getRatingsDataset();
            setExperimentProgress("Finished loading ratings dataset", 100, -1);
        }
        if (datasetLoader instanceof UsersDatasetLoader) {
            UsersDatasetLoader usersDatasetLoader = (UsersDatasetLoader) datasetLoader;

            setExperimentProgress("Loading users dataset", 0, -1);
            usersDatasetLoader.getUsersDataset();
            setExperimentProgress("Finished loading users dataset", 100, -1);
        }
        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;

            setExperimentProgress("Loading content dataset", 0, -1);
            contentDatasetLoader.getContentDataset();
            setExperimentProgress("Finished loading content dataset", 100, -1);
        }
        if (datasetLoader instanceof TrustDatasetLoader) {
            TrustDatasetLoader trustDatasetLoader = (TrustDatasetLoader) datasetLoader;

            setExperimentProgress("Loading trust dataset", 0, -1);
            trustDatasetLoader.getTrustDataset();
            setExperimentProgress("Finished loading trust dataset", 100, -1);
        }
        setExperimentProgress("Finished loading ratings dataset", 100, -1);
    }

    /**
     * Establezco la nueva semilla para todos los elementos que trabajan con
     * valores aleatorios
     *
     * @param loop Vuelta actual
     */
    private void setNextSeedToSeedHolders(int loop) {

        long seedValue = getSeedValue() + loop;

        if (groupRecommenderSystem instanceof SeedHolder) {
            SeedHolder seedHolder = (SeedHolder) groupRecommenderSystem;
            seedHolder.setSeedValue(seedValue);
            Global.showInfoMessage("Reset GRS seed to " + seedHolder.getSeedValue() + "\n");

        }

        groupFormationTechnique.setSeedValue(seedValue);
        Global.showInfoMessage("Reset groupFormationTechnique seed to " + groupFormationTechnique.getSeedValue() + "\n");

        groupValidationTechnique.setSeedValue(seedValue);
        Global.showInfoMessage("Reset groupValidationTechnique seed to " + groupValidationTechnique.getSeedValue() + "\n");

        groupPredictionProtocol.setSeedValue(seedValue);
        Global.showInfoMessage("Reset groupPredictionProtocol seed to " + groupPredictionProtocol.getSeedValue() + "\n");
    }
}
