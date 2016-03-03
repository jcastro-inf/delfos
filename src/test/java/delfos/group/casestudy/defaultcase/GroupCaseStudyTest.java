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

import delfos.Constants;
import delfos.common.FileUtilities;
import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.experiment.casestudy.cluster.TuringPreparator;
import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;
import delfos.group.experiment.validation.validationtechniques.CrossFoldValidation_groupRatedItems;
import delfos.group.grs.RandomGroupRecommender;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class GroupCaseStudyTest {

    private static final long SEED_VALUE = 123456;
    private static final int NUM_EXECUTIONS = 20;
    private static final int NUM_GROUPS = 2;
    private static final int GROUPS_SIZE = 5;

    private static final File EXPERIMENT_DIRECTORY = new File(Constants.getTempDirectory()
            + File.separator
            + GroupCaseStudyTest.class.getSimpleName()
            + File.separator + "testGroupCaseStudy");

    @Test
    public void testGroupCaseStudy() {

        FileUtilities.deleteDirectoryRecursive(EXPERIMENT_DIRECTORY);

        createGroupXML();

        execute();
    }

    public void createGroupXML() {
        RandomGroupRecommender grs = new RandomGroupRecommender();
        grs.setSeedValue(SEED_VALUE);

        DatasetLoader<? extends Rating> datasetLoader
                = new ConfiguredDatasetLoader("ml-100k");

        GroupCaseStudy groupCaseStudy = new GroupCaseStudy();

        groupCaseStudy.setDatasetLoader(datasetLoader)
                .setGroupRecommenderSystem(grs)
                .setGroupFormationTechnique(new FixedGroupSize_OnlyNGroups(NUM_GROUPS, GROUPS_SIZE))
                .setGroupValidationTechnique(new CrossFoldValidation_groupRatedItems())
                .setGroupPredictionProtocol(new NoPredictionProtocol());

        groupCaseStudy.setNumExecutions(NUM_EXECUTIONS);
        groupCaseStudy.setSeedValue(SEED_VALUE);

        List<GroupCaseStudy> groupCaseStudies = Arrays.asList(groupCaseStudy);

        new TuringPreparator().prepareGroupExperiment(EXPERIMENT_DIRECTORY, groupCaseStudies, datasetLoader);
    }

    private void execute() {
        new TuringPreparator().executeAllExperimentsInDirectory(EXPERIMENT_DIRECTORY, 20);
    }

}
