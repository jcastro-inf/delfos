package delfos.group.casestudy.definedcases.estylf2014;

import delfos.Constants;
import delfos.common.FileUtilities;
import delfos.common.aggregationoperators.Mean;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.dataset.loaders.movilens.ml100k.MovieLens100k;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;
import delfos.group.experiment.validation.validationtechniques.GroupValidationTechnique;
import delfos.group.experiment.validation.validationtechniques.HoldOutGroupRatedItems;
import delfos.group.experiment.validation.validationtechniques.NoValidation;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.RandomGroupRecommender;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.group.grs.cww.CentralityWeightedAggregationGRS;
import delfos.group.grs.cww.centrality.CentralityConceptDefinition;
import delfos.group.grs.cww.centrality.CentralityConceptDefinitionFactory;
import delfos.group.io.xml.casestudy.GroupCaseStudyXML;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.rs.RecommenderSystem;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.trustbased.PearsonCorrelationWithPenalty;
import delfos.rs.trustbased.WeightedGraphCalculation;
import delfos.rs.trustbased.implicittrustcomputation.ShambourLu_UserBasedImplicitTrustComputation;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;

/**
 * Crea los experimentos del congreso ISKE 2014 (dentro de flins).
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 22-ene-2014
 */
public class ExperimentsJRS2014 {

    public static final long seed = 1393231163086L;

    @Test
    public void generateCaseXML() {

        String directoryName
                = Constants.getTempDirectory().getAbsolutePath() + File.separator
                + "experiments" + File.separator
                + "jrs2014" + File.separator;

        File directory = new File(directoryName);
        if (directory.exists()) {
            FileUtilities.deleteDirectoryRecursive(directory);
        }
        directory.mkdirs();
        File datasetDirectory = new File(directoryName + "dataset" + File.separator);
        datasetDirectory.mkdirs();

        final int numGroups = 5;
        final int[] groupSizeArray = {5};

        final DatasetLoader<? extends Rating> datasetLoader = new RandomDatasetLoader();
        final int numEjecuciones = 1;
        final Collection<GroupEvaluationMeasure> evaluationMeasures = GroupEvaluationMeasuresFactory.getInstance().getAllClasses();
        final RelevanceCriteria criteria = new RelevanceCriteria(4);
        final GroupPredictionProtocol groupPredictionProtocol = new NoPredictionProtocol();
        final GroupValidationTechnique groupValidationTechniqueValue = new HoldOutGroupRatedItems();

        for (int groupSize : groupSizeArray) {

            final GroupFormationTechnique groupFormationTechnique = new FixedGroupSize_OnlyNGroups(numGroups, groupSize);
            List<RecommenderSystem> coreRSs = new ArrayList<>();

            coreRSs.add(new KnnMemoryBasedCFRS());
            coreRSs.get(0).setAlias("KnnUU");

            for (RecommenderSystem coreRS : coreRSs) {
                for (GroupRecommenderSystem groupRecommenderSystem : getGRS(coreRS)) {
                    GroupCaseStudy groupCaseStudy = new GroupCaseStudy(
                            datasetLoader,
                            groupRecommenderSystem,
                            groupFormationTechnique,
                            groupValidationTechniqueValue, groupPredictionProtocol,
                            evaluationMeasures,
                            criteria,
                            numEjecuciones);

                    groupCaseStudy.setSeedValue(seed);
                    String fileName = groupRecommenderSystem.getAlias() + "_groupSize-" + groupSize + ".xml";
                    File file = new File(directory + File.separator + fileName);
                    GroupCaseStudyXML.caseStudyToXMLFile_onlyDescription(groupCaseStudy, file);
                }
            }
        }

        //generateDatasetFile
        {
            GroupCaseStudy groupCaseStudy = new GroupCaseStudy(
                    new MovieLens100k(new File("C:\\Dropbox\\Datasets\\MovieLens\\0 - MovieLens-100k ratings\\ml-100k")),
                    new RandomGroupRecommender(),
                    new FixedGroupSize_OnlyNGroups(1, 1), new NoValidation(), new NoPredictionProtocol(),
                    GroupEvaluationMeasuresFactory.getInstance().getAllClasses(),
                    new RelevanceCriteria(), 1);

            File file = new File(directory + File.separator + "dataset" + File.separator + "ml-100k.xml");
            GroupCaseStudyXML.caseStudyToXMLFile_onlyDescription(groupCaseStudy, file);
        }
    }

