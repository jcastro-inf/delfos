package delfos.group.casestudy.definedcases.iske2014;

import delfos.Constants;
import delfos.common.FileUtilities;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.dataset.loaders.movilens.ml100k.MovieLens100k;
import delfos.group.casestudy.GroupCaseStudy;
import delfos.group.casestudy.defaultcase.DefaultGroupCaseStudy;
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
import delfos.group.grs.benchmark.polylens.PolyLens;
import delfos.group.grs.filtered.GroupRecommenderSystemWithPreFilter;
import delfos.group.grs.filtered.filters.NoFilter;
import delfos.group.grs.filtered.filters.OutliersRatingsFilter;
import delfos.group.io.xml.casestudy.GroupCaseStudyXML;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
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
public class ExperimentsISKE2014 {

    public static final long seed = 1393231163086L;

    @Test
    public void generateCaseXML() {

        String directoryName
                = Constants.getTempDirectory().getAbsolutePath() + File.separator
                + "experiments" + File.separator
                + "ISKE2014" + File.separator;

        File directory = new File(directoryName);
        if (directory.exists()) {
            FileUtilities.deleteDirectoryRecursive(directory);
        }
        directory.mkdirs();
        File datasetDirectory = new File(directoryName + "dataset" + File.separator);
        datasetDirectory.mkdirs();

        final int numGroups = 100;
        final int[] groupSizeArray = {3, 5, 7, 9};

        final DatasetLoader<? extends Rating> datasetLoader = new RandomDatasetLoader();
        final int numEjecuciones = 1;
        final Collection<GroupEvaluationMeasure> evaluationMeasures = GroupEvaluationMeasuresFactory.getInstance().getAllClasses();
        final RelevanceCriteria criteria = new RelevanceCriteria(4);
        final GroupPredictionProtocol groupPredictionProtocol = new NoPredictionProtocol();
        final GroupValidationTechnique groupValidationTechniqueValue = new HoldOutGroupRatedItems();

        int i = 0;

        for (int groupSize : groupSizeArray) {
            final GroupFormationTechnique groupFormationTechnique = new FixedGroupSize_OnlyNGroups(numGroups, groupSize);
            List<GroupRecommenderSystem> coreGRSs = new ArrayList<>();
            coreGRSs.add(new PolyLens(60));

            for (GroupRecommenderSystem coreGRS : coreGRSs) {
                for (GroupRecommenderSystem groupRecommenderSystem : getGRS(coreGRS)) {
                    GroupCaseStudy groupCaseStudy = new DefaultGroupCaseStudy(
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
                    GroupCaseStudyXML.saveCaseDescription(groupCaseStudy, file.getAbsolutePath());
                    i++;
                }
            }
        }

        //generateDatasetFile
        {
            GroupCaseStudy groupCaseStudy = new DefaultGroupCaseStudy(
                    new MovieLens100k(new File("C:\\Dropbox\\Datasets\\MovieLens\\0 - MovieLens-100k ratings\\ml-100k")),
                    new RandomGroupRecommender(),
                    new FixedGroupSize_OnlyNGroups(1, 1), new NoValidation(), new NoPredictionProtocol(),
                    GroupEvaluationMeasuresFactory.getInstance().getAllClasses(),
                    new RelevanceCriteria(), 1);

            File file = new File(directory + File.separator + "dataset" + File.separator + "ml-100k.xml");
            GroupCaseStudyXML.saveCaseDescription(groupCaseStudy, file.getAbsolutePath());
        }

    }

    private static Iterable<GroupRecommenderSystem<? extends Object, ? extends Object>> getGRS(GroupRecommenderSystem preFilterCoreGRS) {
        LinkedList<GroupRecommenderSystem<? extends Object, ? extends Object>> grsList = new LinkedList<>();

        final double[] differenceThreshold_values = {
            1.0,
            2.0,
            3.0,};

        final double[] percentageMaxFilteredOut_values = {
            0.8};
        final boolean[] keepAtLeastOneRating_values = {true, false};

        int i = 0;

        int stringLength = 3;

        GroupRecommenderSystem grs = new GroupRecommenderSystemWithPreFilter(preFilterCoreGRS, new NoFilter());
        grsList.add(
                grs);
        {
            String number = Integer.toString(i);
            i++;
            while (number.length() < stringLength) {
                number = "0" + number;
            }
            grsList.getLast().setAlias(number + "_" + preFilterCoreGRS + "_PreFilter_noFilter");
        }

        for (double differenceThreshold : differenceThreshold_values) {
            for (double percentageMaxFilteredOut : percentageMaxFilteredOut_values) {
                for (boolean keepAtLeastOneRating : keepAtLeastOneRating_values) {
                    grs = new GroupRecommenderSystemWithPreFilter(new PolyLens(60), new OutliersRatingsFilter(differenceThreshold, percentageMaxFilteredOut, keepAtLeastOneRating));
                    grsList.add(grs);

                    String number = Integer.toString(i);
                    i++;
                    while (number.length() < stringLength) {
                        number = "0" + number;
                    }
                    grsList.getLast().setAlias(number + "_" + preFilterCoreGRS + "_PreFilter_outliers(u=" + differenceThreshold + ",KR=" + keepAtLeastOneRating + ",MPD=" + percentageMaxFilteredOut + ")");
                }
            }
        }

        return grsList;
    }
}
