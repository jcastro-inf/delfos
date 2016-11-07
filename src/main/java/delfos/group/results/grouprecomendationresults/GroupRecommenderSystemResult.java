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
package delfos.group.results.grouprecomendationresults;

import delfos.common.Global;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskInput;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskOutput;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Almacena los resultados de recomendaciones de un sistema de recomendación a grupos
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class GroupRecommenderSystemResult {

    protected Map<GroupOfUsers, SingleGroupRecommendationTaskInput> singleGroupRecommendationInputs;
    protected Map<GroupOfUsers, SingleGroupRecommendationTaskOutput> singleGroupRecommendationOutputs;

    private final String groupCaseStudyAlias;
    private final int thisExecution;
    private final int thisSplit;

    private final long modelBuildTime;

    public GroupRecommenderSystemResult(
            List<SingleGroupRecommendationTaskInput> singleGroupRecommendationInputs,
            List<SingleGroupRecommendationTaskOutput> singleGroupRecommendationOutputs,
            String caseStudyAlias,
            int thisExecution,
            int thisSplit,
            long modelBuildTime
    ) {

        validateSameGroups(singleGroupRecommendationInputs, singleGroupRecommendationOutputs);

        this.singleGroupRecommendationInputs = singleGroupRecommendationInputs.parallelStream()
                .collect(Collectors.toMap(
                        output -> output.getGroupOfUsers(),
                        output -> output));

        this.singleGroupRecommendationOutputs = singleGroupRecommendationOutputs.parallelStream()
                .collect(Collectors.toMap(
                        output -> output.getGroup(),
                        output -> output));
        this.groupCaseStudyAlias = caseStudyAlias;
        this.thisExecution = thisExecution;
        this.thisSplit = thisSplit;
        this.modelBuildTime = modelBuildTime;
    }

    /**
     * Devuelve el número de grupos que han sido evaluados.
     *
     * @return Número de grupos evaluados
     */
    public int getNumGroups() {
        return singleGroupRecommendationOutputs.size();
    }

    private void validateSameGroups(List<SingleGroupRecommendationTaskInput> singleGroupRecommendationInputs, List<SingleGroupRecommendationTaskOutput> singleGroupRecommendationOutputs) {
        Set<GroupOfUsers> groupsInput = singleGroupRecommendationInputs.stream().map(task -> task.getGroupOfUsers()).collect(Collectors.toSet());
        Set<GroupOfUsers> groupsOutput = singleGroupRecommendationOutputs.stream().map(task -> task.getGroup()).collect(Collectors.toSet());

        boolean equals = groupsInput.equals(groupsOutput);

        if (!equals) {

            Set<GroupOfUsers> inInputButNotInOutput = groupsInput.stream().filter(groupOfUsers -> !groupsOutput.contains(groupOfUsers)).collect(Collectors.toSet());
            Set<GroupOfUsers> inOutputButNotInInput = groupsOutput.stream().filter(groupOfUsers -> !groupsInput.contains(groupOfUsers)).collect(Collectors.toSet());

            Global.showWarning("Groups found in Input But Not In Output: " + inInputButNotInOutput.toString());
            Global.showWarning("Groups found in Output But Not In Input: " + inOutputButNotInInput.toString());

            throw new IllegalArgumentException("Groups in the list of GRS input and in the GRS output are different!.");
        }
    }

    public Collection<GroupOfUsers> getGroupsOfUsers() {
        return singleGroupRecommendationInputs.keySet().stream().collect(Collectors.toList());
    }

    public Collection<SingleGroupRecommendationTaskInput> inputsIterator() {
        return singleGroupRecommendationInputs.values().stream().collect(Collectors.toList());
    }

    public Collection<SingleGroupRecommendationTaskOutput> outputsIterator() {
        return singleGroupRecommendationOutputs.values().stream().collect(Collectors.toList());
    }

    public SingleGroupRecommendationTaskInput getGroupInput(GroupOfUsers groupOfUsers) {
        if (singleGroupRecommendationInputs.containsKey(groupOfUsers)) {
            return singleGroupRecommendationInputs.get(groupOfUsers);
        } else {
            throw new IllegalArgumentException("Group '" + groupOfUsers.toString() + "' not in the input list");
        }
    }

    public SingleGroupRecommendationTaskOutput getGroupOutput(GroupOfUsers groupOfUsers) {
        if (singleGroupRecommendationOutputs.containsKey(groupOfUsers)) {
            return singleGroupRecommendationOutputs.get(groupOfUsers);
        } else {
            throw new IllegalArgumentException("Group '" + groupOfUsers.toString() + "' not in the output list");
        }
    }

    /**
     * @return the groupCaseStudyAlias
     */
    public String getGroupCaseStudyAlias() {
        return groupCaseStudyAlias;
    }

    /**
     * @return the thisExecution
     */
    public int getThisExecution() {
        return thisExecution;
    }

    /**
     * @return the thisSplit
     */
    public int getThisSplit() {
        return thisSplit;
    }

    public long getModelBuildTime() {
        return modelBuildTime;
    }

}
