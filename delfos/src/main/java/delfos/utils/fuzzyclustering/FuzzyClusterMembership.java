package delfos.utils.fuzzyclustering;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @version 16-sep-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * @param <IdUser>
 * @param <IdItem>
 */
public class FuzzyClusterMembership<IdUser, IdItem> {

    Map<FuzzyCluster<IdItem>, Map<IdUser, Double>> membershipOfAllDataPoints;
    Set<FuzzyCluster<IdItem>> clusters;
    Set<IdUser> dataPoints;
    Set<IdItem> dataPointsElements;

    public FuzzyClusterMembership(Map<FuzzyCluster<IdItem>, Map<IdUser, Double>> membership) {
        clusters = new TreeSet<>(membership.keySet());

        dataPoints = new TreeSet<>();
        for (Map<IdUser, Double> thisClusterMembership : membership.values()) {
            dataPoints.addAll(thisClusterMembership.keySet());
        }

        dataPointsElements = new TreeSet<>();
        for (FuzzyCluster<IdItem> fuzzyCluster : membership.keySet()) {
            dataPointsElements.addAll(fuzzyCluster.centroid().keySet());
        }

        membershipOfAllDataPoints = new TreeMap<>(membership);

        checkIntegrity(membershipOfAllDataPoints, clusters, dataPoints);
    }

    public double getMembership(IdUser idUser, FuzzyCluster<IdItem> cluster) {
        if (!membershipOfAllDataPoints.containsKey(cluster)) {
            throw new IllegalStateException("Cluster not found.");
        }

        if (!membershipOfAllDataPoints.get(cluster).containsKey(idUser)) {
            throw new IllegalStateException("DataPoint not found");
        }

        Double thisDataPointMembership = membershipOfAllDataPoints.get(cluster).get(idUser);
        return thisDataPointMembership;
    }

    public double getVariation(FuzzyClusterMembership<IdUser, IdItem> fuzzyClusterMembership) {
        checkComparable(fuzzyClusterMembership);

        double sum = 0;
        int n = 0;

        Iterator<FuzzyCluster<IdItem>> otherClusterIterator = fuzzyClusterMembership.clusters.iterator();
        for (Iterator<FuzzyCluster<IdItem>> thisClusterIterator = clusters.iterator(); thisClusterIterator.hasNext();) {
            FuzzyCluster<IdItem> thisCluster = thisClusterIterator.next();
            FuzzyCluster<IdItem> otherCluster = otherClusterIterator.next();

            for (IdUser dataPoint : dataPoints) {
                double thisMembership = this.getMembership(dataPoint, thisCluster);
                double otherMembership = fuzzyClusterMembership.getMembership(dataPoint, otherCluster);

                sum += Math.abs(thisMembership - otherMembership);
                n++;
            }
        }

        double variation = sum / n;
        return variation;
    }

    public void checkComparable(FuzzyClusterMembership<IdUser, IdItem> fuzzyClusterMembership) throws IllegalStateException {
        if (clusters.size() != fuzzyClusterMembership.clusters.size()) {
            System.out.println(clusters);
            System.out.println(fuzzyClusterMembership.clusters);
            throw new IllegalStateException("Clusters are not equal, cannot get variation from membership!");
        }
        if (!dataPoints.equals(fuzzyClusterMembership.dataPoints)) {
            throw new IllegalStateException("Data points are not equal, cannot get variation from membershiop");
        }

        if (!dataPointsElements.equals(fuzzyClusterMembership.dataPointsElements)) {
            throw new IllegalStateException("Data points elements are not equal, cannot get variation from membershiop");
        }
    }

    private void checkIntegrity(Map<FuzzyCluster<IdItem>, Map<IdUser, Double>> fuzzyClusterMembership, Set<FuzzyCluster<IdItem>> clusters, Set<IdUser> dataPoints) {
        //All cluster should be in the membership map.
        if (!fuzzyClusterMembership.keySet().equals(clusters)) {
            throw new IllegalStateException("Cluster sets are not equal!");
        }

        //A user should be in all clusters, with some membership
        for (IdUser idUser : dataPoints) {
            for (FuzzyCluster<IdItem> cluster : fuzzyClusterMembership.keySet()) {
                if (!fuzzyClusterMembership.get(cluster).containsKey(idUser)) {
                    throw new IllegalStateException("Cluster " + cluster + " does not have a membership for dataPoint " + idUser);
                }
            }
        }
    }

    /**
     * Normalise the memberships of all datapoint to make all membership of a
     * datapoint sum exactly one.
     */
    public void normaliseMembership() {

        for (IdUser idUser : dataPoints) {
            double sumMembership = 0;

            for (FuzzyCluster<IdItem> cluster : clusters) {
                double thisDataMembership = getMembership(idUser, cluster);
                sumMembership += thisDataMembership;
            }

            double norm = sumMembership;

            for (FuzzyCluster<IdItem> cluster : clusters) {
                double thisDataMembership = getMembership(idUser, cluster);
                double normalisedMembership = thisDataMembership / norm;
                membershipOfAllDataPoints.get(cluster).put(idUser, normalisedMembership);
            }

        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        int i = 1;
        for (FuzzyCluster<IdItem> cluster : clusters) {
            str.append("Cluster ").append(i).append(": ").append(cluster).append("\n");

            for (IdUser idUser : dataPoints) {
                double membership = getMembership(idUser, cluster);
                str.append("\tPoint ").append(idUser).append(" --> ").append(membership).append("\n");
            }
            i++;

        }
        return str.toString();
    }

    public Set<IdUser> getDataPointKeys() {
        return new TreeSet<>(dataPoints);
    }

    public Set<IdItem> getDataPointElements() {
        return new TreeSet<>(dataPointsElements);
    }

    public Set<FuzzyCluster<IdItem>> getClusters() {
        return new TreeSet<>(clusters);
    }

}
