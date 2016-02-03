package delfos.group.grs.penalty.grouper;

import delfos.utils.fuzzyclustering.FuzzyCMeans;
import delfos.utils.fuzzyclustering.vector.DataVector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.FloatParameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.similaritymeasures.BasicSimilarityMeasure;
import delfos.similaritymeasures.CosineCoefficient;
import delfos.similaritymeasures.Tanimoto;

/**
 *
 * @version 18-sep-2014
* @author Jorge Castro Gallardo
 */
public class GrouperByDataClustering extends Grouper {

    public static final Parameter NUM_ITEMS = new Parameter("NUM_ITEMS", new IntegerParameter(2, 1000, 3));
    public static final Parameter NUM_CLUSTERS = new Parameter("NUM_CLUSTERS", new IntegerParameter(2, 1000, 3));
    public static final Parameter NUM_ROUNDS = new Parameter("NUM_ROUNDS", new IntegerParameter(2, 1000, 3));
    public static final Parameter FUZZINESS = new Parameter("FUZZINESS", new FloatParameter(2, 1000, 3));

    private static final Parameter SIMILARITY_FOR_CLUSTERING = new Parameter(
            "SIMILARITY_FOR_CLUSTERING",
            new ParameterOwnerRestriction(BasicSimilarityMeasure.class, new CosineCoefficient())
    );

    private int oldNumItems = 3;
    private int oldNumClusters = 3;
    private int oldNumRounds = 10;
    private double oldFuzzyness = 2;
    private BasicSimilarityMeasure oldBasicSimilarityMeasure = new Tanimoto();

    public GrouperByDataClustering() {
        super();
        addParameter(NUM_ITEMS);
        addParameter(NUM_CLUSTERS);
        addParameter(NUM_ROUNDS);
        addParameter(FUZZINESS);
        addParameter(SIMILARITY_FOR_CLUSTERING);

        addParammeterListener(() -> {
            int newNumItems = (Integer) getParameterValue(NUM_ITEMS);
            int newNumClusters = (Integer) getParameterValue(NUM_CLUSTERS);
            int newNumRounds = (Integer) getParameterValue(NUM_ROUNDS);
            double newFuzzyness = ((Number) getParameterValue(FUZZINESS)).doubleValue();

            BasicSimilarityMeasure basicSimilarityMeasure = (BasicSimilarityMeasure) getParameterValue(SIMILARITY_FOR_CLUSTERING);

            String oldParametersAlias = this.getClass().getSimpleName()
                    + "_i=" + oldNumItems
                    + "_c=" + oldNumClusters
                    + "_r=" + oldNumRounds
                    + "_f=" + oldFuzzyness
                    + "_s=" + oldBasicSimilarityMeasure.getAlias();

            String newParametersAlias = this.getClass().getSimpleName()
                    + "_i=" + newNumItems
                    + "_c=" + newNumClusters
                    + "_r=" + newNumRounds
                    + "_f=" + newFuzzyness
                    + "_s=" + basicSimilarityMeasure.getAlias();

            if (!oldParametersAlias.equals(newParametersAlias)) {
                oldNumItems = newNumItems;
                oldNumClusters = newNumClusters;
                oldNumRounds = newNumRounds;
                oldFuzzyness = newFuzzyness;
                oldBasicSimilarityMeasure = basicSimilarityMeasure;

                setAlias(newParametersAlias);
            }

        });
    }

    public GrouperByDataClustering(
            int numItems, int numClusters, int numRounds, double fuzzyness, BasicSimilarityMeasure similarityForClustering) {
        this();
        setParameterValue(NUM_ITEMS, numItems);
        setParameterValue(NUM_CLUSTERS, numClusters);
        setParameterValue(NUM_ROUNDS, numRounds);
        setParameterValue(FUZZINESS, fuzzyness);
        setParameterValue(SIMILARITY_FOR_CLUSTERING, similarityForClustering);
    }

    @Override
    public Collection<Collection<Integer>> groupUsers(RatingsDataset<? extends Rating> ratingsDataset, Set<Integer> users) {

        int numItems = (Integer) getParameterValue(NUM_ITEMS);
        int numClusters = (Integer) getParameterValue(NUM_CLUSTERS);
        int numRounds = (Integer) getParameterValue(NUM_ROUNDS);
        double fuzzyness = ((Number) getParameterValue(FUZZINESS)).doubleValue();
        final BasicSimilarityMeasure similarityForClustering = (BasicSimilarityMeasure) getParameterValue(SIMILARITY_FOR_CLUSTERING);

        FuzzyCMeans<Integer, Integer> fuzzyCMeans = new FuzzyCMeans<>(new DistanceFromSimilarity(similarityForClustering));

        Map<Integer, DataVector<Integer>> clusteringInputData = getClusterInputData(ratingsDataset);

        for (Iterator<Integer> it = clusteringInputData.keySet().iterator(); it.hasNext();) {
            int idElementToCluster = it.next();

            if (!users.contains(idElementToCluster)) {
                it.remove();
            }
        }

        List<List<Integer>> groupedItems = fuzzyCMeans.getCrispClusteredItems(clusteringInputData,
                numClusters,
                numRounds,
                fuzzyness);

        Collection<Collection<Integer>> splittedItemsNumItems = splitInPartitions(groupedItems, numItems);

        return splittedItemsNumItems;
    }

    public Map<Integer, DataVector<Integer>> getClusterInputData(RatingsDataset<? extends Rating> ratings) {
        Map<Integer, DataVector<Integer>> clusteringInputData = new TreeMap<>();
        for (int idUser : ratings.allUsers()) {
            try {
                Map<Integer, Double> userRatings = new TreeMap<>();
                for (Map.Entry<Integer, ? extends Rating> entry : ratings.getUserRatingsRated(idUser).entrySet()) {
                    int idItem = entry.getKey();
                    Double rating = entry.getValue().getRatingValue().doubleValue();
                    userRatings.put(idItem, rating);
                }
                clusteringInputData.put(idUser, new DataVector<>(userRatings));
            } catch (UserNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            }
        }
        return clusteringInputData;
    }

    public static Collection<Collection<Integer>> splitInPartitions(
            List<List<Integer>> groupedItems, int numItems) {
        Collection<Collection<Integer>> allPartitions = new ArrayList<>();

        for (Collection<Integer> clusterItems : groupedItems) {
            TreeSet<Integer> itemsToSplit = new TreeSet<>(clusterItems);

            while (!itemsToSplit.isEmpty()) {
                TreeSet<Integer> partition = new TreeSet<>();
                for (Iterator<Integer> it = itemsToSplit.iterator(); it.hasNext();) {
                    int idItem = it.next();

                    partition.add(idItem);
                    it.remove();

                    if (partition.size() == numItems) {
                        break;
                    }
                }
                allPartitions.add(partition);
            }
        }
        return allPartitions;
    }

}
