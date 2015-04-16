package delfos.group.grs.svd;

import java.io.Serializable;
import java.util.ArrayList;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 08-jul-2013
 */
public class GroupSVDModel implements Serializable {

    private static final long serialVersionUID = 1;
    private final GroupOfUsers group;
    private final ArrayList<Double> groupFeatures;

    public GroupSVDModel(GroupOfUsers group, ArrayList<Double> groupFeatures) {
        this.group = group;
        this.groupFeatures = new ArrayList<Double>(groupFeatures);
    }

    public GroupOfUsers getGroup() {
        return group;
    }

    public ArrayList<Double> getGroupFeatures() {
        return groupFeatures;
    }
}
