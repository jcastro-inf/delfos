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
import delfos.ERROR_CODES;
import delfos.common.FileUtilities;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
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
 */
public class PrintGroups extends GroupEvaluationMeasureInformationPrinter {

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            RatingsDataset<? extends Rating> testDataset,
            RelevanceCriteria relevanceCriteria,
            DatasetLoader<? extends Rating> trainingDatasetLoader,
            DatasetLoader<? extends Rating> testDatasetLoader) {

        File output = new File(PRINTER_DIRECTORY.getPath() + File.separator
                + groupRecommenderSystemResult.getGroupCaseStudyAlias() + "__"
                + "exec=" + groupRecommenderSystemResult.getThisExecution()
                + "-split=" + groupRecommenderSystemResult.getThisSplit()
                + "-groups.xml");

        Element groups = new Element("Groups");

        for (GroupOfUsers groupOfUsers : groupRecommenderSystemResult.getGroupsOfUsers()) {
            Element group = new Element("Group");
            group.setAttribute("group", groupOfUsers.toString());
            groups.addContent(group);
        }

        FileUtilities.createDirectoriesForFile(output);

        try (FileWriter fileWriter = new FileWriter(output)) {
            XMLOutputter outputter = new XMLOutputter(Constants.getXMLFormat());
            outputter.output(groups, fileWriter);
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_RESULTS_FILE.exit(ex);
        }

        return new GroupEvaluationMeasureResult(this, 1.0);
    }
}
