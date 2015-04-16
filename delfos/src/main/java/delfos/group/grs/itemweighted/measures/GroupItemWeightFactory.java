package delfos.group.grs.itemweighted.measures;

import delfos.factories.Factory;

public class GroupItemWeightFactory extends Factory<GroupItemWeight> {

    private static final long serialVersionUID = 1L;
    private static final GroupItemWeightFactory instance;

    static {
        instance = new GroupItemWeightFactory();

        instance.addClass(NoWeight.class);
        instance.addClass(StandardDeviationWeights.class);
        instance.addClass(Tweak2Weights.class);
    }

    public static GroupItemWeightFactory getInstance() {
        return instance;
    }

}
