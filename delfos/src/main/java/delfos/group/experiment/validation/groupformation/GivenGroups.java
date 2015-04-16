package delfos.group.experiment.validation.groupformation;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.StringParameter;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén)
 * @version 1.0 13-May-2013
 */
public class GivenGroups extends GroupFormationTechnique {

    private static final String arrayBegin = "[";
    private static final String arrayEnd = "]";
    private static final String arraySeparator = ",";

    private static final long serialVersionUID = 1L;

    private static final Parameter GROUPS = new Parameter("GROUPS", new StringParameter());

    public GivenGroups() {
        addParameter(GROUPS);
    }

    public GivenGroups(String groups) {
        this();

        Collection<GroupOfUsers> parseGroupsString = parseGroupsString(groups);

        if (parseGroupsString.isEmpty()) {
            throw new IllegalArgumentException("Group list is empty");
        }

        setParameterValue(GROUPS, groups);
    }

    public GivenGroups(GroupOfUsers... groups) {
        this();
        if (groups.length == 0) {
            throw new IllegalArgumentException("Group list is empty");
        }
        setParameterValue(GROUPS, Arrays.toString(groups));
    }

    @Override
    public Collection<GroupOfUsers> shuffle(DatasetLoader<? extends Rating> datasetLoader) {

        String groupsString = (String) getParameterValue(GROUPS);

        Collection<GroupOfUsers> groups = parseGroupsString(groupsString);

        return groups;
    }

    private Collection<GroupOfUsers> parseGroupsString(String groupsString) {

        if (!(groupsString.contains(arrayBegin) && groupsString.contains(arrayEnd))) {
            throw new IllegalArgumentException("String does not contain array delimiters: " + arrayBegin + " and " + arrayEnd);
        }

        Collection<GroupOfUsers> ret = new LinkedList<>();

        groupsString = groupsString.replaceAll(" ", "");

        Pattern pattern = Pattern.compile("\\[[\\d,]*\\]");

        Matcher matcher = pattern.matcher(groupsString);
        while (matcher.find()) {
            String group = matcher.group();
            group = group.substring(1, group.length() - 1);

            Collection<Integer> idMembers = new LinkedList<>();
            Arrays.asList(group.split(",")).stream().forEach((idMember) -> idMembers.add(Integer.parseInt(idMember)));
            ret.add(new GroupOfUsers(idMembers));
        }

        return ret;
    }
}
