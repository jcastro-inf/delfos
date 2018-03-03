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
package delfos.experiment.casestudy;

import delfos.casestudy.defaultcase.ExecutionSplitDescriptor;
import delfos.casestudy.parallelisation.ExecutionSplitConsumer;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadTrustDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterListener;
import delfos.common.parameters.ParameterOwnerType;
import delfos.common.parameters.restriction.DoubleParameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.common.parameters.restriction.StringParameter;
import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.TrustDatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.ExperimentAdapter;
import delfos.experiment.ExperimentListener;
import delfos.experiment.SeedHolder;
import delfos.experiment.validation.predictionprotocol.NoPredictionProtocol;
import delfos.experiment.validation.predictionprotocol.PredictionProtocol;
import delfos.experiment.validation.validationtechnique.NoPartitions;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.factories.EvaluationMeasuresFactory;
import delfos.io.excel.casestudy.CaseStudyExcel;
import delfos.io.xml.casestudy.CaseStudyXML;
import delfos.results.MeasureResult;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.RecommendationModelBuildingProgressListener;
import delfos.rs.RecommenderSystem;
import delfos.rs.nonpersonalised.randomrecommender.RandomRecommender;
import delfos.utils.algorithm.progress.ProgressChangedController;
import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Clase encargada de realizar las ejecuciones de los sistemas de recomendación tradicionales, es decir, single user,
 * recomendando conjuntos de productos a un usuario utilizando datos de usuario, datos de productos y valoraciones. Se
 * encarga realizar el proceso completo de prueba de un sistema de recomendación: invoca al método de validación,
 * invocar al metodo buildRecommendationModel del sistema de recomendación, realiza la petición de recomendaciones y
 * recoge los resultados, almacenando el tiempo de ejecución y llamando a las métricas de evaluación.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @param <RecommendationModel>
 * @param <RatingType>
 */
