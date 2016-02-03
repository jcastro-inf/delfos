package delfos.group.casestudy.definedcases.hesitant.experiment2allGroups;

import delfos.Constants;
import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.casestudy.cluster.TuringPreparator;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;
import delfos.group.experiment.validation.validationtechniques.CrossFoldValidation_groupRatedItems;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.grs.GroupRecommenderSystem;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class HesitantGRS_1_allGroups_similarMembers extends DelfosTest {

    public HesitantGRS_1_allGroups_similarMembers() {
    }

    public static final long SEED_VALUE = 123456L;

    File experimentDirectory = new File(Constants.getTempDirectory().getAbsolutePath() + File.separator
            + "HesitantGRS.experiment2allGroups" + File.separator
            + HesitantGRS_1_allGroups_similarMembers.class.getSimpleName() + File.separator);

    public void createCaseStudyXML() {

        TuringPreparator turingPreparator = new TuringPreparator();

        List<GroupCaseStudy> groupCaseStudys = new ArrayList<>();

        for (GroupFormationTechnique groupFormationTechnique : HesitantGRS_configuration.getGroupFormationTechnique()) {
            for (GroupRecommenderSystem groupRecommenderSystem : HesitantGRS_configuration.getGRSs()) {
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

                groupCaseStudys.add(groupCaseStudy);
            }
        }

        turingPreparator.renameCaseStudyWithTheMinimumDistinctAlias(groupCaseStudys);

        turingPreparator.prepareGroupExperiment(
                experimentDirectory,
                groupCaseStudys,
                HesitantGRS_configuration.getDatasetLoader().toArray(new DatasetLoader[0]));
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
