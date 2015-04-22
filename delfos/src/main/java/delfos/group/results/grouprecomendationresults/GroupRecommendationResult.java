package delfos.group.results.grouprecomendationresults;

import delfos.group.groupsofusers.GroupOfUsers;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Almacena los resultados de recomendaciones de un sistema de recomendación a
 * grupos
 *
 * @author Jorge Castro Gallardo
 *
 * @version Unknown date
 */
public class GroupRecommendationResult implements Iterable<Entry<GroupOfUsers, List<Recommendation>>> {

    /**
     * Almacena, para cada grupo de usuarios, su lista de recomendaciones
     */
    protected Map<GroupOfUsers, List<Recommendation>> recommendationResults;
    private final long seed;
    /**
     * Tiempo de construcción del modelo para este resultado.
     */
    private final long buildTime;
    /**
     * Tiempo de recomendación para este resultado.
     */
    private final long recommendationTime;

    private final long groupBuildTime;

    /**
     * Almacena las solicitudes de recomendación que se hicieron, indexadas por
     * grupo.
     */
    private final Map<GroupOfUsers, Collection<Integer>> requests;
    private final String caseAlias;

    /**
     * Devuelve el número de grupos que han sido evaluados.
     *
     * @return Número de grupos evaluados
     */
    public int getNumGroups() {
        return recommendationResults.size();
    }

    /**
     * Vacia todas las estructuras que se utilizan
     */
    public void clear() {
        recommendationResults.clear();
    }

    /**
     * Añade las recomendaciones de un grupo de usuarios a los resultados
     *
     * @param groupOfUsers Grupo de usuarios para el que se añaden las
     * recomendaciones
     * @param recomendations lista de recomendaciones que se le dan al grupo
     * ordenadas por relevancia (similitud o valoración predicha)
     */
    public void add(GroupOfUsers groupOfUsers, Collection<Recommendation> recomendations) {
        ArrayList<Recommendation> recomendationList = new ArrayList<>(recomendations);

        Collections.sort(recomendationList);

        recommendationResults.put(groupOfUsers, recomendationList);
    }

    /**
     * Constructor de la clase, que crea unos resultados de ejecución vacíos
     *
     * @param seed
     * @param buildTime Tiempo de construcción del modelo para calcular los
     * resultados que este objeto almacena.
     * @param groupBuildTime
     * @param groupRecommendationTime
     * @param results Resultados de recomendación.
     * @param requests
     * @param caseAlias
     */
    public GroupRecommendationResult(long seed, long buildTime, long groupBuildTime, long groupRecommendationTime, Map<GroupOfUsers, Collection<Integer>> requests, Map<GroupOfUsers, Collection<Recommendation>> results, String caseAlias) {
        this.recommendationResults = new TreeMap<>();

        for (GroupOfUsers group : results.keySet()) {
            ArrayList<Recommendation> groupRecommendationsSorted = new ArrayList<>(results.get(group));
            Collections.sort(groupRecommendationsSorted);
            recommendationResults.put(group, groupRecommendationsSorted);
        }
        this.seed = seed;
        this.buildTime = buildTime;
        this.groupBuildTime = groupBuildTime;
        this.recommendationTime = groupRecommendationTime;
        this.requests = new TreeMap<>(requests);
        this.caseAlias = caseAlias;

    }

    @Override
    public Iterator<Entry<GroupOfUsers, List<Recommendation>>> iterator() {
        return recommendationResults.entrySet().iterator();
    }

    public long getBuildTime() {
        return buildTime;
    }

    public long getGroupBuildTime() {
        return groupBuildTime;
    }

    public long getRecommendationTime() {
        return recommendationTime;
    }

    /**
     * Devuelve las solicitudes que se hicieron para este grupo.
     *
     * @param group
     * @return
     */
    public Collection<Integer> getRequests(GroupOfUsers group) {
        Collection<Integer> ret = requests.get(group);
        return ret;
    }

    public long getSeed() {
        return seed;
    }

    public String getCaseAlias() {
        return caseAlias;
    }

}
