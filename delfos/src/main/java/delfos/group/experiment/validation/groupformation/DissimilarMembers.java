package delfos.group.experiment.validation.groupformation;

import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.parameters.Parameter;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.similaritymeasures.useruser.UserUserSimilarity;
import java.util.Collection;
import java.util.Random;

/**
 * Crea grupos buscando similitudes entre las preferencias de los miembros. Los
 * grupos no tienen usuarios en común, es decir, la intersección entre cualquier
 * par de grupos generados (de una vez) es siempre el conjunto vacío.
 *
 * @version 10-abr-2014
 * @author Jorge Castro Gallardo
 */
public class DissimilarMembers extends GroupFormationTechnique {

    public static final Parameter GROUP_SIZE_PARAMETER = DissimilarMembers_OnlyNGroups.GROUP_SIZE_PARAMETER;
    public static final Parameter N_CANDIDATES_PARAMETER = DissimilarMembers_OnlyNGroups.N_CANDIDATES_PARAMETER;
    public static final Parameter SIMILARITY_MEASURE = DissimilarMembers_OnlyNGroups.SIMILARITY_MEASURE;

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

        DissimilarMembers_OnlyNGroups dissimilarMembers_OnlyNGroups = new DissimilarMembers_OnlyNGroups(maximumGroups, groupSize, numMembersCandidate);
        dissimilarMembers_OnlyNGroups.setParameterValue(DissimilarMembers_OnlyNGroups.SIMILARITY_MEASURE, similarityMeasure);

        dissimilarMembers_OnlyNGroups.addListener(this::progressChanged);

        Collection<GroupOfUsers> result = dissimilarMembers_OnlyNGroups.shuffle(datasetLoader);

        return result;
    }
}
