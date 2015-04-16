package delfos.group.factories;

import delfos.factories.Factory;
import delfos.group.experiment.validation.groupformation.AllPossibleGroups;
import delfos.group.experiment.validation.groupformation.FixedGroupSize;
import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups;
import delfos.group.experiment.validation.groupformation.GivenGroups;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.groupformation.ProbabilityDistributionOfSizes;
import delfos.group.experiment.validation.groupformation.SimilarMembers;
import delfos.group.experiment.validation.groupformation.SimilarMembers_except;

/**
 * Conoce las técnicas de generación de grupos que se utilizarán en los casos de
 * estudio para evaluar sistemas de recomendación a grupos.
 *
 * @author Jorge Castro Gallardo
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
        instance.addClass(ProbabilityDistributionOfSizes.class);

        instance.addClass(GivenGroups.class);

        instance.addClass(SimilarMembers.class);
        instance.addClass(SimilarMembers_except.class);
    }

    protected GroupFormationTechniquesFactory() {
    }
}
