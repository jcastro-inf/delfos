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
package delfos.utils.fuzzyclustering;

import delfos.utils.fuzzyclustering.distance.DistanceFunction;
import delfos.utils.fuzzyclustering.vector.DataVector;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @version 15-sep-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @param <IdUser>
 * @param <IdItem>
 */
public class FuzzyCMeans<IdUser, IdItem> {

    private final DistanceFunction distanceFunction;

    public FuzzyCMeans(DistanceFunction distanceFunction) {
        this.distanceFunction = distanceFunction;
    }

    public DataVector<IdItem> getMeanVector(Map<IdUser, DataVector<IdItem>> originalVectors) {
        DataVector<IdItem> meanVector = new DataVector<>();
        Set<IdItem> allIdItem = new TreeSet<>();

        for (Map.Entry<IdUser, DataVector<IdItem>> entry : originalVectors.entrySet()) {
            allIdItem.addAll(entry.getValue().keySet());
        }

        for (IdItem idItem : allIdItem) {
            double sum = 0;
            int n = 0;
            for (IdUser idUser : originalVectors.keySet()) {

                if (originalVectors.get(idUser).containsKey(idItem)) {
                    sum += originalVectors.get(idUser).get(idItem);
                    n++;
                }
            }

            if (n != 0) {
                double mean = sum / n;
                meanVector.put(idItem, mean);
            }
        }

        return meanVector;
    }

    private DataVector<IdItem> getFurthestVector(Map<IdUser, DataVector<IdItem>> originalVectors, DataVector<IdItem> firstCluster) {

        DataVector<IdItem> furthestVector = originalVectors.values().iterator().next();
        double furthestDistance = distanceFunction.distance(firstCluster, furthestVector);

        for (DataVector<IdItem> dataVector : originalVectors.values()) {
            double distance = distanceFunction.distance(firstCluster, dataVector);
            if (furthestDistance < distance) {
                furthestVector = dataVector;
            }
        }

        return furthestVector;
    }

    private double getMinDistanceToClusters(Collection<FuzzyCluster<IdItem>> fuzzyClusters, DataVector<IdItem> dataPoint) {
        double minDistance = Double.MAX_VALUE;
        for (FuzzyCluster<IdItem> fuzzyCluster : fuzzyClusters) {
            double distance = distanceFunction.distance(dataPoint, fuzzyCluster.centroid());
            minDistance = Math.min(minDistance, distance);
        }
        return minDistance;
    }

    public DataVector<IdItem> initFirstCluster(Map<IdUser, DataVector<IdItem>> originalVectors) {
        DataVector<IdItem> meanVector = getMeanVector(originalVectors);
        return meanVector;
    }

    public DataVector<IdItem> initSecondCluster(Map<IdUser, DataVector<IdItem>> originalVectors, DataVector<IdItem> firstCluster) {

        DataVector<IdItem> furthestVectorToFirst = getFurthestVector(originalVectors, firstCluster);
        return furthestVectorToFirst;
    }

    public DataVector<IdItem> initOtherCluster(Map<IdUser, DataVector<IdItem>> originalVector, Collection<FuzzyCluster<IdItem>> fuzzyClusters) {

        IdUser selectedIdUser = originalVector.keySet().iterator().next();
        double maxOfMinDistanceToClusters = getMinDistanceToClusters(fuzzyClusters, originalVector.get(selectedIdUser));

        for (IdUser idUser : originalVector.keySet()) {
            double minDistanceToClusters = getMinDistanceToClusters(fuzzyClusters, originalVector.get(idUser));

            if (maxOfMinDistanceToClusters < minDistanceToClusters) {
                maxOfMinDistanceToClusters = minDistanceToClusters;
                selectedIdUser = idUser;
            }
        }
        return new DataVector<>(originalVector.get(selectedIdUser));
    }

