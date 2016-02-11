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
package delfos.group.casestudy.defaultcase;

import delfos.common.Chronometer;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadTrustDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.TrustDatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.experiment.ExperimentAdapter;
import delfos.experiment.SeedHolder;
import static delfos.experiment.SeedHolder.SEED;
import delfos.experiment.casestudy.CaseStudyParameterChangedListener;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationFunction;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskInput;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskOutput;
import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.groupformation.GroupFormationTechniqueProgressListener_default;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.experiment.validation.predictionvalidation.GroupRecommendationRequest;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;
import delfos.group.experiment.validation.validationtechniques.GroupValidationTechnique;
import delfos.group.experiment.validation.validationtechniques.HoldOutGroupRatedItems;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.RandomGroupRecommender;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.rs.RecommenderSystemBuildingProgressListener_default;
import delfos.rs.recommendation.Recommendation;
import delfos.utils.algorithm.progress.ProgressChangedController;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Clase para ejecutar un caso de estudio de sistemas de recomendación a grupos.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 20-Mayo-2013
 */
public class GroupCaseStudy extends ExperimentAdapter {

    private static final long serialVersionUID = 1L;

    /*------------------- DATA VALIDATION PARAMETERS -------------------------*/
    /**
     * Parameter to store the dataset used in this case study
     */
    public static final Parameter DATASET_LOADER = new Parameter(
            DatasetLoader.class.getSimpleName(),
            new ParameterOwnerRestriction(DatasetLoader.class, new ConfiguredDatasetLoader("ml-100k")));

    public static final Parameter GROUP_FORMATION_TECHNIQUE = new Parameter(
            GroupFormationTechnique.class.getSimpleName(),
            new ParameterOwnerRestriction(GroupFormationTechnique.class, new FixedGroupSize_OnlyNGroups(2, 2)));

    public static final Parameter GROUP_VALIDATION_TECHNIQUE = new Parameter(
            GroupValidationTechnique.class.getSimpleName(),
            new ParameterOwnerRestriction(GroupValidationTechnique.class, new HoldOutGroupRatedItems()));

    public static final Parameter GROUP_PREDICTION_PROTOCOL = new Parameter(
            GroupPredictionProtocol.class.getSimpleName(),
            new ParameterOwnerRestriction(GroupPredictionProtocol.class, new NoPredictionProtocol()));

    /*------------------------- TECHNIQUE PARAMETERS -------------------------*/
    public static final Parameter GROUP_RECOMMENDER_SYSTEM = new Parameter(
            GroupRecommenderSystem.class.getSimpleName(),
            new ParameterOwnerRestriction(GroupRecommenderSystem.class, new RandomGroupRecommender()));

    public static final Parameter NUM_EXECUTIONS = new Parameter(
            "numExecutions",
            new IntegerParameter(1, 100000000, 1));

    protected final LinkedList<CaseStudyParameterChangedListener> propertyListeners = new LinkedList<>();

    public GroupCaseStudy() {
        addParameter(SEED);
        addParameter(NUM_EXECUTIONS);

        addParameter(DATASET_LOADER);

        addParameter(GROUP_FORMATION_TECHNIQUE);
        addParameter(GROUP_VALIDATION_TECHNIQUE);
        addParameter(GROUP_PREDICTION_PROTOCOL);
        addParameter(GROUP_RECOMMENDER_SYSTEM);
    }

    public GroupCaseStudy(DatasetLoader<? extends Rating> datasetLoader) {
        this();

        setParameterValue(DATASET_LOADER, datasetLoader);
        setAlias(datasetLoader.getAlias());
    }

    public GroupCaseStudy(DatasetLoader<? extends Rating> datasetLoader,
            GroupRecommenderSystem<? extends Object, ? extends Object> groupRecommenderSystem,
            GroupFormationTechnique groupFormationTechnique,
            GroupValidationTechnique groupValidationTechnique, GroupPredictionProtocol groupPredictionProtocol,
            Collection<GroupEvaluationMeasure> groupEvaluationMeasures,
            RelevanceCriteria relevanceCriteria,
            int numExecutions) {

        this();

        setParameterValue(NUM_EXECUTIONS, numExecutions);

        setParameterValue(GROUP_FORMATION_TECHNIQUE, groupFormationTechnique);
        setParameterValue(GROUP_VALIDATION_TECHNIQUE, groupValidationTechnique);
        setParameterValue(GROUP_PREDICTION_PROTOCOL, groupPredictionProtocol);
        setParameterValue(GROUP_RECOMMENDER_SYSTEM, groupRecommenderSystem);

        setAlias(groupRecommenderSystem.getAlias());
    }

