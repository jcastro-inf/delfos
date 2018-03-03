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
package delfos.experiment.casestudy.cluster;

import java.io.File;
import java.util.List;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.experiment.Experiment;
import delfos.experiment.casestudy.CaseStudy;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;

/**
 *
 * @version 19-jun-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public interface ExperimentPreparator {

    public <ExperimentType extends Experiment> void prepareExperimentGeneral(
            List<ExperimentType> experiments, File directory);

    public void executeExperimentsGeneral(File directory);

    public void prepareExperiment(File directory, List<CaseStudy> caseStudies, DatasetLoader< ? extends Rating> datasetLoader);

    public void prepareGroupExperiment(File experimentBaseDirectory, List<GroupCaseStudy> groupCaseStudies, DatasetLoader<? extends Rating>... datasetLoader);
}
