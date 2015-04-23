package delfos.group.casestudy.definedcases.cww;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import delfos.common.Global;
import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.experiment.casestudy.cluster.TuringPreparator;
import delfos.group.casestudy.GroupCaseStudy;
import delfos.group.casestudy.defaultcase.DefaultGroupCaseStudy;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.svd.SVDforGroup_ratingsAggregation;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.experiment.validation.validationtechniques.CrossFoldValidation_Ratings;
import delfos.group.experiment.validation.validationtechniques.GroupValidationTechnique;
import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.groupformation.SimilarMembers;
import delfos.group.experiment.validation.groupformation.SimilarMembers_except;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;

/**
 * Para comprobar como afecta la generación del grupo al performance del mismo
 * sistema de recomendación.
 *
 * @version 25-jun-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class GRS_CWW_GroupFormationTecnniques extends DelfosTest {

    public GRS_CWW_GroupFormationTecnniques() {
    }

    @Test
    public void makeCasesAndExecuteLocaly() {
        Global.setNoVerbose();

        String experimentBaseDirectoryString = "experiments" + File.separator + this.getClass().getSimpleName() + File.separator;
        File experimentBaseDirectory = new File(experimentBaseDirectoryString);

        GroupRecommenderSystem groupRecommenderSystem = new SVDforGroup_ratingsAggregation();
        DatasetLoader datasetLoader = new ConfiguredDatasetLoader("ml-100k");

        List<GroupCaseStudy> groupCaseStudies = getGroupCaseStudies(datasetLoader, groupRecommenderSystem);

        TuringPreparator turingPreparator = new TuringPreparator();
        turingPreparator.prepareGroupExperiment(experimentBaseDirectory, groupCaseStudies, datasetLoader);
//        for (GroupCaseStudy groupCaseStudy : groupCaseStudies) {
//            try {
//                groupCaseStudy.execute();
//                String defaultFileName = groupCaseStudy.getAlias();
//                GroupCaseStudyXML.saveCaseResults(groupCaseStudy, groupCaseStudy.getGroupRecommenderSystem().getAlias(), defaultFileName);
//            } catch (CannotLoadContentDataset | CannotLoadRatingsDataset | UserNotFound | ItemNotFound ex) {
//                Logger.getLogger(GRS_CWW_GroupFormationTecnniques.class.getName()).log(Level.SEVERE, null, ex);
//            }
//
//        }
    }

    private List<GroupCaseStudy> getGroupCaseStudies(DatasetLoader datasetLoader, GroupRecommenderSystem... groupRecommenderSystems) {
        List<GroupCaseStudy> ret = new ArrayList<>();

        final int numGroups = 50;
        final int groupSize = 5;
        final int numEjecuciones = 1;
        final Collection<GroupEvaluationMeasure> evaluationMeasures = GroupEvaluationMeasuresFactory.getInstance().getAllClasses();
        final RelevanceCriteria criteria = new RelevanceCriteria(4);
        final GroupPredictionProtocol groupPredictionProtocol = new NoPredictionProtocol();
        final GroupValidationTechnique groupValidationTechniqueValue = new CrossFoldValidation_Ratings();

        final GroupFormationTechnique[] groupFormationTechniques = {
            new SimilarMembers(numGroups, groupSize),
            new SimilarMembers_except(numGroups, groupSize, 1),
            new SimilarMembers_except(numGroups, groupSize, 2),
            new SimilarMembers_except(numGroups, groupSize, 3),
            new FixedGroupSize_OnlyNGroups(numGroups, groupSize)
        };

        for (GroupFormationTechnique groupFormationTechnique : groupFormationTechniques) {
            for (GroupRecommenderSystem groupRecommenderSystem : groupRecommenderSystems) {
                GroupCaseStudy groupCaseStudy = new DefaultGroupCaseStudy(datasetLoader, groupRecommenderSystem, groupFormationTechnique, groupValidationTechniqueValue, groupPredictionProtocol, evaluationMeasures, criteria, numEjecuciones);
                groupCaseStudy.setAlias(groupFormationTechnique.getAlias() + "->" + groupRecommenderSystem.getAlias());
                ret.add(groupCaseStudy);
            }
        }

        return ret;
    }
}
