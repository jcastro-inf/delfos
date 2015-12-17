package delfos.group.results.groupevaluationmeasures;

import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.io.xml.parameterowner.ParameterOwnerXML;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.jdom2.Element;

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
    public GroupEvaluationMeasureResult getMeasureResult(GroupRecommenderSystemResult groupRecommenderSystemResult, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {
        MeanIterative meanMembers = new MeanIterative();
        //todo: implementarlo

        Map<Integer, Integer> numGroupsWithKeyMembers = new TreeMap<>();

        Element allGroupsDescribed = ParameterOwnerXML.getElement(this);

        for (GroupOfUsers groupOfUsers : groupRecommenderSystemResult) {

            if (!numGroupsWithKeyMembers.containsKey(groupOfUsers.size())) {
                numGroupsWithKeyMembers.put(groupOfUsers.size(), 0);
            }
            numGroupsWithKeyMembers.put(groupOfUsers.size(), numGroupsWithKeyMembers.get(groupOfUsers.size()) + 1);

            meanMembers.addValue(groupOfUsers.size());

            Element groupElement = new Element("group");
            groupElement.setAttribute("members", groupOfUsers.toString());

            allGroupsDescribed.addContent(groupElement);
        }

        for (Entry<Integer, Integer> entry : numGroupsWithKeyMembers.entrySet()) {
            Integer size = entry.getKey();
            Integer numGroups = entry.getValue();

            allGroupsDescribed.setAttribute("members_" + size, Integer.toString(numGroups));
        }

        return new GroupEvaluationMeasureResult(this, (float) meanMembers.getMean(), allGroupsDescribed);
    }

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }
}
