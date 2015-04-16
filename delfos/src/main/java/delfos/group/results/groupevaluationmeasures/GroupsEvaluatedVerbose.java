package delfos.group.results.groupevaluationmeasures;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.jdom2.Element;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.io.xml.parameterowner.ParameterOwnerXML;
import delfos.rs.recommendation.Recommendation;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommendationResult;

/**
 * Medida de evaluación que muestra los grupos que se evaluaron y los usuarios
 * que hay en cada uno de los grupos evaluados. Asimismo añade algunas
 * estadísticas generales sobre los mismos.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 15-Jan-2013
 */
public class GroupsEvaluatedVerbose extends GroupEvaluationMeasure {

    @Override
    public GroupMeasureResult getMeasureResult(GroupRecommendationResult recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {
        MeanIterative meanMembers = new MeanIterative();
        //todo: implementarlo

        Map<Integer, Integer> numGroupsWithKeyMembers = new TreeMap<Integer, Integer>();

        Element allGroupsDescribed = ParameterOwnerXML.getElement(this);

        for (Iterator<Entry<GroupOfUsers, List<Recommendation>>> it = recommendationResults.iterator(); it.hasNext();) {
            Entry<GroupOfUsers, List<Recommendation>> entry = it.next();

            GroupOfUsers group = entry.getKey();

            if (!numGroupsWithKeyMembers.containsKey(group.size())) {
                numGroupsWithKeyMembers.put(group.size(), 0);
            }
            numGroupsWithKeyMembers.put(group.size(), numGroupsWithKeyMembers.get(group.size()) + 1);

            meanMembers.addValue(group.size());

            Element groupElement = new Element("group");
            groupElement.setAttribute("members", group.toString());

            allGroupsDescribed.addContent(groupElement);
        }

        for (Entry<Integer, Integer> entry : numGroupsWithKeyMembers.entrySet()) {
            Integer size = entry.getKey();
            Integer numGroups = entry.getValue();

            allGroupsDescribed.setAttribute("members_" + size, Integer.toString(numGroups));
        }

        return new GroupMeasureResult(this, (float) meanMembers.getMean(), allGroupsDescribed);
    }

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }
}
