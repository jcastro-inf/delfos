package delfos.group.experiment.validation.groupformation;

import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import delfos.similaritymeasures.useruser.UserUserSimilarity;
import delfos.similaritymeasures.useruser.UserUserSimilarityWrapper;
import delfos.similaritymeasures.useruser.UserUserSimilarityWrapper_relevanceFactor;
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
public class SimilarMembers extends GroupFormationTechnique {

    static {

        UserUserSimilarity defaultSimilarity = new UserUserSimilarityWrapper_relevanceFactor(
                new UserUserSimilarityWrapper(
                        new PearsonCorrelationCoefficient()
                ),
                20
        );

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
    public static final Parameter SIMILARITY_MEASURE;

    /**
     * Genera una validación de usuarios que genera grupos de tamaño fijo. Por
     * defecto, el tamaño de los grupos es de cuatro miembros.
     */
    public SimilarMembers() {
        super();
        addParameter(GROUP_SIZE_PARAMETER);
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
     */
    public SimilarMembers(int groupSizeValue) {
        this();
        setParameterValue(GROUP_SIZE_PARAMETER, groupSizeValue);
    }

    @Override
    public Collection<GroupOfUsers> shuffle(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset {

        if (datasetLoader == null) {
            throw new IllegalStateException("The datasetLoader is null.");
        }
        Random random = new Random(getSeedValue());

        final int groupSize = (Integer) getParameterValue(GROUP_SIZE_PARAMETER);
        final int maximumGroups = datasetLoader.getRatingsDataset().allUsers().size() / groupSize;
        UserUserSimilarity similarityMeasure = (UserUserSimilarity) getParameterValue(SIMILARITY_MEASURE);

        SimilarMembers_OnlyNGroups similarMembers_OnlyNGroups = new SimilarMembers_OnlyNGroups(maximumGroups, groupSize);
        similarMembers_OnlyNGroups.setParameterValue(SimilarMembers_OnlyNGroups.SIMILARITY_MEASURE, similarityMeasure);

        similarMembers_OnlyNGroups.addListener(this::progressChanged);

        Collection<GroupOfUsers> result = similarMembers_OnlyNGroups.shuffle(datasetLoader);

        return result;
    }
}
