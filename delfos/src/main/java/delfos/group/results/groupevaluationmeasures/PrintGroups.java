package delfos.group.results.groupevaluationmeasures;

import delfos.Constants;
import delfos.ERROR_CODES;
import delfos.common.FileUtilities;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

/**
 * Medida de evaluación que muestra los grupos que se evaluaron y los usuarios
 * que hay en cada uno de los grupos evaluados. Asimismo añade algunas
 * estadísticas generales sobre los mismos.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 15-Jan-2013
 */
public class PrintGroups extends GroupEvaluationMeasure {

    public static final File TEST_SET_DIRECTORY = new File(
            Constants.getTempDirectory().getAbsoluteFile() + File.separator
            + "GroupCaseStudy-Print" + File.separator);

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(GroupRecommenderSystemResult groupRecommenderSystemResult, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        FileUtilities.createDirectoryPath(TEST_SET_DIRECTORY);

        String fileName = TEST_SET_DIRECTORY.getPath() + File.separator
                + groupRecommenderSystemResult.getGroupCaseStudyAlias() + "__"
                + "-exec=" + groupRecommenderSystemResult.getThisExecution()
                + "-split=" + groupRecommenderSystemResult.getThisSplit()
                + "-groups.xml";

        File file = new File(fileName);

        Element groups = new Element("Groups");

        for (GroupOfUsers groupOfUsers : groupRecommenderSystemResult) {
            Element group = new Element("Group");
            group.setAttribute("group", groupOfUsers.toString());
            groups.addContent(group);
        }

        XMLOutputter outputter = new XMLOutputter(Constants.getXMLFormat());
        FileUtilities.createDirectoriesForFile(file);
        try (FileWriter fileWriter = new FileWriter(file)) {
            outputter.output(groups, fileWriter);
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_RESULTS_FILE.exit(ex);
        }

        return new GroupEvaluationMeasureResult(this, 1.0);
    }

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }
}