    public GroupCaseStudy(DatasetLoader<? extends Rating> datasetLoader,
            GroupRecommenderSystem<? extends Object, ? extends Object> groupRecommenderSystem,
            GroupFormationTechnique groupFormationTechnique,
            GroupValidationTechnique groupValidationTechnique, GroupPredictionProtocol groupPredictionProtocol,
            Collection<GroupEvaluationMeasure> groupEvaluationMeasures,
            RelevanceCriteria relevanceCriteria,
            int numExecutions, long seed) {
        this(datasetLoader, groupRecommenderSystem, groupFormationTechnique, groupValidationTechnique, groupPredictionProtocol, groupEvaluationMeasures, relevanceCriteria, numExecutions);
        setSeedValue(seed);
    }

    /**
     * Devuelve la técnica utilizada para generar los grupos que se evaluarán en
     * el caso de estudio.
     *
     * @return Técnica de formación de grupos utilizada
     */
    public GroupFormationTechnique getGroupFormationTechnique() {
        return (GroupFormationTechnique) getParameterValue(GROUP_FORMATION_TECHNIQUE);
    }

    /**
     * Devuelve la validación que se utiliza en este caso de estudio.
     *
     * @return
     */
    public GroupValidationTechnique getGroupValidationTechnique() {
        return (GroupValidationTechnique) getParameterValue(GROUP_VALIDATION_TECHNIQUE);
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.GROUP_CASE_STUDY;
    }

    public GroupPredictionProtocol getGroupPredictionProtocol() {
        return (GroupPredictionProtocol) getParameterValue(GROUP_PREDICTION_PROTOCOL);
    }

