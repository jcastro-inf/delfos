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
package delfos.group.grouplevelcasestudy;

import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.groupsofusers.measuresovergroups.GroupMeasure;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 03-Jun-2013
 */
public class GroupLevelResults {

    private final Map<GroupMeasure, Double> groupMeasures = new TreeMap<>();
    private final Map<GroupRecommenderSystem, Map<GroupEvaluationMeasure, GroupEvaluationMeasureResult>> evaluationMeasures = new TreeMap<>();
    private final GroupOfUsers group;

    public GroupLevelResults(GroupOfUsers group) {
        this.group = group;

    }

    public void setGroupMeasure(GroupMeasure groupMeasure, double groupMeasureValue) {
        groupMeasures.put(groupMeasure, groupMeasureValue);
    }

    public void setEvaluationMeasure(GroupRecommenderSystem groupRecommenderSystem, GroupEvaluationMeasure evaluationMeasure, GroupEvaluationMeasureResult measureResult) {
        if (!this.evaluationMeasures.containsKey(groupRecommenderSystem)) {
            this.evaluationMeasures.put(groupRecommenderSystem, new TreeMap<>());
        }
        this.evaluationMeasures.get(groupRecommenderSystem).put(evaluationMeasure, measureResult);
    }

    public double getGroupMeasureValue(GroupMeasure groupMeasure) {
        return groupMeasures.get(groupMeasure);
    }

    public GroupEvaluationMeasureResult getEvaluationMeasureValue(GroupRecommenderSystem groupRecommenderSystem, GroupEvaluationMeasure groupEvaluationMeasure) {
        return evaluationMeasures.get(groupRecommenderSystem).get(groupEvaluationMeasure);
    }
}
