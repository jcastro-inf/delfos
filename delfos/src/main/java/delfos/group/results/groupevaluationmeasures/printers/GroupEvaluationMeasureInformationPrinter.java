package delfos.group.results.groupevaluationmeasures.printers;

import delfos.Constants;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import java.io.File;

/**
 * Abstract class for measures that collect and print detailed information in a
 * file.
 *
 * @author Jorge Castro Gallardo
 */
public abstract class GroupEvaluationMeasureInformationPrinter extends GroupEvaluationMeasure {

    public static final File PRINTER_DIRECTORY = new File(
            Constants.getTempDirectory().getAbsoluteFile() + File.separator
            + "GroupCaseStudy-Print" + File.separator);

    @Override
    public final boolean usesRatingPrediction() {
        return false;
    }
}
