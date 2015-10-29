package delfos.group.experiment.validation.groupformation;

import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.similaritymeasures.useruser.UserUserSimilarity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Crea grupos buscando similitudes entre las preferencias de los miembros. Los
 * grupos no tienen usuarios en común, es decir, la intersección entre cualquier
 * par de grupos generados (de una vez) es siempre el conjunto vacío.
 *
 * @version 10-abr-2014
 * @author Jorge Castro Gallardo
 */
public class SimilarMembers_OnlyNGroups extends GroupFormationTechnique {

    public static final Parameter GROUP_SIZE_PARAMETER = SimilarMembers.GROUP_SIZE_PARAMETER;
    public static final Parameter N_CANDIDATES_PARAMETER = SimilarMembers.N_CANDIDATES_PARAMETER;
    public static final Parameter SIMILARITY_MEASURE = SimilarMembers.SIMILARITY_MEASURE;

    public static final Parameter NUM_GROUPS_PARAMETER = new Parameter("numGroups", new IntegerParameter(1, 1000000, 5));

    /**
     * Genera una validación de usuarios que genera grupos de tamaño fijo. Por
     * defecto, el tamaño de los grupos es de cuatro miembros.
     */
    public SimilarMembers_OnlyNGroups() {
        super();
        addParameter(GROUP_SIZE_PARAMETER);
        addParameter(NUM_GROUPS_PARAMETER);
        addParameter(N_CANDIDATES_PARAMETER);
        addParameter(SIMILARITY_MEASURE);

        addParammeterListener(() -> {
            int numGroupsValue = (Integer) getParameterValue(NUM_GROUPS_PARAMETER);
            int groupSizeValue = (Integer) getParameterValue(GROUP_SIZE_PARAMETER);

            String oldAlias = getAlias();
            String newAlias = this.getClass().getSimpleName() + "(num=" + numGroupsValue + " size=" + groupSizeValue + ")";

            if (!oldAlias.equals(newAlias)) {
                setAlias(newAlias);
            }
        });
    }

    /**
     * Genera una técnica de generación de grupos que genera
     * <b>numGroupsValue</b> de tamaño <b>groupSizeValue</b> y con
     *
     * @param groupSizeValue Tamaño de los grupos generados
     * @param numGroupsValue Número de grupos considerados
     */
    public SimilarMembers_OnlyNGroups(int numGroupsValue, int groupSizeValue) {
        this(numGroupsValue, groupSizeValue, 20);

    }

    /**
     * Genera una validación de usuarios que genera grupos de tamaño fijo. Por
     * defecto, el tamaño de los grupos es de cuatro miembros.
     *
     * @param groupSizeValue Tamaño de los grupos generados
     * @param numGroupsValue Número de grupos considerados
     * @param numCandidates Número de vecinos candidatos que se consideran al
     * seleccionar el siguiente miembro del grupo aleatoriamente.
     */
    public SimilarMembers_OnlyNGroups(int numGroupsValue, int groupSizeValue, int numCandidates) {
        this();
        setParameterValue(GROUP_SIZE_PARAMETER, groupSizeValue);
        setParameterValue(NUM_GROUPS_PARAMETER, numGroupsValue);
        setParameterValue(N_CANDIDATES_PARAMETER, numCandidates);

    }