    public int hashDataValidation() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.getSeedValue());
        hash = 97 * hash + Objects.hashCode(this.getDatasetLoader());
        hash = 97 * hash + Objects.hashCode(this.getGroupFormationTechnique());
        hash = 97 * hash + Objects.hashCode(this.getRelevanceCriteria());
        hash = 97 * hash + Objects.hashCode(this.getGroupPredictionProtocol());
        hash = 97 * hash + Objects.hashCode(this.getGroupValidationTechnique());
        return hash;
    }

    public int hashTechnique() {
        return this.getGroupRecommenderSystem().hashCode();
    }

    // ================== VARIABLES QUE NO CAMBIAN EN LA EJECUCION =============
    private final Collection<GroupEvaluationMeasure> groupEvaluationMeasures = GroupEvaluationMeasuresFactory.getInstance().getAllClasses();

    /**
     * Matrix of Map with the results of each evaluation measure. The matrix is
     * indexed by execution and then by split.
     */
    protected Map<GroupEvaluationMeasure, GroupEvaluationMeasureResult>[][] executionsResult;
    protected boolean finished = false;
    private long[][] buildTimes;//executionTimes[ejecucion][conjunto]
    private long[][] recommendationTimes;//executionTimes[ejecucion][conjunto]
    private long[][] groupBuildTime;

    /**
     * Método que realiza la ejecución del caso de estudio, con la configuración
     * que se haya
     *
     * @throws CannotLoadContentDataset
     * @throws CannotLoadRatingsDataset
     * @throws UserNotFound
     * @throws ItemNotFound
     */
    public void execute() throws CannotLoadContentDataset, CannotLoadRatingsDataset, UserNotFound, ItemNotFound {

        final GroupRecommenderSystem groupRecommenderSystem = getGroupRecommenderSystem();
        final GroupFormationTechnique groupFormationTechnique = getGroupFormationTechnique();
        final RelevanceCriteria relevanceCriteria = getRelevanceCriteria();
        final GroupPredictionProtocol groupPredictionProtocol = getGroupPredictionProtocol();
        final GroupValidationTechnique groupValidationTechnique = getGroupValidationTechnique();
        final DatasetLoader<? extends Rating> originalDatasetLoader = getDatasetLoader();

        initGroupCaseStudy();

        int numVueltas = 1;
        int numParticiones = getGroupValidationTechnique().getNumberOfSplits();
        int maxVueltas = numParticiones * getNumExecutions();

        setNumVueltas(maxVueltas);

        executionsResult = new Map[getNumExecutions()][numParticiones];
        for (int execution = 0; execution < getNumExecutions(); execution++) {
            for (int split = 0; split < numParticiones; split++) {
                executionsResult[execution][split] = Collections.synchronizedMap(new TreeMap<>());
            }
        }
        groupRecommenderSystem.addRecommendationModelBuildingProgressListener(new RecommenderSystemBuildingProgressListener_default(System.out, 5000));
        initTimes(getNumExecutions(), numParticiones);

        MeanIterative tiempoParticion = new MeanIterative();
        groupFormationTechnique.addListener(new GroupFormationTechniqueProgressListener_default(System.out, 5000));

        loadDataset(originalDatasetLoader);

        int loopCount = 0;
        setExperimentProgress(getAlias() + " --> Running Case Study Group", 0, -1);
        for (int executionIndex = 0; executionIndex < getNumExecutions(); executionIndex++) {

            final int ejecucionActual = executionIndex;
            setNextSeedToSeedHolders(loopCount);

            Collection<GroupOfUsers> groups = groupFormationTechnique.shuffle(originalDatasetLoader);
            PairOfTrainTestRatingsDataset[] pairsOfTrainTest = groupValidationTechnique.shuffle(originalDatasetLoader, groups);

            for (int thisPartition = 0; thisPartition < pairsOfTrainTest.length; thisPartition++) {

                final int particionActual = thisPartition;

                setNextSeedToSeedHolders(loopCount);

                Chronometer cronometroTiempoParticion = new Chronometer();

                setVueltaActual(ejecucionActual * numParticiones + particionActual);

                setExperimentProgress(getAlias() + " --> Executing (ex " + (ejecucionActual + 1) + "/" + getNumExecutions() + ") (split " + (particionActual + 1) + "/" + numParticiones + ")", ejecucionActual * numParticiones + particionActual, -1);
                setExecutionProgress(getAlias() + " --> Building Recommender System Model", 0, -1);

                DatasetLoader<? extends Rating> trainDatasetLoader = pairsOfTrainTest[particionActual].getTrainingDatasetLoader();
                DatasetLoader<? extends Rating> testDatasetLoader = pairsOfTrainTest[particionActual].getTestDatasetLoader();

                long totalBuildTime;
                Object groupRecommendationModel;
                {
                    Chronometer buildTime = new Chronometer();
                    groupRecommendationModel = groupRecommenderSystem.buildRecommendationModel(trainDatasetLoader);
                    if (groupRecommendationModel == null) {
                        throw new IllegalStateException("The RecommendationModel cannot be null");
                    }

                    long spent = buildTime.getTotalElapsed();
                    setBuildTime(ejecucionActual, particionActual, spent);
                    totalBuildTime = spent;
                }

                Global.showInfoMessage("----------------------- End of Build ----------------------------------" + "\n");

                Global.showInfoMessage("---------------- Init of group recommendation -------------------------" + "\n");
                long totalGroupBuildTime = 0;
                long totalGroupRecommendationTime = 0;

                List<SingleGroupRecommendationTaskInput> taskGroupRecommendationInput = new ArrayList<>(groups.size());
                for (GroupOfUsers groupOfUsers : groups) {
                    for (GroupRecommendationRequest groupRecommendationRequest : groupPredictionProtocol.getGroupRecommendationRequests(trainDatasetLoader, testDatasetLoader, groupOfUsers)) {

                        taskGroupRecommendationInput.add(new SingleGroupRecommendationTaskInput(
                                groupRecommenderSystem,
                                groupRecommendationRequest.predictionPhaseDatasetLoader,
                                groupRecommendationModel,
                                groupOfUsers,
                                groupRecommendationRequest.itemsToPredict)
                        );
                    }
                }

                taskGroupRecommendationInput.parallelStream().forEach(task -> {

                    Set<Integer> groupRequests = task.getItemsRequested();
                    GroupOfUsers groupOfUsers = task.getGroupOfUsers();

                    if (groupRequests == null) {
                        throw new IllegalArgumentException("Group request for group '" + groupOfUsers.toString() + "' are null.");
                    }
                    if (groupRequests.isEmpty()) {
                        throw new IllegalArgumentException("Group request for group '" + groupOfUsers.toString() + "' are empty.");
                    }
                });

                final ProgressChangedController recommendationProgress = new ProgressChangedController(
                        "Group recommendation",
                        taskGroupRecommendationInput.size(),
                        this::setExecutionProgress);

                List<SingleGroupRecommendationTaskOutput> taskGroupRecommendationOutput = taskGroupRecommendationInput
                        .parallelStream()
                        .map(new SingleGroupRecommendationFunction())
                        .map(output -> {
                            recommendationProgress.setTaskFinished();
                            return output;
                        })
                        .collect(Collectors.toList());

                taskGroupRecommendationOutput.parallelStream().forEach(task -> {

                    GroupOfUsers groupOfUsers = task.getGroup();
                    final Collection<Recommendation> groupRecommendations = task.getRecommendations();

                    if (groupRecommendations == null) {
                        throw new IllegalStateException("Group recommendations for group '" + groupOfUsers.toString() + "'" + getAlias() + " --> Cannot recommend to group a null recommendations, should be an empty list instead.");
                    }
                });

                setExecutionProgress(getAlias() + " --> Recommendation process", 100, -1);

                if (groupRecommenderSystem instanceof Closeable) {
                    Closeable closeable = (Closeable) groupRecommenderSystem;
                    try {
                        closeable.close();
                    } catch (IOException ex) {
                        Logger.getLogger(GroupCaseStudy.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                //Se realiza el conteo del tiempo consumido en la ejecución
                setGroupBuildTime(ejecucionActual, particionActual, totalGroupBuildTime);
                setRecommendationTime(ejecucionActual, particionActual, totalGroupRecommendationTime);

                tiempoParticion.addValue(cronometroTiempoParticion.getTotalElapsed());

                long tiempoRestanteExperimento = (long) ((maxVueltas - numVueltas) * tiempoParticion.getMean());
                setExperimentProgress(getAlias() + " --> Executing (ex " + (ejecucionActual + 1) + "/" + getNumExecutions() + ") (split " + (particionActual + 1) + "/" + numParticiones + ")", ejecucionActual * numParticiones + particionActual, tiempoRestanteExperimento);
                numVueltas++;

                GroupRecommenderSystemResult groupRecommendationResult
                        = new GroupRecommenderSystemResult(
                                taskGroupRecommendationInput,
                                taskGroupRecommendationOutput,
                                getAlias(), ejecucionActual, particionActual);

                groupEvaluationMeasures.parallelStream().forEach(groupEvaluationMeasure -> {
                    GroupEvaluationMeasureResult groupMeasureResult
                            = groupEvaluationMeasure.getMeasureResult(
                                    groupRecommendationResult,
                                    originalDatasetLoader,
                                    testDatasetLoader.getRatingsDataset(),
                                    relevanceCriteria, trainDatasetLoader, testDatasetLoader);

                    executionsResult[ejecucionActual][particionActual].put(groupEvaluationMeasure, groupMeasureResult);

                });

                loopCount++;
            }
        }

        setExperimentProgress(getAlias() + " --> Waiting for evaluation measures values", 0, -1);

        setExperimentProgress(getAlias() + " --> Finished evaluation measures values", 100, -1);
        //caseStudyProgressChangedFireEvent(100);

        finished = true;
        setExperimentProgress(getAlias() + " --> Experiment execution finished", 100, -1);
    }

    private void initGroupCaseStudy() {
        finished = false;

        setExperimentProgress(getAlias() + " --> Starting Case Study Group", 0, -1);
        setExecutionProgress(getAlias() + " --> Starting Case Study Group", 0, -1);
    }

    /**
     * Devuelve las medidas de evaluación que se aplican a los resultados de
     * ejecución del sistema de recomendación a grupos
     *
     * @return Medidas de evaluación aplicadas
     */
    public Collection<GroupEvaluationMeasure> getEvaluationMeasures() {
        return groupEvaluationMeasures;
    }

    /**
     * Devuelve el resultado de la medida de evaluación de GRS que se indica por
     * parámetro en la ejecución indicada.
     *
     * @param em Medida de evaluación que se consulta.
     * @param numExec Ejecución para la que se consulta el resultado de la
     * medida.
     * @param split
     * @return Objeto que encapsula los resultados de la aplicación de la medida
     * a las recomendaciones del sistema.
     */
    public GroupEvaluationMeasureResult getMeasureResult(GroupEvaluationMeasure em, int numExec, int split) {

        Map<GroupEvaluationMeasure, GroupEvaluationMeasureResult> thisExecution_evaluationMeasuresValues = this.executionsResult[numExec][split];

        if (thisExecution_evaluationMeasuresValues.containsKey(em)) {
            GroupEvaluationMeasureResult groupMeasureResult = thisExecution_evaluationMeasuresValues.get(em);
            return groupMeasureResult;
        } else {
            throw new IllegalArgumentException("The evaluation measure " + em + " has no value.");
        }
    }

    /**
     * Obtiene el número de ejecuciones que se realizan
     *
     * @return Número de ejecuciones.
     */
    public int getNumExecutions() {
        return ((Number) getParameterValue(NUM_EXECUTIONS)).intValue();
    }

    /**
     * Obtiene el sistema de recomendación a grupos que se está utilizando.
     *
     * @return Sistema de recomendación a grupos que se utiliza.
     */
    public GroupRecommenderSystem getGroupRecommenderSystem() {
        return (GroupRecommenderSystem) getParameterValue(GROUP_RECOMMENDER_SYSTEM);
    }

    /**
     * Devuelve el tiempo de ejecución en milisegundos de una partición concreta
     * de una ejecución dada
     *
     * @param execution Ejecución para la que se quiere conocer el tiempo
     * @param split
     * @return tiempo de ejecución en milisegundos
     */
    public long getBuildTime(int execution, int split) {
        return buildTimes[execution][split];
    }

    /**
     * Devuelve el tiempo de recomendación que el algoritmo demoró en la
     * ejecución indicada. Engloba el cálculo de las recomendaciones para todos
     * los datos del conjunto de test.
     *
     * @param execution Ejecución para la que se consulta el tiempo
     * @param split
     * @return Tiempo en milisegundos de la fase de recomendación
     * @throws IllegalArgumentException Si ex es mayor o igual que el número de
     * ejecuciones o es menor que cero.
     *
     */
    public long getRecommendationTime(int execution, int split) {
        return recommendationTimes[execution][split];
    }

    protected void initTimes(int execution, int numParticiones) {
        buildTimes = new long[execution][numParticiones];
        groupBuildTime = new long[execution][numParticiones];
        recommendationTimes = new long[execution][numParticiones];
    }

    protected void setBuildTime(int execution, int particionActual, long time) {
        buildTimes[execution][particionActual] = time;
    }

    protected void setRecommendationTime(int execution, int particionActual, long time) {
        recommendationTimes[execution][particionActual] = time;
    }

    /**
     * Devuelve el DatasetLoader<? extends Rating> que el experimento utiliza
     *
     * @return DatasetLoader<? extends Rating> que el experimento utiliza
     */
    public DatasetLoader<? extends Rating> getDatasetLoader() {
        return (DatasetLoader<? extends Rating>) getParameterValue(DATASET_LOADER);
    }

    /**
     * Obtiene el criterio de relevancia utilizado en el caso de estudio.
     *
     * @return Criterio de relevancia actual.
     */
    public RelevanceCriteria getRelevanceCriteria() {
        return RelevanceCriteria.DEFAULT_RELEVANCE_CRITERIA;
    }

    @Override
    public boolean isFinished() {
        return this.finished;
    }

    //TODO: Poner una función que
    /**
     * Método para asignar al caso de estudio el resultado calculado de una
     * medida de evaluación sobre una ejecución.
     *
     * @param ejecucion Ejecución a la que se refiere el resultado.
     * @param split
     * @param e Medida de evaluación
     * @param groupMeasureResult Resultado de la medida
     * @deprecated Está previsto eliminar este método.
     */
    public void putResult(int ejecucion, int split, GroupEvaluationMeasure e, GroupEvaluationMeasureResult groupMeasureResult) {
        executionsResult[ejecucion][split].put(e, groupMeasureResult);
    }

    protected void setGroupBuildTime(int ejecucionActual, int splitActual, long totalGroupBuildTime) {
        groupBuildTime[ejecucionActual][splitActual] = totalGroupBuildTime;
    }

    /**
     * Devuelve el tiempo empleado en la construcción del modelo de los grupos
     * ya formados.
     *
     * @param ex
     * @param split
     * @return tiempo en milisegundos.
     */
    public long getGroupBuildTime(int ex, int split) {
        return groupBuildTime[ex][split];
    }

    @Override
    public boolean hasErrors() {
        return false;
    }

    /**
     * Devuelve el tiempo medio de construcción del modelo de recomendación en
     * milisegundos.
     *
     * @return
     */
    public long getAggregateBuildTime() {
        MeanIterative meanValue = new MeanIterative();
        for (long[] executionTimes : buildTimes) {
            for (long executionSplitTime : executionTimes) {
                meanValue.addValue(executionSplitTime);
            }
        }
        return (long) meanValue.getMean();
    }

    /**
     * Devuelve el tiempo medio de construcción de todos los perfiles de grupo
     * en milisegundos.
     *
     * @return
     */
    public long getAggregateGroupBuildTime() {
        MeanIterative meanValue = new MeanIterative();
        for (long[] executionTimes : groupBuildTime) {
            for (long executionSplitTime : executionTimes) {
                meanValue.addValue(executionSplitTime);
            }
        }
        return (long) meanValue.getMean();
    }

    /**
     * Devuelve el tiempo medio de cálculo de todas las recomendaciones en
     * milisegundos.
     *
     * @return
     */
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
    public final void setSeedValue(long seedValue) {
        setParameterValue(SEED, seedValue);

        final GroupRecommenderSystem groupRecommenderSystem = getGroupRecommenderSystem();
        final GroupFormationTechnique groupFormationTechnique = getGroupFormationTechnique();
        final GroupPredictionProtocol groupPredictionProtocol = getGroupPredictionProtocol();
        final GroupValidationTechnique groupValidationTechnique = getGroupValidationTechnique();
        final DatasetLoader<? extends Rating> datasetLoader = getDatasetLoader();

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
        final GroupRecommenderSystem groupRecommenderSystem = getGroupRecommenderSystem();
        final GroupFormationTechnique groupFormationTechnique = getGroupFormationTechnique();
        final RelevanceCriteria relevanceCriteria = getRelevanceCriteria();
        final GroupPredictionProtocol groupPredictionProtocol = getGroupPredictionProtocol();
        final GroupValidationTechnique groupValidationTechnique = getGroupValidationTechnique();
        final DatasetLoader<? extends Rating> datasetLoader = getDatasetLoader();

        final long caseStudySeed = getSeedValue();
        long thisLoopSeed = caseStudySeed + loop;

        if (groupRecommenderSystem instanceof SeedHolder) {
            SeedHolder seedHolder = (SeedHolder) groupRecommenderSystem;
            seedHolder.setSeedValue(thisLoopSeed);
            Global.showInfoMessage("Reset GRS seed to " + seedHolder.getSeedValue() + "\n");

        }
        if (datasetLoader instanceof SeedHolder) {
            SeedHolder seedHolder = (SeedHolder) datasetLoader;
            seedHolder.setSeedValue(thisLoopSeed);
            Global.showInfoMessage("Reset dataset seed to " + seedHolder.getSeedValue() + "\n");

        }

        groupFormationTechnique.setSeedValue(thisLoopSeed);
        Global.showInfoMessage("Reset groupFormationTechnique seed to " + groupFormationTechnique.getSeedValue() + "\n");

        groupValidationTechnique.setSeedValue(thisLoopSeed);
        Global.showInfoMessage("Reset groupValidationTechnique seed to " + groupValidationTechnique.getSeedValue() + "\n");

        groupPredictionProtocol.setSeedValue(thisLoopSeed);
        Global.showInfoMessage("Reset groupPredictionProtocol seed to " + groupPredictionProtocol.getSeedValue() + "\n");
    }

    private Map<GroupEvaluationMeasure, GroupEvaluationMeasureResult> groupEvaluationMeasuresResults = null;

    /**
     * Devuelve el resultado agregado de la medida de evaluación de GRS que se
     * indica por parámetro.
     *
     * @param em Medida de evaluación que se consulta.
     * @return Objeto que encapsula los resultados de la aplicación de la medida
     * a las recomendaciones del sistema.
     */
    public GroupEvaluationMeasureResult getAggregateMeasureResult(GroupEvaluationMeasure em) {

        if (groupEvaluationMeasuresResults != null) {
            if (groupEvaluationMeasuresResults.containsKey(em)) {
                return groupEvaluationMeasuresResults.get(em);
            }
        }

        List<GroupEvaluationMeasureResult> measureResult = new ArrayList<>();
        for (int execution = 0; execution < getNumExecutions(); execution++) {
            for (int split = 0; split < getGroupValidationTechnique().getNumberOfSplits(); split++) {
                measureResult.add(getMeasureResult(em, execution, split));
            }

        }
        return em.agregateResults(measureResult);
    }

    public void setAggregateResults(Map<GroupEvaluationMeasure, GroupEvaluationMeasureResult> groupEvaluationMeasuresResults) {
        this.groupEvaluationMeasuresResults = groupEvaluationMeasuresResults;
    }

    public long[][] getBuildTimes() {
        return buildTimes;
    }

    public void setGroupRecommenderSystem(GroupRecommenderSystem<? extends Object, ? extends Object> groupRecommenderSystem) {
        setParameterValue(GROUP_RECOMMENDER_SYSTEM, groupRecommenderSystem);
    }
}
