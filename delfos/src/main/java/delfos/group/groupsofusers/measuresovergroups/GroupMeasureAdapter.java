package delfos.group.groupsofusers.measuresovergroups;

import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;

/**
 * Define los métodos para solicitar una medida que se aplica sobre un grupo de
 * usuarios.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 03-Jun-2013
 */
public abstract class GroupMeasureAdapter extends ParameterOwnerAdapter implements GroupMeasure {

    public GroupMeasureAdapter() {
        super();
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.GROUP_MEASURE;
    }
}
