package delfos.group.experiment.validation.groupformation;

import delfos.common.Global;
import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import delfos.similaritymeasures.useruser.UserUserSimilarity;
import delfos.similaritymeasures.useruser.UserUserSimilarityWrapper;
import delfos.similaritymeasures.useruser.UserUserSimilarityWrapper_relevanceFactor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.TreeSet;

/**
 * Crea grupos buscando similitudes entre las preferencias de los miembros. Los
 * grupos no tienen usuarios en común, es decir, la intersección entre cualquier
 * par de grupos generados (de una vez) es siempre el conjunto vacío.
 *
 * @version 10-abr-2014
 * @author Jorge Castro Gallardo
 */
public class DissimilarMembers extends GroupFormationTechnique {

    static {

        UserUserSimilarity defaultSimilarity = new UserUserSimilarityWrapper_relevanceFactor(new UserUserSimilarityWrapper(new PearsonCorrelationCoefficient()), 5);

        ParameterOwnerRestriction parameterOwnerRestriction = new ParameterOwnerRestriction(
                UserUserSimilarity.class,
                defaultSimilarity);
        SIMILARITY_MEASURE = new Parameter(
                "SIMILARITY_MEASURE",
                parameterOwnerRestriction);
    }

    /**
     * Parámetro para establecer el número de usuarios que tendrán los grupos
     * generados con esta validación de grupos
     */
    public static final Parameter GROUP_SIZE_PARAMETER = new Parameter("groupSize", new IntegerParameter(1, 10000, 5));
    public static final Parameter N_CANDIDATES_PARAMETER = new Parameter("numCandidates", new IntegerParameter(1, 1000000, 20));
    public static final Parameter SIMILARITY_MEASURE;

    /**
     * Genera una validación de usuarios que genera grupos de tamaño fijo. Por
     * defecto, el tamaño de los grupos es de cuatro miembros.
     */
    public DissimilarMembers() {
        super();
        addParameter(GROUP_SIZE_PARAMETER);
        addParameter(N_CANDIDATES_PARAMETER);
        addParameter(SIMILARITY_MEASURE);

        addParammeterListener(() -> {
            int groupSizeValue = (Integer) getParameterValue(GROUP_SIZE_PARAMETER);

            String oldAlias = getAlias();
            String newAlias = this.getClass().getSimpleName() + "(size=" + groupSizeValue + ")";

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
     * @param numCandidates Número de vecinos candidatos que se consideran al
     * seleccionar el siguiente miembro del grupo aleatoriamente.
     */
    public DissimilarMembers(int groupSizeValue, int numCandidates) {
        this();
        setParameterValue(GROUP_SIZE_PARAMETER, groupSizeValue);
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
        final int maximumGroups = datasetLoader.getRatingsDataset().allUsers().size() / groupSize;

        UserUserSimilarity similarityMeasure = (UserUserSimilarity) getParameterValue(SIMILARITY_MEASURE);

        ArrayList<GroupOfUsers> groupsGenerated = new ArrayList<>();
        ArrayList<Integer> usersRemainToSelect = new ArrayList<>(datasetLoader.getRatingsDataset().allUsers());

        int numGruposGenerados = 0;
        while (usersRemainToSelect.size() >= groupSize) {

            TreeSet<Integer> usersGrupoActual = new TreeSet<>();
            {
                int index = random.nextInt(usersRemainToSelect.size());
                Integer idUser = usersRemainToSelect.remove(index);
                usersGrupoActual.add(idUser);
            }

            while (usersGrupoActual.size() < groupSize) {

                // Calculo la métrica greedy para elegir el siguiente miembro.
                ArrayList<Neighbor> similaritiesToGroup = new ArrayList<>(usersRemainToSelect.size());

                for (int idCandidateMember : usersRemainToSelect) {

                    double similarityToGroup = 1;
                    for (int idMember : usersGrupoActual) {
                        try {
                            double thisMemberSimilarity = similarityMeasure.similarity(datasetLoader, idCandidateMember, idMember);

                            if (thisMemberSimilarity < 0) {
                                similarityToGroup = similarityToGroup * thisMemberSimilarity;
                            } else {
                                similarityToGroup = similarityToGroup * 0;
                            }
                        } catch (CouldNotComputeSimilarity ex) {

                        }
                    }

                    similaritiesToGroup.add(new Neighbor(RecommendationEntity.USER, idCandidateMember, similarityToGroup));
                }

                //Los ordeno por su similitud al grupo
                similaritiesToGroup.sort(Neighbor.BY_SIMILARITY_ASC);

                //Elijo aleatoriamente el siguiente entre los n con mayor similitud.
                int indexSelected = random.nextInt(Math.min(similaritiesToGroup.size(), numMembersCandidate));
                Neighbor neighborSelected = similaritiesToGroup.remove(indexSelected);
                Integer idUser = neighborSelected.getIdNeighbor();
                double similarity = neighborSelected.getSimilarity();

                if (Global.isVerboseAnnoying()) {
                    Global.showInfoMessage("Selected user " + idUser + ", similarity of " + similarity + " --> Group: " + usersGrupoActual + "\n");
                }

                boolean removed = usersRemainToSelect.remove(idUser);
                usersGrupoActual.add(idUser);
            }
            groupsGenerated.add(new GroupOfUsers(usersGrupoActual));
            numGruposGenerados++;
            progressChanged("Group generation", (numGruposGenerados * 100) / (maximumGroups));
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