public class CaseStudy<RecommendationModel extends Object, RatingType extends Rating>
        extends ExperimentAdapter
        implements ParameterListener, RecommendationModelBuildingProgressListener {

    /**
     * Parameter to store the dataset used in this case study
     */
    public static final Parameter DATASET_LOADER = new Parameter(
            DatasetLoader.class.getSimpleName(),
            new ParameterOwnerRestriction(DatasetLoader.class, new ConfiguredDatasetLoader("ml-100k")));

    public static final Parameter VALIDATION_TECHNIQUE = new Parameter(
            ValidationTechnique.class.getSimpleName(),
            new ParameterOwnerRestriction(ValidationTechnique.class, new NoPartitions()));

    public static final Parameter PREDICTION_PROTOCOL = new Parameter(
            PredictionProtocol.class.getSimpleName(),
            new ParameterOwnerRestriction(PredictionProtocol.class, new NoPredictionProtocol()));

    /*------------------------- TECHNIQUE PARAMETERS -------------------------*/
    public static final Parameter RECOMMENDER_SYSTEM = new Parameter(
            RecommenderSystem.class.getSimpleName(),
            new ParameterOwnerRestriction(RecommenderSystem.class, new RandomRecommender()));

    public static final Parameter NUM_EXECUTIONS = new Parameter(
            "numExecutions",
            new IntegerParameter(1, 100000000, 1));

    public static final Parameter EVALUATION_MEASURES_AS_STRING = new Parameter(
            "EVALUATION_MEASURES_AS_STRING",
            new StringParameter(EvaluationMeasuresFactory.getInstance().getAllClasses().toString()));

    public static final Parameter RELEVANCE_CRITERIA  = new Parameter(
            "RELEVANCE_CRITERIA",
            new DoubleParameter(-1000,1000,4.0)
    );

    protected void initParameters() {
        addParameter(SEED);
        addParameter(NUM_EXECUTIONS);
        addParameter(DATASET_LOADER);
        addParameter(VALIDATION_TECHNIQUE);
        addParameter(PREDICTION_PROTOCOL);
        addParameter(RECOMMENDER_SYSTEM);
        addParameter(EVALUATION_MEASURES_AS_STRING);
        addParameter(RELEVANCE_CRITERIA);
    }

    protected final ArrayList<CaseStudyParameterChangedListener> propertyListeners = new ArrayList<>();
    protected final ArrayList<ExperimentListener> experimentProgressListeners = new ArrayList<>();
    protected boolean running;
    protected boolean finished = false;
    protected int _ejecucionActual;
    protected int _conjuntoActual;

    protected Map<Integer, Map<Integer, Map<EvaluationMeasure, MeasureResult>>> allLoopsResults = Collections
            .synchronizedMap(new HashMap<>());

    protected boolean errors = false;

    protected Map<EvaluationMeasure, MeasureResult> aggregateResults;
    protected File resultsDirectory = new File("." + File.separator + "temp");

    public void setResultsDirectory(File RESULTS_DIRECTORY) {
        if (RESULTS_DIRECTORY.exists() && !RESULTS_DIRECTORY.isDirectory()) {
            throw new IllegalStateException("Must be a directory");
        }
        this.resultsDirectory = RESULTS_DIRECTORY;
    }

    public CaseStudy(){
        initParameters();
    }

    /**
     * Constructor del caso de uso. Recoge la configuración del mismo por los parámetros y los almacena en la clase
     * creada. La ejecución del mismo se realizará posteriormente con el método <b>execute()</b>.
     * <p>
     * <b>Nota:</b>
     * El número de ejecuciones se asigna en el caso de uso, pero el número de particiones del conjunto de datos que se
     * realiza en cada ejecución lo determina la clase que implementa <code>{@link ValidationTechnique}</code>
     *  @param recommenderSystem Sistema de recomendación que será probado en el caso de uso. Los parámetros del mismo
     * deben haber sido asignados al objeto con anterioridad mediante sus métodos <b>setParameter(parameter)</b>
     * @param datasetLoader
     * @param validationTechnique Método de validación que se usará para la división de los datasets en training y test.
 * Tras cada ejecución se llama al método
 * <b>shuffle()</b> para que realice una nueva división aleatoria del conjunto.
     * @param predictionProtocol
     * @param relevanceCriteria
     * @param evaluationMeasures Vector con las medidas de evaluación que se aplicarán a los resultados de las
* ejecuciones.
     * @param executionNumber número de veces que se ejecutará la recomendación Cuanto mayor sea el número de
* ejecuciones, más fiel es el valor de las métricas de evaluación, pero se incrementa el tiempo de ejecución.
     */
    public static <RecommendationModel, RatingType extends Rating> CaseStudy<RecommendationModel, RatingType> create(
            RecommenderSystem<RecommendationModel> recommenderSystem,
            DatasetLoader<RatingType> datasetLoader,
            ValidationTechnique validationTechnique,
            PredictionProtocol predictionProtocol,
            RelevanceCriteria relevanceCriteria,
            Collection<EvaluationMeasure> evaluationMeasures,
            int executionNumber) {

        CaseStudy<RecommendationModel, RatingType> caseStudy = new CaseStudy<>();

        caseStudy.setRelevanceCriteria(relevanceCriteria);
        caseStudy.setValidationTechnique(validationTechnique);
        caseStudy.setNumVueltas(validationTechnique.getNumberOfSplits() * executionNumber);
        caseStudy.setRecommenderSystem(recommenderSystem);
        caseStudy.setNumExecutions(executionNumber);
        caseStudy.setEvaluationMeasures(evaluationMeasures);
        caseStudy.setDatasetLoader(datasetLoader);
        caseStudy.setPredictionProtocol(predictionProtocol);
        caseStudy.registerInListeners();
        caseStudy.setAlias(recommenderSystem.getAlias());

        return caseStudy;
    }

    public static <RecommendationModel, RatingType extends Rating> CaseStudy<RecommendationModel, RatingType> create(
            RecommenderSystem<RecommendationModel> recommenderSystem,
            DatasetLoader<RatingType> configuredDatasetLoader) {
        CaseStudy<RecommendationModel, RatingType> caseStudy = new CaseStudy<>();

        caseStudy.setRecommenderSystem(recommenderSystem);
        caseStudy.setDatasetLoader(configuredDatasetLoader);

        caseStudy.setAlias(configuredDatasetLoader.getAlias());

        return caseStudy;
    }

    @Override
    public void execute(){
        finished = false;

        final DatasetLoader<? extends Rating> originalDatasetLoader = getDatasetLoader();
        int numberOfExecutionSplits = getNumSplits() * getNumExecutions();
        setNumVueltas(numberOfExecutionSplits);
        loadDataset(originalDatasetLoader);

        ProgressChangedController groupCaseStudyProgressChangedController = new ProgressChangedController(
                getAlias(),
                numberOfExecutionSplits,
                this::setExperimentProgress);

        Stream<Integer> executionsStream = IntStream.range(0, getNumExecutions()).boxed();

        if (Global.isParallelExecutionSplits()) {
            executionsStream = executionsStream.parallel();
        }

        executionsStream.flatMap(execution -> {
            Stream<Integer> splitsStream = IntStream.range(0, getNumSplits()).boxed();

            if (Global.isParallelExecutionSplits()) {
                splitsStream = splitsStream.parallel();
            }

            return splitsStream
                    .map(split -> {
                        return new ExecutionSplitConsumer(
                                execution,
                                split,
                                this
                        );

                    });
        }).forEach(executionSplit -> {
            ExecutionSplitDescriptor executionSplitDescriptor = executionSplit.getDescriptorAndResults();

            synchronized (allLoopsResults) {
                final int execution = executionSplitDescriptor.getExecution();

                if (!allLoopsResults.containsKey(execution)) {
                    allLoopsResults.put(execution, Collections.synchronizedMap(new HashMap<>()));
                }

                allLoopsResults.get(execution).put(executionSplitDescriptor.getSplit(), executionSplitDescriptor.getResults());

                List<Integer> finishedSplits = allLoopsResults.get(execution).keySet().stream().sorted().collect(Collectors.toList());
                List<Integer> allSplits = IntStream.range(0, getNumSplits()).boxed().sorted().collect(Collectors.toList());

                if (finishedSplits.equals(allSplits)) {
                    setResultsThisExecution(execution, allLoopsResults.get(execution));
                }
            }

            groupCaseStudyProgressChangedController.setTaskFinished();
        });

        setExperimentProgress("Finished execution, computing evaluation measures", 100, -1);

        aggregateResults = getEvaluationMeasures().stream().parallel().collect(Collectors.toMap(Function.identity(),
                groupEvaluationMeasure -> {

                    List<MeasureResult> allResultsThisMeasure
                    = allLoopsResults.values().stream().parallel()
                    .flatMap(resultsForThisExecution -> resultsForThisExecution.values().stream())
                    .map(resultExecutionSplit -> resultExecutionSplit.get(groupEvaluationMeasure))
                    .collect(Collectors.toList());

                    MeasureResult resultsAggregated
                    = groupEvaluationMeasure.agregateResults(allResultsThisMeasure);

                    return resultsAggregated;
                }));

        Global.showInfoMessage("Case study finished\n");
        setRunning(false);
        setFinished();
        setErrors(false);

        executionProgressFireEvent("Case study finished", 100, -1);
    }

    public void loadDataset(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadContentDataset, CannotLoadTrustDataset, CannotLoadRatingsDataset, CannotLoadUsersDataset {
        executionProgressFireEvent("Loading dataset", 0, -1);

        {
            executionProgressFireEvent("Loading ratings dataset", 0, -1);
            datasetLoader.getRatingsDataset();
            executionProgressFireEvent("Finished loading ratings dataset", 0, -1);
        }
        if (datasetLoader instanceof UsersDatasetLoader) {
            UsersDatasetLoader usersDatasetLoader = (UsersDatasetLoader) datasetLoader;

            executionProgressFireEvent("Loading users dataset", 0, -1);
            usersDatasetLoader.getUsersDataset();
            executionProgressFireEvent("Finished loading users dataset", 0, -1);
        }
        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;

            executionProgressFireEvent("Loading content dataset", 0, -1);
            contentDatasetLoader.getContentDataset();
            executionProgressFireEvent("Finished loading content dataset", 0, -1);
        }
        if (datasetLoader instanceof TrustDatasetLoader) {
            TrustDatasetLoader trustDatasetLoader = (TrustDatasetLoader) datasetLoader;

            executionProgressFireEvent("Loading trust dataset", 0, -1);
            trustDatasetLoader.getTrustDataset();
            executionProgressFireEvent("Finished loading trust dataset", 0, -1);
        }
        executionProgressFireEvent("Finished loading ratings dataset", 0, -1);
    }

    public MeasureResult getMeasureResult(EvaluationMeasure em, int execution, int split) {

        Map<Integer, Map<EvaluationMeasure, MeasureResult>> thisExecutionResults = allLoopsResults.get(execution);

        Map<EvaluationMeasure, MeasureResult> thisSplitResults = thisExecutionResults.get(split);

        MeasureResult measureResult = thisSplitResults.get(em);

        return measureResult;
    }

    @Override
    public void parameterChanged() {
        datasetLoaderParameterChanged(getDatasetLoader());
    }

    public void addCaseStudyPropertyListener(CaseStudyParameterChangedListener listener) {
        this.propertyListeners.add(listener);
        listener.caseStudyParameterChanged(this);
    }

    public void removeCaseStudyPropertyChangedListener(CaseStudyParameterChangedListener listener) {
        this.propertyListeners.remove(listener);
    }

    protected void caseStudyPropertyChangedFireEvent() {
        propertyListeners.stream().forEach((listener) -> {
            listener.caseStudyParameterChanged(this);
        });
    }

    protected int totalCaseStudyPercent(double percent) {
        double totalPercent;
        int maxVueltas = getNumExecutions() * getNumSplits();
        int vueltasActual = _ejecucionActual * getNumSplits() + _conjuntoActual;
        totalPercent = (((double) vueltasActual) / (maxVueltas)) * 100;

        double add;
        add = (double) ((percent / 100.0) / maxVueltas) * 100;
        totalPercent += add;

        return (int) totalPercent;
    }

    public void executionProgressFireEvent(
            String executionProgressTask,
            int executionProgressPercent,
            long executionProgressRemainingTime) {

        updateExperimentProgress(executionProgressTask, executionProgressPercent, executionProgressRemainingTime);

        experimentProgressListeners.stream().forEach((listener) -> {
            listener.progressChanged(this);
        });

    }

    @Override
    public void buildingProgressChanged(String actualJob, int percent, long remainingSeconds) {
        //Informa a los listener de la ejecución. Se divide por dos porque se
        //Considera un 50% para el building y un 50% para la recomendación
        executionProgressFireEvent("Building: " + actualJob, percent / 2, remainingSeconds);
    }

    /**
     * Método que devuelve el resultado agregado de una medida de evaluación.
     *
     * @param em Medida de evaluación.
     * @return Resultado de la medida de evaluación.
     */
    public MeasureResult getMeasureResult(EvaluationMeasure em) {
        List<MeasureResult> measureResult = new ArrayList<>();
        getMeasureResult(em, 0, 0);
        for (int i = 0; i < getNumExecutions(); i++) {
            for (int j = 0; j < getNumSplits(); j++) {
                measureResult.add(getMeasureResult(em, i, j));
            }
        }
        return em.agregateResults(measureResult);
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    public boolean isRunning() {
        return running;
    }

    protected void setRunning(boolean state) {
        running = state;
        caseStudyPropertyChangedFireEvent();
    }

    protected void setFinished() {
        finished = true;
        caseStudyPropertyChangedFireEvent();
    }

    public void datasetLoaderParameterChanged(DatasetLoader<? extends Rating> loader) {
        setRunning(false);
    }

    /**
     * Devuelve el número de particiones.
     *
     * @return
     */
    public int getNumSplits() {
        return getValidationTechnique().getNumberOfSplits();
    }
    protected String executionProgressTask = "";
    protected int executionProgressPercent = 0;
    protected long executionProgressRemainingTime = -1;
    protected String experimentProgressTask = "";
    protected int experimentProgressPercent = 0;
    protected long experimentProgressRemainingTime = -1;

    protected void updateExperimentProgress(
            String executionProgressTask,
            int executionProgressPercent,
            long executionProgressRemainingTime) {

        //Primero la ejecución
        this.executionProgressTask = executionProgressTask;
        this.executionProgressPercent = executionProgressPercent;
        this.executionProgressRemainingTime = executionProgressRemainingTime;

        double _experimentProgressPercent = (getNumVueltas() * 100 + executionProgressPercent) / getNumVueltas();

        //Actualizo las variables que luego el listener del experimento pedirá.
        this.experimentProgressTask = this.getAlias();
        this.experimentProgressPercent = (int) _experimentProgressPercent;
        this.experimentProgressRemainingTime = -1;
    }

    @Override
    public void addExperimentListener(ExperimentListener listener) {
        experimentProgressListeners.add(listener);
        listener.progressChanged(this);
    }

    @Override
    public void removeExperimentListener(ExperimentListener listener) {
        experimentProgressListeners.remove(listener);
    }

    @Override
    public int getExperimentProgressPercent() {

        return experimentProgressPercent;
    }

    @Override
    public long getExperimentProgressRemainingTime() {
        return experimentProgressRemainingTime;
    }

    @Override
    public String getExperimentProgressTask() {
        return experimentProgressTask;
    }

    @Override
    public int getExecutionProgressPercent() {
        return executionProgressPercent;
    }

    @Override
    public long getExecutionProgressRemainingTime() {
        return executionProgressRemainingTime;
    }

    @Override
    public String getExecutionProgressTask() {
        return executionProgressTask;
    }

    @Override
    public int getNumVueltas() {
        return numVueltas;
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
        setNextSeedToSeedHolders(seedValue);
    }

    protected void setNextSeedToSeedHolders(long seedValue) {

        if (getRecommenderSystem() instanceof SeedHolder) {
            SeedHolder seedHolder = (SeedHolder) getRecommenderSystem();
            seedHolder.setSeedValue(seedValue);
            Global.showInfoMessage("Reset RecommenderSystem seed to " + seedHolder.getSeedValue() + "\n");

        }

        getValidationTechnique().setSeedValue(seedValue);
        Global.showInfoMessage("Reset validationTechnique seed to " + getValidationTechnique().getSeedValue() + "\n");

        getPredictionProtocol().setSeedValue(seedValue);
        Global.showInfoMessage("Reset predictionProtocol seed to " + getPredictionProtocol().getSeedValue() + "\n");
    }

    /**
     * Método que devuelve el resultado agregado de una medida de evaluación.
     *
     * @param em Medida de evaluación
     * @return Resultado de la medida de evaluación
     */
    public MeasureResult getAggregateMeasureResult(EvaluationMeasure em) {
        List<MeasureResult> measureResults = new ArrayList<>();
        for (int execution = 0; execution < getNumExecutions(); execution++) {
            for (int split = 0; split < getValidationTechnique().getNumberOfSplits(); split++) {
                MeasureResult measureResult = getMeasureResult(em, execution, split);
                measureResults.add(measureResult);
            }

        }

        return em.agregateResults(measureResults);
    }

    public void setAggregateResults(Map<EvaluationMeasure, MeasureResult> aggregateResults) {
        this.aggregateResults = aggregateResults;
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.CASE_STUDY;
    }

    public int hashDataValidation() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.getSeedValue());
        hash = 97 * hash + Objects.hashCode(this.getDatasetLoader());
        hash = 97 * hash + Objects.hashCode(this.getPredictionProtocol());
        hash = 97 * hash + Objects.hashCode(this.getRelevanceCriteria());
        hash = 97 * hash + Objects.hashCode(this.getValidationTechnique());
        return hash;
    }

    public int hashTechnique() {
        return this.getRecommenderSystem().hashCode();
    }

    protected void registerInListeners() {
        this.getDatasetLoader().addParammeterListener(this);
    }


    @Override
    public boolean hasErrors() {
        return errors;
    }

    protected void setErrors(boolean b) {
        errors = b;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void initStructures(int executionNumber, int splitNumber) {

        for (int execution = 0; execution < executionNumber; execution++) {
            allLoopsResults.put(execution, Collections.synchronizedMap(new TreeMap<>()));
            for (int split = 0; split < splitNumber; split++) {
                allLoopsResults.get(execution).put(split, Collections.synchronizedMap(new TreeMap<>()));
            }
        }
    }

    public long getLoopSeed(int execution, int split) {
        long caseStudySeed = getSeedValue();
        long offsetExecutionSplit = execution * getNumSplits() + split;
        long finalSeed = caseStudySeed + offsetExecutionSplit;
        return finalSeed;
    }

    protected final Map<Integer, Map<Integer, Map<EvaluationMeasure, MeasureResult>>> resultsAsTheyFinish
            = new TreeMap<>();

    protected synchronized void setResultsThisExecution(Integer newExecution, Map<Integer, Map<EvaluationMeasure, MeasureResult>> resultsNewExecution) {

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

            CaseStudy<RecommendationModel, RatingType> caseStudyCloned = (CaseStudy<RecommendationModel, RatingType>) this.clone();

            caseStudyCloned.setNumExecutions(execution + 1);

            caseStudyCloned.allLoopsResults
                    = resultsAsTheyFinish.entrySet().stream()
                    .filter(entry -> entry.getKey() <= execution)
                    .collect(Collectors.toMap(
                            entry -> entry.getKey(),
                            entry -> entry.getValue()
                    ));

            caseStudyCloned.aggregateResults = getEvaluationMeasures().stream().parallel().collect(Collectors.toMap(Function.identity(),
                    evaluationMeasure -> {

                        List<Map<EvaluationMeasure, MeasureResult>> maps = caseStudyCloned.
                                allLoopsResults.
                                values().stream().
                                flatMap(x -> x.values().stream()).
                                collect(Collectors.toList());

                        List<MeasureResult> allResultsThisMeasure = new ArrayList<>();

                        for(Map<EvaluationMeasure, MeasureResult> map: maps){
                            if(!map.containsKey(evaluationMeasure)){
                                Global.showWarning(new IllegalStateException("Evaluation measure "+evaluationMeasure+" not found in map with keys: "+map.keySet()));
                            } else if(map.get(evaluationMeasure) == null){
                                MeasureResult measureResult = map.get(evaluationMeasure);
                                Global.showWarning(new IllegalStateException("Map contains a result with null EvaluationMeasure"));
                            } else{
                                MeasureResult measureResult = map.get(evaluationMeasure);
                                allResultsThisMeasure.add(measureResult);
                            }
                        }

                        MeasureResult resultsAggregated
                        = evaluationMeasure.agregateResults(allResultsThisMeasure);

                        return resultsAggregated;
                    }));

            caseStudyCloned.setFinished();

            CaseStudyXML.saveCaseResults(caseStudyCloned, resultsDirectory);
            CaseStudyExcel.saveCaseResults(caseStudyCloned, resultsDirectory);

        });
    }

    public Collection<EvaluationMeasure> getEvaluationMeasures(){
        String evaluationMeasuresAsString = getParameterValue(EVALUATION_MEASURES_AS_STRING).toString();


        List<EvaluationMeasure> evaluationMeasures = Arrays.stream(evaluationMeasuresAsString.
                replace(",", " ").
                split(" ")).
                map(evaluatonMeasureAsString -> {

                    return EvaluationMeasuresFactory.getInstance().getClassByName(evaluatonMeasureAsString);
                }).collect(Collectors.toList());

        return evaluationMeasures;
    }

    public RecommenderSystem<RecommendationModel> getRecommenderSystem() {
        return (RecommenderSystem<RecommendationModel>) getParameterValue(RECOMMENDER_SYSTEM);
    }

    public CaseStudy<RecommendationModel,RatingType> setRecommenderSystem(RecommenderSystem<RecommendationModel> recommenderSystem){
        setParameterValue(RECOMMENDER_SYSTEM,recommenderSystem);
        return this;
    }

    public DatasetLoader<RatingType> getDatasetLoader() {
        return (DatasetLoader<RatingType>) getParameterValue(DATASET_LOADER);
    }

    public CaseStudy<RecommendationModel, RatingType> setValidationTechnique(ValidationTechnique validationTechnique) {
        setParameterValue(VALIDATION_TECHNIQUE,validationTechnique);
        return this;
    }

    public ValidationTechnique getValidationTechnique(){
        return (ValidationTechnique) getParameterValue(VALIDATION_TECHNIQUE);
    }

    public CaseStudy<RecommendationModel, RatingType> setNumExecutions(int numExecutions) {
        setParameterValue(NUM_EXECUTIONS,numExecutions);
        return this;
    }

    public int getNumExecutions(){
        return ((Number) getParameterValue(NUM_EXECUTIONS)).intValue();
    }


    /**
     * Devuelve el protocolo de predicción que usa el caso de estudio.
     *
     * @return Protocolo de predicción aplicado.
     */
    public PredictionProtocol getPredictionProtocol() {
        return (PredictionProtocol) getParameterValue(PREDICTION_PROTOCOL);
    }

    public CaseStudy<RecommendationModel, RatingType> setPredictionProtocol(PredictionProtocol predictionProtocol){
        setParameterValue(PREDICTION_PROTOCOL,predictionProtocol);
        return this;
    }

    public CaseStudy<RecommendationModel, RatingType> setRelevanceCriteria(RelevanceCriteria relevanceCriteria) {
        setParameterValue(RELEVANCE_CRITERIA,relevanceCriteria.getThreshold().doubleValue());
        return this;
    }

    public RelevanceCriteria getRelevanceCriteria(){
        double relevanceValue = ((Number) getParameterValue(RELEVANCE_CRITERIA)).doubleValue();
        return new RelevanceCriteria(relevanceValue);
    }

    public CaseStudy<RecommendationModel, RatingType> setEvaluationMeasures(
            Collection<EvaluationMeasure> evaluationMeasures) {

        String evaluationMeasuresAsString = evaluationMeasures.stream().
                map(evaluationMeasure -> evaluationMeasure.toString()).
                reduce((s, s2) -> s +","+s2).
                orElse("");

        setParameterValue(EVALUATION_MEASURES_AS_STRING,evaluationMeasuresAsString);
        return this;
    }

    public CaseStudy<RecommendationModel, RatingType> setDatasetLoader(
            DatasetLoader<RatingType> datasetLoader) {

        setParameterValue(DATASET_LOADER,datasetLoader);
        return this;
    }
}
