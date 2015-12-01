package delfos.group.casestudy.definedcases.hesitant.experiment2;

import delfos.Constants;
import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.casestudy.cluster.TuringPreparator;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.groupformation.SimilarMembers;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;
import delfos.group.experiment.validation.validationtechniques.CrossFoldValidation_groupRatedItems;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.hesitant.HesitantKnnGroupUser;
import delfos.utils.hesitant.similarity.HesitantPearson;
import delfos.utils.hesitant.similarity.factory.HesitantSimilarityFactory;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class HesitantGRS_2_CaseStudy_similarMembersAllGroups extends DelfosTest {

    public HesitantGRS_2_CaseStudy_similarMembersAllGroups() {
    }

    public static final long SEED_VALUE = 123456L;

    File experimentDirectory = new File(Constants.getTempDirectory().getAbsolutePath() + File.separator
            + "HesitantGRS.experiment2" + File.separator
            + HesitantGRS_2_CaseStudy_similarMembersAllGroups.class.getSimpleName() + File.separator);

    private Collection<GroupFormationTechnique> getGroupFormationTechnique() {
        return Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 50, 100, 200, 500).stream()
                .map((groupSize) -> {
                    GroupFormationTechnique gft = new SimilarMembers(groupSize);
                    return gft;
                }).collect(Collectors.toList());

    }

    private Collection<ConfiguredDatasetLoader> getDatasetLoader() {
        return Arrays.asList(new ConfiguredDatasetLoader("ml-100k"));
    }

    private List<GroupRecommenderSystem> getGRSs() {
        int neighborhoodSize = 100;

        List<GroupRecommenderSystem> ret = new ArrayList<>();

        ret.addAll(HesitantSimilarityFactory.getAll()
                .stream()
                .map((hesitantSimilarity) -> {

                    DecimalFormat format = new DecimalFormat("000");

                    HesitantKnnGroupUser grs = new HesitantKnnGroupUser();
                    grs.setAlias(hesitantSimilarity.getName() + "_neighborhoodSize=" + format.format(neighborhoodSize));
                    grs.setParameterValue(HesitantKnnGroupUser.NEIGHBORHOOD_SIZE, neighborhoodSize);
                    grs.setParameterValue(HesitantKnnGroupUser.HESITANT_SIMILARITY_MEASURE, hesitantSimilarity);
                    return grs;

                }).collect(Collectors.toList()));

        {

            HesitantPearson hesitantSimilarity = new HesitantPearson();
            NumberFormat format = new DecimalFormat("000");
            HesitantKnnGroupUser hesitantGRS = new HesitantKnnGroupUser();

            hesitantGRS.setAlias(hesitantSimilarity.getName() + "_deleteRepeated" + "_neighborhoodSize=" + format.format(neighborhoodSize));
            hesitantGRS.setParameterValue(HesitantKnnGroupUser.NEIGHBORHOOD_SIZE, neighborhoodSize);
            hesitantGRS.setParameterValue(HesitantKnnGroupUser.HESITANT_SIMILARITY_MEASURE, hesitantSimilarity);
            hesitantGRS.setParameterValue(HesitantKnnGroupUser.DELETE_REPEATED, true);

            ret.add(hesitantGRS);
        }
        return ret;
    }

    public void createCaseStudyXML() {

        TuringPreparator turingPreparator = new TuringPreparator();

        List<GroupCaseStudy> groupCaseStudys = new ArrayList<>();

        for (GroupFormationTechnique groupFormationTechnique : getGroupFormationTechnique()) {
            for (GroupRecommenderSystem groupRecommenderSystem : getGRSs()) {
                GroupCaseStudy groupCaseStudy = new GroupCaseStudy(
                        null,
                        groupRecommenderSystem,
                        groupFormationTechnique,
                        new CrossFoldValidation_groupRatedItems(),
                        new NoPredictionProtocol(),
                        GroupEvaluationMeasuresFactory.getInstance().getAllClasses(),
                        new RelevanceCriteria(4),
                        1,
                        SEED_VALUE
                );

                groupCaseStudy.setAlias(
                        "_dataValidation=" + groupCaseStudy.hashDataValidation()
                        + "_technique=" + groupCaseStudy.hashTechnique()
                        + "_" + groupRecommenderSystem.getAlias()
                        + "_allHash=" + groupCaseStudy.hashCode()
                );
                groupCaseStudys.add(groupCaseStudy);
            }
        }

        turingPreparator.prepareGroupExperiment(
                experimentDirectory,
                groupCaseStudys,
                getDatasetLoader().toArray(new DatasetLoader[0]));
    }

    @Test
    public void testExecute() throws Exception {
        FileUtilities.deleteDirectoryRecursive(experimentDirectory);
        createCaseStudyXML();
        Global.show("This case study has " + new TuringPreparator()
                .sizeOfAllExperimentsInDirectory(experimentDirectory)
                + " experiments");
    }
}
