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
package delfos.group.factories;

import delfos.factories.Factory;
import delfos.group.experiment.validation.groupformation.AllPossibleGroups;
import delfos.group.experiment.validation.groupformation.DissimilarMembers;
import delfos.group.experiment.validation.groupformation.DissimilarMembers_OnlyNGroups;
import delfos.group.experiment.validation.groupformation.FixedGroupSize;
import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups;
import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups_withOverlapingMembers;
import delfos.group.experiment.validation.groupformation.GivenGroups;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique_cache;
import delfos.group.experiment.validation.groupformation.ProbabilityDistributionOfSizes;
import delfos.group.experiment.validation.groupformation.SimilarMembers;
import delfos.group.experiment.validation.groupformation.SimilarMembers_OnlyNGroups;
import delfos.group.experiment.validation.groupformation.SimilarMembers_except;

/**
 * Conoce las técnicas de generación de grupos que se utilizarán en los casos de estudio para evaluar sistemas de
 * recomendación a grupos.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 16-Jan-2013
 * @version 9-Enero-2013 Adaptado a la nueva declaración de las factorías.
 */
public class GroupFormationTechniquesFactory extends Factory<GroupFormationTechnique> {

    protected static final GroupFormationTechniquesFactory instance;

    public static GroupFormationTechniquesFactory getInstance() {
        return instance;
    }

    static {
        instance = new GroupFormationTechniquesFactory();

        instance.addClass(AllPossibleGroups.class);
        instance.addClass(FixedGroupSize.class);
        instance.addClass(FixedGroupSize_OnlyNGroups.class);
        instance.addClass(FixedGroupSize_OnlyNGroups_withOverlapingMembers.class);

        instance.addClass(ProbabilityDistributionOfSizes.class);

        instance.addClass(GivenGroups.class);

        instance.addClass(SimilarMembers_OnlyNGroups.class);
        instance.addClass(SimilarMembers_except.class);
        instance.addClass(SimilarMembers.class);

        instance.addClass(DissimilarMembers.class);
        instance.addClass(DissimilarMembers_OnlyNGroups.class);

        instance.addClass(GroupFormationTechnique_cache.class);
    }

    protected GroupFormationTechniquesFactory() {
    }
}
