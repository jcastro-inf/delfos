package delfos.group.experiment.validation.groupformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Validación de grupos que genera grupos de usuarios, utilizando miembros
 * elegidos aleatoriamente, de un tamaño fijo.
 *
 * @author Jorge Castro Gallardo
 */
public class ProbabilityDistributionOfSizes extends GroupFormationTechnique {

    private float[] acumulateProbabilities = null;

    /**
     * Parámetro para establecer el número de usuarios que tendrán los grupos
     * generados con esta validación de grupos
     */
    public static final Parameter numGroups = new Parameter("numGroups", new IntegerParameter(1, 10000000, 5));

    /**
     * Genera una validación de usuarios que genera grupos de tamaño fijo. Por
     * defecto, el tamaño de los grupos es de cuatro miembros.
     */
    public ProbabilityDistributionOfSizes() {
        super();
        addParameter(numGroups);
    }

    /**
     * Genera una validación de usuarios que genera grupos de tamaño fijo. Por
     * defecto, el tamaño de los grupos es de cuatro miembros.
     *
     * @param numGroupsValue Número de grupos que se generan.
     * @param probabilitiesVector Probabilidad de que un grupo de tamaño i+2 se
     * genere.
     */
    public ProbabilityDistributionOfSizes(int numGroupsValue, double... probabilitiesVector) {
        super();
        addParameter(numGroups);
        setParameterValue(numGroups, numGroupsValue);

        double norma = 0;
        for (double x : probabilitiesVector) {
            norma += x;
        }

        acumulateProbabilities = new float[probabilitiesVector.length];

        acumulateProbabilities[0] = (float) (probabilitiesVector[0] / norma);
        float anterior = acumulateProbabilities[0];
        for (int i = 1; i < probabilitiesVector.length; i++) {
            acumulateProbabilities[i] = (float) (anterior + probabilitiesVector[i] / norma);
            anterior = acumulateProbabilities[i];
        }
    }

    @Override
    public Collection<GroupOfUsers> shuffle(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset {
        if (datasetLoader == null) {
            throw new IllegalStateException("The datasetLoader is null.");
        }
        Random random = new Random(getSeedValue());

        int numGroupsValue = (Integer) getParameterValue(numGroups);

        Set<GroupOfUsers> grupos = new HashSet<>(numGroupsValue);

        ArrayList<Integer> usuarios;
        usuarios = new ArrayList<>(datasetLoader.getRatingsDataset().allUsers());

        int indexGrupoActual = 0;
        while (grupos.size() < numGroupsValue) {

            Set<Integer> usersGrupoActual = new TreeSet<>();

            int groupSize = getGroupSize(random.nextLong());
            while (usersGrupoActual.size() < groupSize) {
                int idUser = usuarios.remove(random.nextInt(usuarios.size()));
                usersGrupoActual.add(idUser);

                if (usuarios.isEmpty()) {
                    usuarios.addAll(datasetLoader.getRatingsDataset().allUsers());
                }
            }
            boolean add = grupos.add(new GroupOfUsers(usersGrupoActual));

            if (add && grupos.size() % 10000 == 0) {

                indexGrupoActual++;
            }

            if (usuarios.isEmpty()) {
                usuarios.addAll(datasetLoader.getRatingsDataset().allUsers());
            }
        }
        GroupOfUsers[] groupOfUsers = new GroupOfUsers[grupos.size()];

        indexGrupoActual = 0;
        for (GroupOfUsers grupoActual : grupos) {
            groupOfUsers[indexGrupoActual] = grupoActual;
            indexGrupoActual++;
        }

        progressChanged("Group generation", 100);

        return Arrays.asList(groupOfUsers);
    }

    private int getGroupSize(long seed) {
        float r = new Random(seed).nextFloat();
        int size = -1;

        for (int i = 0; i < acumulateProbabilities.length; i++) {
            if (acumulateProbabilities[i] > r) {
                size = i + 2;
                break;
            }
        }
        return size;
    }
}
