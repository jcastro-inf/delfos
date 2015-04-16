package delfos.group.grs.penalty.grouper;

import delfos.factories.Factory;

/**
 *
* @author Jorge Castro Gallardo
 * @version 20-sept-2014
 */
public class GrouperFactory extends Factory<Grouper> {

    private static final GrouperFactory instance;

    public static GrouperFactory getInstance() {
        return instance;
    }

    static {
        instance = new GrouperFactory();

        instance.addClass(GrouperByIdItem.class);
        instance.addClass(GrouperByDataClustering.class);

    }
}
