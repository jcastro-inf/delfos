package delfos.group.experiment.validation.groupformation;

import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
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
     * Genera una validación de usuarios que genera grupos de tamaño fijo. Por
     * defecto, el tamaño de los grupos es de cuatro miembros.
     *
     * @param groupSizeValue Tamaño de los grupos generados
     * @param numGroupsValue Número de grupos considerados
     */
    public SimilarMembers_OnlyNGroups(int numGroupsValue, int groupSizeValue) {
        this();
        setParameterValue(GROUP_SIZE_PARAMETER, groupSizeValue);
        setParameterValue(NUM_GROUPS_PARAMETER, numGroupsValue);

    }

    @Override
    public synchronized Collection<GroupOfUsers> shuffle(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset {
        if (datasetLoader == null) {
            throw new IllegalStateException("The datasetLoader is null.");
        }
        Random random = new Random(getSeedValue());

        final int groupSize = (Integer) getParameterValue(GROUP_SIZE_PARAMETER);
        final int numGroups = (Integer) getParameterValue(NUM_GROUPS_PARAMETER);
        final int maximumGroups = datasetLoader.getRatingsDataset().allUsers().size() / groupSize;

        if (maximumGroups < numGroups) {
            throw new IllegalArgumentException("The number of groups * groupSize exceed the number of users (" + numGroups + " * " + groupSize + " > " + datasetLoader.getRatingsDataset().allUsers().size());
        }

        UserUserSimilarity similarityMeasure = (UserUserSimilarity) getParameterValue(SIMILARITY_MEASURE);

        ArrayList<GroupOfUsers> groupsGenerated = new ArrayList<>();
        ArrayList<User> usersRemainToSelect = new ArrayList<>(datasetLoader.getUsersDataset());

        int numGruposGenerados = 0;
        while (groupsGenerated.size() < numGroups) {
            TreeSet<Integer> usersThisGroup = new TreeSet<>();

            if (usersRemainToSelect.size() < groupSize) {
                throw new IllegalStateException("No more users!!");
            }

            User firstMember;
            {
                int indexToRemove = random.nextInt(usersRemainToSelect.size());
                firstMember = usersRemainToSelect.remove(indexToRemove);
                usersThisGroup.add(firstMember.getId());
            }

            List<Neighbor> similarityToFirstUser = usersRemainToSelect.parallelStream()
                    .map(idCandidateMember -> {
                        double similarity = similarityMeasure.similarity(datasetLoader, idCandidateMember.getId(), firstMember.getId());
                        return new Neighbor(RecommendationEntity.USER, idCandidateMember, similarity);
                    })
                    .sorted(Neighbor.BY_SIMILARITY_DESC)
                    .collect(Collectors.toList());

            List<Neighbor> usersToAdd = similarityToFirstUser.subList(0, groupSize - 1);

            for (Neighbor newMember : usersToAdd) {
                usersThisGroup.add(newMember.getIdNeighbor());
                boolean remove = usersRemainToSelect.remove((User) newMember.getNeighbor());
                if (!remove) {
                    throw new IllegalStateException("asdfasdf");
                }
            }

            groupsGenerated.add(new GroupOfUsers(usersThisGroup));
            numGruposGenerados++;
            progressChanged("Group generation", (numGruposGenerados * 100) / numGroups);
        }

        GroupOfUsers[] gruposGenerados = new GroupOfUsers[groupsGenerated.size()];

        numGruposGenerados = 0;
        for (GroupOfUsers grupoActual : groupsGenerated) {
            gruposGenerados[numGruposGenerados] = grupoActual;
            numGruposGenerados++;
        }

        progressChanged(
                "Group generation", 100);
        return Arrays.asList(gruposGenerados);
    }
}
