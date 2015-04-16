package delfos.group.grouplevelcasestudy;

import java.util.Map;
import java.util.TreeMap;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.groupsofusers.measuresovergroups.GroupMeasure;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupMeasureResult;

/**
 *
* @author Jorge Castro Gallardo
 * @version 1.0 03-Jun-2013
 */
public class GroupLevelResults {

    private final Map<GroupMeasure, Double> groupMeasures = new TreeMap<GroupMeasure, Double>();
    private final Map<GroupRecommenderSystem,Map<GroupEvaluationMeasure, GroupMeasureResult>> evaluationMeasures = new TreeMap<GroupRecommenderSystem, Map<GroupEvaluationMeasure, GroupMeasureResult>>();
    private final GroupOfUsers group;

    public GroupLevelResults(GroupOfUsers group) {
        this.group = group;

    }

    public void setGroupMeasure(GroupMeasure groupMeasure, double groupMeasureValue) {
        groupMeasures.put(groupMeasure, groupMeasureValue);
    }

    public void setEvaluationMeasure(GroupRecommenderSystem groupRecommenderSystem, GroupEvaluationMeasure evaluationMeasure, GroupMeasureResult measureResult) {
        if(!this.evaluationMeasures.containsKey(groupRecommenderSystem)){
            this.evaluationMeasures.put(groupRecommenderSystem, new TreeMap<GroupEvaluationMeasure, GroupMeasureResult>());
        }
        this.evaluationMeasures.get(groupRecommenderSystem).put(evaluationMeasure, measureResult);
    }

    public double getGroupMeasureValue(GroupMeasure groupMeasure) {
        return groupMeasures.get(groupMeasure);
    }

    public GroupMeasureResult getEvaluationMeasureValue(GroupRecommenderSystem groupRecommenderSystem, GroupEvaluationMeasure groupEvaluationMeasure) {
        return evaluationMeasures.get(groupRecommenderSystem).get(groupEvaluationMeasure);
    }
}