    @Override
    public Collection<GroupOfUsers> shuffle(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset {
        if (datasetLoader == null) {
            throw new IllegalStateException("The datasetLoader is null.");
        }
        Random random = new Random(getSeedValue());

        final int numMembersCandidate = (Integer) getParameterValue(N_CANDIDATES_PARAMETER);
        final int groupSize = (Integer) getParameterValue(GROUP_SIZE_PARAMETER);
        final int numGroups = (Integer) getParameterValue(NUM_GROUPS_PARAMETER);
        final int maximumGroups = datasetLoader.getRatingsDataset().allUsers().size() / groupSize;

        if (maximumGroups < numGroups) {
            throw new IllegalArgumentException("The number of groups * groupSize exceed the number of users (" + numGroups + " * " + groupSize + " > " + datasetLoader.getRatingsDataset().allUsers().size());
        }

        UserUserSimilarity similarityMeasure = (UserUserSimilarity) getParameterValue(SIMILARITY_MEASURE);

        ArrayList<GroupOfUsers> groupsGenerated = new ArrayList<>();
        ArrayList<Integer> usersRemainToSelect = new ArrayList<>(datasetLoader.getRatingsDataset().allUsers());

        int numGruposGenerados = 0;
        while (groupsGenerated.size() < numGroups) {

            TreeSet<Integer> usersGrupoActual = new TreeSet<>();
            {
                int index = random.nextInt(usersRemainToSelect.size());
                Integer idUser = usersRemainToSelect.remove(index);
                usersGrupoActual.add(idUser);
            }

            List<Neighbor> similaritiesToGroupStablished = usersRemainToSelect.parallelStream()
                    .map(idCandidateMember -> {
                        Double similarityToGroup = usersGrupoActual.parallelStream()
                        .map(idMember -> similarityMeasure.similarity(datasetLoader, idCandidateMember, idMember))
                        .map(similarity -> {
                            if (similarity <= 0) {
                                return 0.0;
                            } else {
                                return similarity;
                            }
                        })
                        .reduce((similarityA, similarityB) -> similarityA * similarityB).get();
                        return new Neighbor(RecommendationEntity.USER, idCandidateMember, similarityToGroup);
                    })
                    .sorted(Neighbor.BY_SIMILARITY_DESC)
                    .collect(Collectors.toList());

            while (usersGrupoActual.size() < groupSize) {

                //Elijo aleatoriamente el siguiente entre los n con mayor similitud.
                int indexSelected = random.nextInt(Math.min(similaritiesToGroupStablished.size(), numMembersCandidate));
                Neighbor neighborSelected = similaritiesToGroupStablished.remove(indexSelected);
                int idNewMember = neighborSelected.getIdNeighbor();
                double similarity = neighborSelected.getSimilarity();

                if (Global.isVerboseAnnoying()) {
                    Global.showInfoMessage("Selected user " + idNewMember + ", similarity of " + similarity + " --> Group: " + usersGrupoActual + "\n");
                }

                boolean removed = usersRemainToSelect.remove((Integer) idNewMember);

                //update neighbors
                similaritiesToGroupStablished = similaritiesToGroupStablished.parallelStream()
                        .map(idCandidateMember -> {
                            double similarityWithNewMember = similarityMeasure.similarity(datasetLoader, idCandidateMember.getIdNeighbor(), idNewMember);

                            if (similarityWithNewMember <= 0) {
                                similarityWithNewMember = 0;
                            }

                            similarityWithNewMember *= idCandidateMember.getSimilarity();
                            return new Neighbor(RecommendationEntity.USER, idCandidateMember.getIdNeighbor(), similarityWithNewMember);
                        })
                        .sorted(Neighbor.BY_SIMILARITY_DESC)
                        .collect(Collectors.toList());

                usersGrupoActual.add(idNewMember);
            }
            groupsGenerated.add(new GroupOfUsers(usersGrupoActual));
            numGruposGenerados++;
            progressChanged("Group generation", (numGruposGenerados * 100) / numGroups);
        }

        while (groupsGenerated.size() > numGroups) {
            groupsGenerated.remove(random.nextInt(groupsGenerated.size()));
        }
        GroupOfUsers[] gruposGenerados = new GroupOfUsers[groupsGenerated.size()];

        numGruposGenerados = 0;
        for (GroupOfUsers grupoActual : groupsGenerated) {
            gruposGenerados[numGruposGenerados] = grupoActual;
            numGruposGenerados++;
        }

        progressChanged("Group generation", 100);
        return Arrays.asList(gruposGenerados);
    }
}
