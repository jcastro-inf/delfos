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
package delfos.group.experiment.validation.groupformation;

import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.StringParameter;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
    public Collection<GroupOfUsers> generateGroups(DatasetLoader<? extends Rating> datasetLoader) {

        String groupsString = (String) getParameterValue(GROUPS);

        Collection<GroupOfUsers> groups = parseGroupsString(groupsString)
                .stream().map(group -> new GroupOfUsers(
                        group.getIdMembers().stream().map(user -> datasetLoader.getUsersDataset().getUser(user)).collect(Collectors.toList()))
                )
                .collect(Collectors.toList());

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
            ret.add(new GroupOfUsers(idMembers.toArray(new Integer[0])));
        }

        return ret;
    }
}
