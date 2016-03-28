package delfos.group.io.excel.casestudy;

import delfos.constants.TestConstants;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups;
import delfos.group.experiment.validation.predictionvalidation.HoldOutPrediction;
import delfos.group.experiment.validation.validationtechniques.CrossFoldValidation_Ratings;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import java.io.File;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class GroupCaseStudyExcelTest {

    public static final File TEST_DIRECTORY = new File(TestConstants.TEST_DATA_DIRECTORY + "GroupCaseStudyToExcel" + File.separator);

    @BeforeClass
    public static void setUpClass() {
        TEST_DIRECTORY.mkdirs();
        TEST_DIRECTORY.deleteOnExit();
    }

    /**
     * Test of saveCaseResults method, of class GroupCaseStudyExcel.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testSaveCaseResults() throws Exception {

        GroupCaseStudy caseStudyGroup = new GroupCaseStudy(
                new RandomDatasetLoader(),
                new AggregationOfIndividualRatings(),
                new FixedGroupSize_OnlyNGroups(2, 5), new CrossFoldValidation_Ratings(), new HoldOutPrediction(),
                GroupEvaluationMeasuresFactory.getInstance().getAllClasses(),
                new RelevanceCriteria(4), 7);

        caseStudyGroup.execute();
        caseStudyGroup.setAlias("GroupCaseStudyExcelTest_testCase");

        GroupCaseStudyExcel.saveCaseResults(caseStudyGroup, TEST_DIRECTORY);
    }

    @Test
    public void testCombinations() {

    }
}
