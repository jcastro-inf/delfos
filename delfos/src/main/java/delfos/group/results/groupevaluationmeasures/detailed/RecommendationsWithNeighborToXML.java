package delfos.group.results.groupevaluationmeasures.detailed;

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

        ArrayList<Neighbor> neighborsSorted = new ArrayList<>(neighbors);
        Collections.sort(neighborsSorted, Neighbor.BY_ID);

        Map<Neighbor, Integer> neighborsRank = new TreeMap<>();
        List<Neighbor> neighborsSortedBySimilarity = new ArrayList<>(neighbors);
        Collections.sort(neighborsSortedBySimilarity, Neighbor.BY_SIMILARITY_DESC);

        Element rawDataElement = new Element(NEIGHBORS_ELEMENT_NAME + "_RAW");
        rawDataElement.setAttribute(ID_TARGET_ATTRIBUTE_NAME, targetId);
        StringBuilder rawData = new StringBuilder();

        neighborsSortedBySimilarity.stream().forEachOrdered(neighbor -> {
            neighborsRank.put(neighbor, neighborsRank.size() + 1);
        });
        rawDataElement.addContent(rawData.toString());

        neighborsSorted.stream().forEachOrdered((neighbor) -> {
            getNeighborElement(neighbor, neighborsRank.get(neighbor), element);
            rawData.append(neighbor.getIdNeighbor()).append("\t").append(neighbor.getSimilarity()).append("\t").append(neighborsRank.get(neighbor)).append("\n");
        });

        element.addContent(rawDataElement);
        return element;
    }

    private static void getNeighborElement(Neighbor neighbor, int rank, Element element) {
        Element neighborElement = new Element("neighbor");

        neighborElement.setAttribute(ID_NEIGHBOR_TTRIBUTE_NAME, Integer.toString(neighbor.getIdNeighbor()));
        neighborElement.setAttribute(SIMILARITY_ATTRIBUTE_NAME, Float.toString(neighbor.getSimilarity()));
        neighborElement.setAttribute(RANK_ATTRIBUTE_NAME, Integer.toString(rank));

        element.addContent(neighborElement);
    }
}
