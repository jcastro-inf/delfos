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
import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;
import delfos.group.experiment.validation.validationtechniques.GroupValidationTechnique;
import delfos.group.experiment.validation.validationtechniques.HoldOutGroupRatedItems;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.RandomGroupRecommender;
import delfos.group.io.excel.casestudy.GroupCaseStudyExcel;
import delfos.group.io.xml.casestudy.GroupCaseStudyXML;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import delfos.utils.algorithm.progress.ProgressChangedController;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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

    private File resultsDirectory = new File("." + File.separator + "temp");

    public void setResultsDirectory(File RESULTS_DIRECTORY) {
        if (RESULTS_DIRECTORY.exists() && !RESULTS_DIRECTORY.isDirectory()) {
            throw new IllegalStateException("Must be a directory");
        }
        this.resultsDirectory = RESULTS_DIRECTORY;
    }

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
            int numExecutions, long seed) {
        this(datasetLoader, groupRecommenderSystem, groupFormationTechnique, groupValidationTechnique, groupPredictionProtocol, groupEvaluationMeasures, relevanceCriteria, numExecutions);
        setSeedValue(seed);
    }

    public GroupCaseStudy(DatasetLoader<? extends Rating> datasetLoader,
            GroupRecommenderSystem<? extends Object, ? extends Object> groupRecommenderSystem,
            GroupFormationTechnique groupFormationTechnique,
            GroupValidationTechnique groupValidationTechnique,
            GroupPredictionProtocol groupPredictionProtocol,
            Collection<GroupEvaluationMeasure> groupEvaluationMeasures,
            RelevanceCriteria relevanceCriteria,
            int numExecutions) {

        this();

        setParameterValue(DATASET_LOADER, datasetLoader);
        setParameterValue(NUM_EXECUTIONS, numExecutions);

        setParameterValue(GROUP_FORMATION_TECHNIQUE, groupFormationTechnique);
        setParameterValue(GROUP_VALIDATION_TECHNIQUE, groupValidationTechnique);
        setParameterValue(GROUP_PREDICTION_PROTOCOL, groupPredictionProtocol);
        setParameterValue(GROUP_RECOMMENDER_SYSTEM, groupRecommenderSystem);

        setAlias(groupRecommenderSystem.getAlias());
    }

    /**
     * First index is the number of execution, the second is the number of
     * split.
     */
    private Map<Integer, Map<Integer, Map<GroupEvaluationMeasure, GroupEvaluationMeasureResult>>> allLoopsResults;
    private Map<GroupEvaluationMeasure, GroupEvaluationMeasureResult> aggregateResults;

    public void execute() throws CannotLoadContentDataset, CannotLoadRatingsDataset, UserNotFound, ItemNotFound {
        final DatasetLoader<? extends Rating> originalDatasetLoader = getDatasetLoader();
        int numberOfExecutionSplits = getNumSplits() * getNumExecutions();
        setNumVueltas(numberOfExecutionSplits);
        loadDataset(originalDatasetLoader);

        ProgressChangedController groupCaseStudyProgressChangedController = new ProgressChangedController(
                getAlias(),
                numberOfExecutionSplits,
                this::setExperimentProgress);

        allLoopsResults = IntStream.range(0, getNumExecutions()).boxed().parallel().collect(Collectors.toMap(Function.identity(),
                execution -> {
                    long loopSeedForValidation = getLoopSeed(execution, 0);

                    GroupFormationTechnique groupFormationTechnique = (GroupFormationTechnique) getGroupFormationTechnique().clone();
                    GroupValidationTechnique groupValidationTechnique = (GroupValidationTechnique) getGroupValidationTechnique().clone();

                    groupFormationTechnique.setSeedValue(loopSeedForValidation);
                    groupValidationTechnique.setSeedValue(loopSeedForValidation);

                    Collection<GroupOfUsers> groups = groupFormationTechnique.shuffle(originalDatasetLoader);
                    PairOfTrainTestRatingsDataset[] pairsOfTrainTest = groupValidationTechnique.shuffle(originalDatasetLoader, groups);

                    Map<Integer, Map<GroupEvaluationMeasure, GroupEvaluationMeasureResult>> resultsThisExecution = IntStream.range(0, getNumSplits()).boxed().parallel().collect(Collectors.toMap(Function.identity(),
                                    split -> {
                                        Map<GroupEvaluationMeasure, GroupEvaluationMeasureResult> execute = new ExecutionExplitConsumer(execution, split, this, pairsOfTrainTest, groups).execute();
                                        groupCaseStudyProgressChangedController.setTaskFinished();
                                        return execute;
                                    }
                            )
                    );

                    setResultsThisExecution(execution, resultsThisExecution);

                    return resultsThisExecution;
                })
        );

        Set<GroupEvaluationMeasure> groupEvaluationMeasures = allLoopsResults.get(0).get(0).keySet();

        aggregateResults = groupEvaluationMeasures.parallelStream().collect(Collectors.toMap(Function.identity(),
                groupEvaluationMeasure -> {

                    List<GroupEvaluationMeasureResult> allResultsThisMeasure
                    = allLoopsResults.values().parallelStream()
                    .flatMap(resultsForThisExecution -> resultsForThisExecution.values().stream())
                    .map(resultExecutionSplit -> resultExecutionSplit.get(groupEvaluationMeasure))
                    .collect(Collectors.toList());

                    GroupEvaluationMeasureResult resultsAggregated
                    = groupEvaluationMeasure.agregateResults(allResultsThisMeasure);

                    return resultsAggregated;
                }));

        setExecutionProgress(getAlias(), 100, 0);
    }

    protected long getLoopSeed(int execution, int split) {
        long caseStudySeed = getSeedValue();
        long offsetExecutionSplit = execution * getNumSplits() + split;
        long finalSeed = caseStudySeed + offsetExecutionSplit;
        return finalSeed;
    }

    protected void setSeedToSeedHolders(long seed) {
        final GroupFormationTechnique groupFormationTechnique = getGroupFormationTechnique();
        final GroupPredictionProtocol groupPredictionProtocol = getGroupPredictionProtocol();
        final GroupValidationTechnique groupValidationTechnique = getGroupValidationTechnique();

        groupFormationTechnique.setSeedValue(seed);
        groupValidationTechnique.setSeedValue(seed);
        groupPredictionProtocol.setSeedValue(seed);
    }

    /**
     * Number of executions in this caseStudy.
     *
     * @return
     */
    public int getNumExecutions() {
        return ((Number) getParameterValue(NUM_EXECUTIONS)).intValue();
    }

    /**
     * Returns the number of splits per execution in this caseStudy, therefore
     * the case study evaluates the GRS on numExecutions* numSplits different
     * datasets.
     *
     * @return
     */
    public int getNumSplits() {
        return getGroupValidationTechnique()
                .getNumberOfSplits();
    }

    public GroupRecommenderSystem getGroupRecommenderSystem() {
        return (GroupRecommenderSystem) getParameterValue(GROUP_RECOMMENDER_SYSTEM);
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
        return this.aggregateResults != null && this.allLoopsResults != null;
    }

    @Override
    public boolean hasErrors() {
        return false;
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

    public static final Object exmutLoadDatasetsOnceAtATime = 1;

    private void loadDataset(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadContentDataset, CannotLoadTrustDataset, CannotLoadRatingsDataset, CannotLoadUsersDataset {

        synchronized (exmutLoadDatasetsOnceAtATime) {
            final String taskName = "Loading dataset '" + datasetLoader.getAlias() + "'";
            setExperimentProgress(taskName, 0, -1);

            {
                setExperimentProgress(taskName + "  ratings dataset", 1, -1);
                datasetLoader.getRatingsDataset();
                setExperimentProgress("Finished loading ratings dataset", 100, -1);
            }
            if (datasetLoader instanceof UsersDatasetLoader) {
                UsersDatasetLoader usersDatasetLoader = (UsersDatasetLoader) datasetLoader;

                setExperimentProgress(taskName + "  users dataset", 0, -1);
                usersDatasetLoader.getUsersDataset();
                setExperimentProgress("Finished loading users dataset", 100, -1);
            }
            if (datasetLoader instanceof ContentDatasetLoader) {
                ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;

                setExperimentProgress(taskName + "  items dataset", 0, -1);
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
    }

    public GroupEvaluationMeasureResult getMeasureResult(GroupEvaluationMeasure groupEvaluationMeasure, int execution, int split) {
        Map<Integer, Map<GroupEvaluationMeasure, GroupEvaluationMeasureResult>> executionResults = this.allLoopsResults.get(execution);
        Map<GroupEvaluationMeasure, GroupEvaluationMeasureResult> splitResults = executionResults.get(split);
        GroupEvaluationMeasureResult groupEvaluationMeasureResult = splitResults.get(groupEvaluationMeasure);
        return groupEvaluationMeasureResult;
    }

    public GroupEvaluationMeasureResult getAggregateMeasureResult(GroupEvaluationMeasure em) {
        GroupEvaluationMeasureResult groupEvaluationMeasureResult = aggregateResults.get(em);
        return groupEvaluationMeasureResult;
    }

    public void setAggregateResults(Map<GroupEvaluationMeasure, GroupEvaluationMeasureResult> groupEvaluationMeasuresResults) {
        this.aggregateResults = groupEvaluationMeasuresResults;
    }

    public GroupCaseStudy setDatasetLoader(DatasetLoader<? extends Rating> datasetLoader) {
        setParameterValue(DATASET_LOADER, datasetLoader);
        return this;
    }

    public GroupCaseStudy setGroupRecommenderSystem(GroupRecommenderSystem<? extends Object, ? extends Object> groupRecommenderSystem) {
        setParameterValue(GROUP_RECOMMENDER_SYSTEM, groupRecommenderSystem);
        return this;
    }

    public GroupCaseStudy setGroupFormationTechnique(GroupFormationTechnique groupFormationTechnique) {
        setParameterValue(GROUP_FORMATION_TECHNIQUE, groupFormationTechnique);
        return this;
    }

    public GroupCaseStudy setGroupValidationTechnique(GroupValidationTechnique groupValidationTechnique) {
        setParameterValue(GROUP_VALIDATION_TECHNIQUE, groupValidationTechnique);
        return this;

    }

    public GroupCaseStudy setGroupPredictionProtocol(GroupPredictionProtocol groupPredictionProtocol) {
        setParameterValue(GROUP_PREDICTION_PROTOCOL, groupPredictionProtocol);
        return this;

    }

    public GroupCaseStudy setNumExecutions(int numExecutions) {
        setParameterValue(NUM_EXECUTIONS, numExecutions);
        return this;
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

    public Iterable<GroupEvaluationMeasure> getEvaluationMeasures() {
        if (aggregateResults != null) {
            return aggregateResults.keySet();
        } else {
            return GroupEvaluationMeasuresFactory.getInstance().getAllClasses();
        }
    }

    @Override
    public GroupCaseStudy clone() {
        return (GroupCaseStudy) super.clone();
    }

    private final Map<Integer, Map<Integer, Map<GroupEvaluationMeasure, GroupEvaluationMeasureResult>>> resultsAsTheyFinish
            = new TreeMap<>();

    private synchronized void setResultsThisExecution(Integer newExecution, Map<Integer, Map<GroupEvaluationMeasure, GroupEvaluationMeasureResult>> resultsNewExecution) {

        Set<Integer> executionsHoles = IntStream.range(0, this.getNumExecutions()).boxed().sorted().collect(Collectors.toSet());
        executionsHoles.removeAll(resultsAsTheyFinish.keySet());

        int firstHole = executionsHoles.stream().mapToInt(i -> i).min().orElse(-1);

        resultsAsTheyFinish.put(newExecution, resultsNewExecution);
        if (newExecution != firstHole) {
            //lo guardo y punto.
            return;
        }

        executionsHoles.remove(newExecution);
        int nextHole = executionsHoles.stream().mapToInt(i -> i).min().orElse(this.getNumExecutions());

        List<Integer> toSave = IntStream.range(firstHole, nextHole).boxed().sorted().collect(Collectors.toList());

        toSave.stream().forEach(execution -> {

            GroupCaseStudy groupCaseStudyCloned = this.clone();

            groupCaseStudyCloned.setNumExecutions(execution + 1);

            groupCaseStudyCloned.allLoopsResults
                    = resultsAsTheyFinish.entrySet().stream()
                    .filter(entry -> entry.getKey() <= execution)
                    .collect(Collectors.toMap(
                                    entry -> entry.getKey(),
                                    entry -> entry.getValue()
                            )
                    );

            Set<GroupEvaluationMeasure> groupEvaluationMeasures = groupCaseStudyCloned.allLoopsResults.get(0).get(0).keySet();

            groupCaseStudyCloned.aggregateResults = groupEvaluationMeasures.parallelStream().collect(Collectors.toMap(Function.identity(),
                    groupEvaluationMeasure -> {

                        List<GroupEvaluationMeasureResult> allResultsThisMeasure
                        = groupCaseStudyCloned.allLoopsResults.values().parallelStream()
                        .flatMap(resultsForThisExecution -> resultsForThisExecution.values().stream())
                        .map(resultExecutionSplit -> resultExecutionSplit.get(groupEvaluationMeasure))
                        .collect(Collectors.toList());

                        GroupEvaluationMeasureResult resultsAggregated
                        = groupEvaluationMeasure.agregateResults(allResultsThisMeasure);

                        return resultsAggregated;
                    }));

            GroupCaseStudyXML.saveCaseResults(groupCaseStudyCloned, resultsDirectory);
            GroupCaseStudyExcel.saveCaseResults(groupCaseStudyCloned, resultsDirectory);

        });

    }
}