    public Set<FuzzyCluster<IdItem>> executeClustering(
            Map<IdUser, DataVector<IdItem>> originalVectors,
            int numClusters,
            int numRounds,
            double fuzziness) {

        try {
            Set<FuzzyCluster<IdItem>> clusters = initClusters(numClusters, originalVectors);

            //FirstIteration.
            FuzzyClusterMembership<IdUser, IdItem> prevMembership = updateMembership(originalVectors, clusters, fuzziness);
            clusters = updateClustersCentroids(originalVectors, clusters, prevMembership, fuzziness);

            //Second and following iterations.
            for (int round = 1; round < numRounds; round++) {

                FuzzyClusterMembership<IdUser, IdItem> thisRoundClusterMembership = updateMembership(originalVectors, clusters, fuzziness);

                double variation = prevMembership.getVariation(thisRoundClusterMembership);

                Set<FuzzyCluster<IdItem>> thisRoundClustersCentroids = updateClustersCentroids(originalVectors, clusters, thisRoundClusterMembership, fuzziness);

                prevMembership = thisRoundClusterMembership;
                clusters = thisRoundClustersCentroids;

                if (variation < 0.001) {
                    break;
                }
            }

            return clusters;
        } catch (Throwable ex) {
            printlnDouble("Clustering failed, input details:");

            printlnDouble("\tnumClusters =" + numClusters);
            printlnDouble("\tnumRounds =" + numRounds);
            printlnDouble("\tfuzzyness=" + fuzziness);

            for (Map.Entry<IdUser, DataVector<IdItem>> entry : originalVectors.entrySet()) {
                IdUser id = entry.getKey();
                DataVector<IdItem> dataVector = entry.getValue();

                printlnDouble("id " + id + " -> " + dataVector);
            }

            throw ex;
        }
    }

    public Set<FuzzyCluster<IdItem>> initClusters(int numClusters, Map<IdUser, DataVector<IdItem>> originalVectors) {
        Set<FuzzyCluster<IdItem>> clusters = new TreeSet<>();

        for (int i = 0; i < numClusters; i++) {
            switch (i) {
                case 0:
                    clusters.add(new FuzzyCluster<>(initFirstCluster(originalVectors)));
                    break;
                case 1:
                    DataVector<IdItem> firstClusterCentroid = clusters.iterator().next().centroid();
                    clusters.add(new FuzzyCluster<>(initSecondCluster(originalVectors, firstClusterCentroid)));
                    break;
                default:
                    clusters.add(new FuzzyCluster<>(initOtherCluster(originalVectors, clusters)));
            }
        }
        return clusters;
    }

    public FuzzyClusterMembership<IdUser, IdItem> updateMembership(Map<IdUser, DataVector<IdItem>> originalVectors, Set<FuzzyCluster<IdItem>> clusters, double fuzziness) {
        Map<FuzzyCluster<IdItem>, Map<IdUser, Double>> membership = new TreeMap<>();

        for (FuzzyCluster<IdItem> cluster : clusters) {
            membership.put(cluster, new TreeMap<IdUser, Double>());
            for (IdUser idUser : originalVectors.keySet()) {
                DataVector<IdItem> dataVector = originalVectors.get(idUser);
                DataVector<IdItem> clusterVector = cluster.centroid();

                double distance = distanceFunction.distance(dataVector, clusterVector);

                if (distance != 0) {
                    double membershipOfDataPointToCluster = Math.pow((1 / distance), (1.0 / (fuzziness - 1)));
                    membership.get(cluster).put(idUser, membershipOfDataPointToCluster);
                } else {
                    membership.get(cluster).put(idUser, 1.0);
                }
            }
        }

        FuzzyClusterMembership<IdUser, IdItem> fuzzyClusterMembership = new FuzzyClusterMembership<>(membership);
        fuzzyClusterMembership.normaliseMembership();

        return fuzzyClusterMembership;
    }

    public Set<FuzzyCluster<IdItem>> updateClustersCentroids(Map<IdUser, DataVector<IdItem>> originalVectors, Set<FuzzyCluster<IdItem>> clusters, FuzzyClusterMembership<IdUser, IdItem> allClustersMemberships, double fuzzyness) {
        Set<FuzzyCluster<IdItem>> updatedClusterCenters = new TreeSet<>();

        for (FuzzyCluster<IdItem> cluster : allClustersMemberships.clusters) {
            Map<IdItem, Double> newCentroid = updateClusterCentroid(allClustersMemberships, originalVectors, cluster, fuzzyness);
            FuzzyCluster<IdItem> newFuzzyCluster = new FuzzyCluster<>(new DataVector<>(newCentroid));
            updatedClusterCenters.add(newFuzzyCluster);
        }
        return updatedClusterCenters;
    }

