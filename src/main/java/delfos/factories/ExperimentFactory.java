package delfos.factories;

import delfos.experiment.ExperimentAdapter;
import delfos.experiment.casestudy.CaseStudy;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;

public class ExperimentFactory extends Factory<ExperimentAdapter> {
    private static final ExperimentFactory instance;

    private ExperimentFactory(){

    }

    static {
        instance = new ExperimentFactory();

        instance.addClass(CaseStudy.class);
        instance.addClass(GroupCaseStudy.class);
    }



}