    private static Iterable<GroupRecommenderSystem<? extends Object, ? extends Object>> getGRS(RecommenderSystem singleUserRecommender) {

        LinkedList<GroupRecommenderSystem<? extends Object, ? extends Object>> grsList = new LinkedList<>();

        int i = 0;
        int stringLength = 3;

        {
            //Baseline
            AggregationOfIndividualRatings baseline = new AggregationOfIndividualRatings(singleUserRecommender, new Mean());

            {
                String number = Integer.toString(i);
                i++;
                while (number.length() < stringLength) {
                    number = "0" + number;
                }
                grsList.add(baseline);
                grsList.getLast().setAlias(number + "_" + baseline.getName() + "_" + baseline.getSingleUserRecommender().getName() + "_Aggr_" + baseline.getAggregationOperator().getName());
            }
        }
        ArrayList<WeightedGraphCalculation> graphCalculation = new ArrayList<>();

        {
            WeightedGraphCalculation wgc = new ShambourLu_UserBasedImplicitTrustComputation();
            wgc.setAlias("ShambourLu");
            graphCalculation.add(wgc);
        }
        {
            WeightedGraphCalculation wgc = new PearsonCorrelationWithPenalty(30);
            wgc.setAlias("PCC(30)");
            graphCalculation.add(wgc);
        }

        double[] strongDefinitionValues = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1};

        boolean[] strongApplyValues = {false, true};
        boolean[] normaliseValues = {false, true};
        for (WeightedGraphCalculation weightedGraphCalculation : graphCalculation) {
            for (CentralityConceptDefinition centralityConceptDefinition : CentralityConceptDefinitionFactory.getInstance().getAllClasses()) {
                for (boolean normalise : normaliseValues) {
                    for (boolean strongApply : strongApplyValues) {
                        if (!strongApply) {
                            double aStrong = 0;
                            double bStrong = 1;

                            String number = Integer.toString(i);
                            i++;
                            while (number.length() < stringLength) {
                                number = "0" + number;
                            }

                            grsList.add(new CentralityWeightedAggregationGRS(
                                    singleUserRecommender,
                                    weightedGraphCalculation,
                                    centralityConceptDefinition,
                                    normalise,
                                    strongApply,
                                    aStrong,
                                    bStrong));

                            grsList.getLast().setAlias("CentralityGRS");

                            StringBuilder grsAlias = new StringBuilder();

                            grsAlias.append(number).append("_");
                            grsAlias.append(grsList.getLast().getAlias()).append("_");
                            grsAlias.append(weightedGraphCalculation.getAlias()).append("_");
                            grsAlias.append(singleUserRecommender.getAlias()).append("_");

                            if (normalise) {
                                grsAlias.append("normalise_");
                            }
                            if (strongApply) {
                                grsAlias.append("Strong(").append(aStrong).append(",").append(bStrong).append(")_");
                            }

                            grsList.getLast().setAlias(grsAlias.toString());
                        } else {
                            for (int j = 1; j < strongDefinitionValues.length; j++) {
                                double aStrong = strongDefinitionValues[j - 1];
                                double bStrong = strongDefinitionValues[j];

                                String number = Integer.toString(i);
                                i++;
                                while (number.length() < stringLength) {
                                    number = "0" + number;
                                }

                                grsList.add(new CentralityWeightedAggregationGRS(
                                        singleUserRecommender,
                                        weightedGraphCalculation, centralityConceptDefinition, normalise,
                                        strongApply,
                                        aStrong,
                                        bStrong));

                                StringBuilder grsAlias = new StringBuilder();

                                grsAlias.append(number).append("_");
                                grsAlias.append(grsList.getLast().getName()).append("_");
                                grsAlias.append(weightedGraphCalculation.getName()).append("_");
                                grsAlias.append(singleUserRecommender.getName()).append("_");

                                if (normalise) {
                                    grsAlias.append("normalise_");
                                }
                                if (strongApply) {
                                    grsAlias.append("Strong(").append(aStrong).append(",").append(bStrong).append(")_");
                                }

                                grsList.getLast().setAlias(grsAlias.toString());
                            }
                        }
                    }
                }
            }
        }

        return grsList;
    }
}