    public Map<IdItem, Double> updateClusterCentroid(FuzzyClusterMembership<IdUser, IdItem> allClustersMemberships, Map<IdUser, DataVector<IdItem>> originalVectors, FuzzyCluster<IdItem> cluster, double fuzzyness) {

        Map<IdItem, Double> newCentroid = new TreeMap<>();
        for (IdItem idItem : allClustersMemberships.dataPointsElements) {
            double numerator = 0;
            double denominator = 0;
            for (IdUser idUser : allClustersMemberships.dataPoints) {
                if (originalVectors.get(idUser).containsKey(idItem)) {
                    double dataVectorValue = originalVectors.get(idUser).get(idItem);
                    double membershipValue = allClustersMemberships.getMembership(idUser, cluster);

                    numerator += dataVectorValue * Math.pow(membershipValue, fuzzyness);
                    denominator += Math.pow(membershipValue, fuzzyness);
                }
            }
            double newCentroidCoordinate = numerator / denominator;
            newCentroid.put(idItem, newCentroidCoordinate);
        }

        return newCentroid;
    }

    class UserByMembership<IdUser> implements Comparable<UserByMembership<IdUser>> {

        final IdUser idUser;
        final double userMembership;

        public UserByMembership(
                final IdUser idUser,
                final double userMembership) {
            this.idUser = idUser;
            this.userMembership = userMembership;
        }

        @Override
        public String toString() {
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(3);
            df.setMinimumFractionDigits(3);

            return idUser + " (" + df.format(userMembership) + ")";
        }

        @Override
        public int compareTo(UserByMembership<IdUser> o) {
            return -Double.compare(userMembership, o.userMembership);
        }

    }

    public List<List<IdUser>> getCrispClusteredItems(
            Map<IdUser, DataVector<IdItem>> clusteringInputData,
            int numClusters,
            int numRounds,
            double fuzzyness) {

        Set<FuzzyCluster<IdItem>> fuzzyClusters = executeClustering(clusteringInputData, numClusters, numRounds, fuzzyness);
        FuzzyClusterMembership<IdUser, IdItem> fuzzyClusterMembership = updateMembership(clusteringInputData, fuzzyClusters, fuzzyness);

        Map<FuzzyCluster<IdItem>, PriorityQueue<UserByMembership<IdUser>>> bestMembership = new TreeMap<>();
        for (IdUser idUser : fuzzyClusterMembership.getDataPointKeys()) {
            FuzzyCluster<IdItem> bestCluster = fuzzyClusterMembership.clusters.iterator().next();
            double maxMembership = fuzzyClusterMembership.getMembership(idUser, bestCluster);

            for (FuzzyCluster<IdItem> cluster : fuzzyClusterMembership.getClusters()) {
                double membership = fuzzyClusterMembership.getMembership(idUser, cluster);

                if (maxMembership < membership) {
                    maxMembership = membership;
                    bestCluster = cluster;
                }
            }
            if (!bestMembership.containsKey(bestCluster)) {
                bestMembership.put(bestCluster, new PriorityQueue<UserByMembership<IdUser>>());
            }
            bestMembership.get(bestCluster).add(new UserByMembership<>(idUser, maxMembership));
        }

        List<List<IdUser>> groupedItems = new ArrayList<>();
        for (FuzzyCluster<IdItem> fuzzyCluster : bestMembership.keySet()) {
            LinkedList<IdUser> thisClusterList = new LinkedList<>();
            PriorityQueue<UserByMembership<IdUser>> queue = bestMembership.get(fuzzyCluster);

            while (!queue.isEmpty()) {
                UserByMembership<IdUser> poll = queue.poll();
                thisClusterList.add(poll.idUser);
            }

            groupedItems.add(thisClusterList);
        }

        return groupedItems;
    }

    public static void printlnDouble(String msg) {
        System.out.println(msg);
        System.err.println(msg);
    }
}
