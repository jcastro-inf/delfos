package delfos.group.results.grouprecomendationresults;

import delfos.common.Global;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskInput;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskOutput;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Almacena los resultados de recomendaciones de un sistema de recomendación a
 * grupos
 *
 * @author Jorge Castro Gallardo
 */
public class GroupRecommenderSystemResult implements Iterable<GroupOfUsers> {

    protected List<SingleGroupRecommendationTaskInput> singleGroupRecommendationInputs;
    protected List<SingleGroupRecommendationTaskOutput> singleGroupRecommendationOutputs;

    private final String groupCaseStudyAlias;
    private final int thisExecution;
    private final int thisSplit;

    public GroupRecommenderSystemResult(List<SingleGroupRecommendationTaskInput> singleGroupRecommendationInputs, List<SingleGroupRecommendationTaskOutput> singleGroupRecommendationOutputs, String caseStudyAlias, int thisExecution, int thisSplit) {

        validateSameGroups(singleGroupRecommendationInputs, singleGroupRecommendationOutputs);

        this.singleGroupRecommendationInputs = singleGroupRecommendationInputs;
        this.singleGroupRecommendationOutputs = singleGroupRecommendationOutputs;
        this.groupCaseStudyAlias = caseStudyAlias;
        this.thisExecution = thisExecution;
        this.thisSplit = thisSplit;
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

    @Override
    public Iterator<GroupOfUsers> iterator() {
        return singleGroupRecommendationInputs.stream().map(task -> task.getGroupOfUsers()).iterator();
    }

    public Iterator<SingleGroupRecommendationTaskInput> inputsIterator() {
        return singleGroupRecommendationInputs.iterator();
    }

    public Iterator<SingleGroupRecommendationTaskOutput> outputsIterator() {
        return singleGroupRecommendationOutputs.iterator();
    }

    public SingleGroupRecommendationTaskInput getGroupInput(GroupOfUsers groupOfUsers) {
        Optional<SingleGroupRecommendationTaskInput> findAny = singleGroupRecommendationInputs.stream().filter(singleGroupRecommendationInput -> singleGroupRecommendationInput.getGroupOfUsers().equals(groupOfUsers)).findAny();

        if (findAny.isPresent()) {
            return findAny.get();
        } else {
            throw new IllegalArgumentException("Group '" + groupOfUsers.toString() + "' not in the input list");
        }
    }

    public SingleGroupRecommendationTaskOutput getGroupOutput(GroupOfUsers groupOfUsers) {
        Optional<SingleGroupRecommendationTaskOutput> findAny = singleGroupRecommendationOutputs.stream().filter(singleGroupRecommendationOutput -> singleGroupRecommendationOutput.getGroup().equals(groupOfUsers)).findAny();

        if (findAny.isPresent()) {
            return findAny.get();
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

}
