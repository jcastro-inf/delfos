package delfos.group.results.groupevaluationmeasures.detailed;

import delfos.Constants;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.jdom2.Element;

/**
 * @author Jorge Castro Gallardo
 */
public class RecommendationsWithNeighborToXML {

    public static final String NEIGHBORS_ELEMENT_NAME = "neighbors";
    public static final String ID_TARGET_ATTRIBUTE_NAME = "idTarget";

    public static final String ID_NEIGHBOR_TTRIBUTE_NAME = "idItem";
    public static final String SIMILARITY_ATTRIBUTE_NAME = "preference";
    public static final String RANK_ATTRIBUTE_NAME = "rank";

    public static Element getNeighborsElement(String targetId, List<Neighbor> neighbors) {

        Element element = new Element(NEIGHBORS_ELEMENT_NAME);
        element.setAttribute(ID_TARGET_ATTRIBUTE_NAME, targetId);

        ArrayList<Neighbor> neighborsSortedById = new ArrayList<>(neighbors);
        Collections.sort(neighborsSortedById, Neighbor.BY_ID);

        List<Neighbor> neighborsSortedBySimilarity = new ArrayList<>(neighbors);
        Collections.sort(neighborsSortedBySimilarity, Neighbor.BY_SIMILARITY_DESC);

        Map<Neighbor, Integer> neighborsRank = new TreeMap<>();
        neighborsSortedBySimilarity.stream().forEachOrdered(neighbor -> {
            neighborsRank.put(neighbor, neighborsRank.size() + 1);
        });

        neighborsSortedById.stream().forEachOrdered((neighbor) -> {
            element.addContent(getNeighborElement(neighbor, neighborsRank.get(neighbor)));
        });

        if (Constants.isRawResultDefined()) {
            Element rawDataElement = new Element(NEIGHBORS_ELEMENT_NAME + "_RAW");
            rawDataElement.setAttribute(ID_TARGET_ATTRIBUTE_NAME, targetId);
            StringBuilder rawData = new StringBuilder();
            neighborsSortedById.stream().forEachOrdered((neighbor) -> {
                rawData.append(neighbor.getIdNeighbor()).append("\t").append(neighbor.getSimilarity()).append("\t").append(neighborsRank.get(neighbor)).append("\n");
            });
            rawDataElement.addContent(rawData.toString());

            element.addContent(rawDataElement);
        }
        return element;
    }

    private static Element getNeighborElement(Neighbor neighbor, int rank) {
        Element neighborElement = new Element("neighbor");

        neighborElement.setAttribute(ID_NEIGHBOR_TTRIBUTE_NAME, Integer.toString(neighbor.getIdNeighbor()));
        neighborElement.setAttribute(SIMILARITY_ATTRIBUTE_NAME, Float.toString(neighbor.getSimilarity()));
        neighborElement.setAttribute(RANK_ATTRIBUTE_NAME, Integer.toString(rank));

        return neighborElement;
    }
}
