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
package delfos.group.results.groupevaluationmeasures.printers;

import delfos.Constants;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import java.io.File;

/**
 * Abstract class for measures that collect and print detailed information in a
 * file.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
