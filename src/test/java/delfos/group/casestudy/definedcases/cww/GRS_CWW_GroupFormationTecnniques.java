package delfos.group.casestudy.definedcases.cww;

import delfos.Constants;
import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.casestudy.cluster.TuringPreparator;
import delfos.experiment.validation.validationtechnique.CrossFoldValidation_Ratings;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.groupformation.SimilarMembers_OnlyNGroups;
import delfos.group.experiment.validation.groupformation.SimilarMembers_except;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.svd.SVDforGroup_ratingsAggregation;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Test;

/**
 * Para comprobar como afecta la generación del grupo al performance del mismo
 * sistema de recomendación.
 *
 * @version 25-jun-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class GRS_CWW_GroupFormationTecnniques extends DelfosTest {

    public GRS_CWW_GroupFormationTecnniques() {
    }

    @Test
    public void makeCasesAndExecuteLocaly() {

        String experimentBaseDirectoryString
                = Constants.getTempDirectory().getAbsolutePath() + File.separator
                + "experiments" + File.separator
                + this.getClass().getSimpleName() + File.separator;

        File experimentBaseDirectory = new File(experimentBaseDirectoryString);

        GroupRecommenderSystem groupRecommenderSystem = new SVDforGroup_ratingsAggregation();
        DatasetLoader datasetLoader = new ConfiguredDatasetLoader("ml-100k");

        List<GroupCaseStudy> groupCaseStudies = getGroupCaseStudies(datasetLoader, groupRecommenderSystem);

        TuringPreparator turingPreparator = new TuringPreparator();
        turingPreparator.prepareGroupExperiment(experimentBaseDirectory, groupCaseStudies, datasetLoader);
    }

    private List<GroupCaseStudy> getGroupCaseStudies(DatasetLoader datasetLoader, GroupRecommenderSystem... groupRecommenderSystems) {
        List<GroupCaseStudy> ret = new ArrayList<>();

        final int numGroups = 50;
        final int groupSize = 5;
        final int numEjecuciones = 1;
        final Collection<GroupEvaluationMeasure> evaluationMeasures = GroupEvaluationMeasuresFactory.getInstance().getAllClasses();
        final RelevanceCriteria criteria = new RelevanceCriteria(4);
        final GroupPredictionProtocol groupPredictionProtocol = new NoPredictionProtocol();
        final ValidationTechnique validationTechniqueValue = new CrossFoldValidation_Ratings();

        final GroupFormationTechnique[] groupFormationTechniques = {
            new SimilarMembers_OnlyNGroups(numGroups, groupSize),
            new SimilarMembers_except(numGroups, groupSize, 1),
            new SimilarMembers_except(numGroups, groupSize, 2),
            new SimilarMembers_except(numGroups, groupSize, 3),
            new FixedGroupSize_OnlyNGroups(numGroups, groupSize)
        };

        for (GroupFormationTechnique groupFormationTechnique : groupFormationTechniques) {
            for (GroupRecommenderSystem groupRecommenderSystem : groupRecommenderSystems) {
                GroupCaseStudy groupCaseStudy = new GroupCaseStudy(datasetLoader, groupRecommenderSystem, groupFormationTechnique, validationTechniqueValue, groupPredictionProtocol, evaluationMeasures, criteria, numEjecuciones);
                groupCaseStudy.setAlias(groupFormationTechnique.getAlias() + "->" + groupRecommenderSystem.getAlias());
                ret.add(groupCaseStudy);
            }
        }

        return ret;
    }
}
