package delfos.group;

import delfos.Constants;
import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.loaders.jester.Jester;
import delfos.experiment.ExperimentListerner_default;
import delfos.experiment.casestudy.ExecutionProgressListener_default;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;
import delfos.group.experiment.validation.validationtechniques.GroupValidationTechnique;
import delfos.group.experiment.validation.validationtechniques.HoldOutGroupRatedItems;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.RandomGroupRecommender;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.group.grs.aggregation.AggregationOfIndividualRecommendations;
import delfos.group.io.xml.casestudy.GroupCaseStudyXML;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.rs.collaborativefiltering.knn.modelbased.KnnModelBasedCFRS;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Jorge Castro Gallardo
 * @version 1.0 14-May-2013
 */
public class DefaultExecution {

    private static final int numEjecuciones = 1;
    private static final int numOfGroups = 100;
    private static final long seed = 1358987046897L;

    private static final File resultsDirectory = new File(Constants.getTempDirectory().getPath() + File.separator);

    public void execute() throws CannotLoadContentDataset, CannotLoadRatingsDataset {

        Map<GroupFormationTechnique, Map<GroupEvaluationMeasure, String>> resultadosPorTamGrupos = new TreeMap<>();
        List<GroupFormationTechnique> groupFormationTechniques = getGroupFormationTechniques();
        Collection<GroupEvaluationMeasure> groupEvaluationMeasures = GroupEvaluationMeasuresFactory.getInstance().getAllClasses();

        for (Map.Entry<String, DatasetLoader> entryDataset : getDatasetLoaders().entrySet()) {
            DatasetLoader<? extends Rating> datasetLoader = entryDataset.getValue();
            String datasetName = entryDataset.getKey();
            RelevanceCriteria criteria = datasetLoader.getDefaultRelevanceCriteria();
            for (GroupValidationTechnique groupValidationTechnique : getGroupValidationTechniques()) {
                for (GroupPredictionProtocol groupPredictionProtocol : getGroupPredictionProtocols()) {
                    for (Map.Entry<String, GroupRecommenderSystem> entryRecommender : getGroupRecommenderSystems().entrySet()) {
                        GroupRecommenderSystem groupRecommenderSystem = entryRecommender.getValue();
                        String recommenderName = entryRecommender.getKey();
                        for (GroupFormationTechnique groupFormationTechnique : groupFormationTechniques) {
                            try {
                                resultadosPorTamGrupos.put(groupFormationTechnique, new TreeMap<>());
                                GroupCaseStudy caseStudyGroupRecommendation = new GroupCaseStudy(
                                        datasetLoader,
                                        groupRecommenderSystem,
                                        groupFormationTechnique,
                                        groupValidationTechnique, groupPredictionProtocol,
                                        groupEvaluationMeasures,
                                        criteria,
                                        numEjecuciones);

                                caseStudyGroupRecommendation.setSeedValue(seed);

                                caseStudyGroupRecommendation.addExperimentListener(new ExperimentListerner_default(System.out, 10000));
                                caseStudyGroupRecommendation.addExecutionProgressListener(new ExecutionProgressListener_default(System.out, 10000));
                                caseStudyGroupRecommendation.execute();
                                Global.show("-----------------------------------------------\n");
                                for (GroupEvaluationMeasure groupEvaluationMeasure : groupEvaluationMeasures) {
                                    Global.showln(caseStudyGroupRecommendation.getGroupRecommenderSystem().getName() + " --> " + caseStudyGroupRecommendation.getAggregateMeasureResult(groupEvaluationMeasure).toString());
                                    resultadosPorTamGrupos.get(groupFormationTechnique).put(groupEvaluationMeasure, caseStudyGroupRecommendation.getAggregateMeasureResult(groupEvaluationMeasure).toString());
                                }

                                int tamGrupos = (Integer) groupFormationTechnique.getParameterValue(FixedGroupSize_OnlyNGroups.GROUP_SIZE_PARAMETER);

                                caseStudyGroupRecommendation.setAlias("defaultExecution_" + "Dataset=" + datasetName + "_tamGroups=" + tamGrupos + "_" + recommenderName + "_" + System.currentTimeMillis());

                                GroupCaseStudyXML.saveCaseResults(caseStudyGroupRecommendation, resultsDirectory);
                            } catch (CannotLoadContentDataset ex) {
                                Global.showError(ex);
                                ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
                            } catch (CannotLoadRatingsDataset ex) {
                                Global.showError(ex);
                                ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
                            } catch (UserNotFound ex) {
                                Global.showError(ex);
                                ERROR_CODES.USER_NOT_FOUND.exit(ex);
                            } catch (ItemNotFound ex) {
                                Global.showError(ex);
                                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
                            }

                            Global.show(groupRecommenderSystem.getNameWithParameters());
                            for (GroupFormationTechnique gft : resultadosPorTamGrupos.keySet()) {
                                Global.showln("\t" + gft.getNameWithParameters());
                                for (GroupEvaluationMeasure gem : resultadosPorTamGrupos.get(gft).keySet()) {
                                    Global.showln("\t\t" + resultadosPorTamGrupos.get(gft).get(gem));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Devuelve las técnicas de formación de grupos que se deben aplicar. Para
     * cada una se ejecuta un caso de estudio distinto.
     *
     * @return Lista de técnicas de formación de grupos que se aplican.
     */
    public static List<GroupFormationTechnique> getGroupFormationTechniques() {
        List<GroupFormationTechnique> gft = new LinkedList<>();

        gft.add(new FixedGroupSize_OnlyNGroups(numOfGroups, 2));
        gft.add(new FixedGroupSize_OnlyNGroups(numOfGroups, 4));

        return gft;
    }

    /**
     * Obtiene los sistemas de recomendación que se evaluan.
     *
     * @return
     */
    public static Map<String, GroupRecommenderSystem> getGroupRecommenderSystems() {
        Map<String, GroupRecommenderSystem> groupRecommenderSystems = new TreeMap<>();

        groupRecommenderSystems.put(AggregationOfIndividualRecommendations.class.getSimpleName(), new AggregationOfIndividualRatings(new KnnModelBasedCFRS()));

        groupRecommenderSystems.put("A_" + RandomGroupRecommender.class.getSimpleName(), new RandomGroupRecommender());
        groupRecommenderSystems.put(AggregationOfIndividualRecommendations.class.getSimpleName(), new AggregationOfIndividualRecommendations());

        return groupRecommenderSystems;
    }

    private static Iterable<GroupPredictionProtocol> getGroupPredictionProtocols() {
        List<GroupPredictionProtocol> ret = new LinkedList<>();
        ret.add(new NoPredictionProtocol());
        return ret;
    }

    private static Map<String, DatasetLoader> getDatasetLoaders() {
        Map<String, DatasetLoader> ret = new TreeMap<>();

        DatasetLoader<? extends Rating> jester1 = new Jester();
        jester1.setParameterValue(Jester.DATASET_VERSION_PARAMETER, Jester.VERSION_1);
        ret.put("jester-1", jester1);

//        DatasetLoader<? extends Rating> jester2 = new Jester();
//        jester2.setParameterValue(Jester.DATASET_VERSION_PARAMETER, Jester.VERSION_2);
//        ret.put("jester-2", jester2);
//
//        DatasetLoader<? extends Rating> jester3 = new Jester();
//        jester3.setParameterValue(Jester.DATASET_VERSION_PARAMETER, Jester.VERSION_3);
//        ret.put("jester-3", jester3);
//
//        DatasetLoader<? extends Rating> movielens = new CSVfileDatasetLoader();
//        movielens.setParameterValue(CSVfileDatasetLoader.RATINGS_FILE, new File(Path.getDatasetDirectory() + File.separator + "movilens" + File.separator + "movi_ratings.csv"));
//        movielens.setParameterValue(CSVfileDatasetLoader.CONTENT_FILE, new File(Path.getDatasetDirectory() + File.separator + "movilens" + File.separator + "movi_peliculas.csv"));
//        ret.put("movielens", movielens);
        return ret;
    }

    private static Iterable<GroupValidationTechnique> getGroupValidationTechniques() {
        List<GroupValidationTechnique> ret = new LinkedList<>();
        ret.add(new HoldOutGroupRatedItems());
        return ret;
    }
}
