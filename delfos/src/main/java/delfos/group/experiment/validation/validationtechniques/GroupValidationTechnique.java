package delfos.group.experiment.validation.validationtechniques;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.experiment.SeedHolder;
import delfos.experiment.validation.validationtechnique.ValidationTechniqueProgressListener;

/**
 * Clase abstracta que define los métodos comunes de las distintas técnicas de
 * validación para sistemas de recomendación a grupos.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknow date
 * @version 1.1 Implementación de la interfaz {@link SeedHolder}.
 */
public abstract class GroupValidationTechnique extends ParameterOwnerAdapter implements SeedHolder {

    private final LinkedList<ValidationTechniqueProgressListener> listeners = new LinkedList<>();

    public GroupValidationTechnique() {
        super();
        addParameter(SEED);
    }

    public void addListener(ValidationTechniqueProgressListener listener) {
        listeners.add(listener);
        listener.progressChanged("", 0);
    }

    public void removeListener(ValidationTechniqueProgressListener listener) {
        listeners.remove(listener);
    }

    protected void progressChanged(String message, int percent) {
        listeners.stream().forEach((listener) -> {
            listener.progressChanged(message, percent);
        });
    }

    /**
     * Esta función se debe implementar la generación de los conjuntos de
     * validación y será invocada cada vez que se realice una ejecución, para
     * probar conjuntos de validación diferentes
     *
     * @param datasetLoader
     * @param groupsOfUsers Grupos de usuarios que se evaluarán posteriormente.
     * @return
     */
    public abstract PairOfTrainTestRatingsDataset[] shuffle(
            DatasetLoader<? extends Rating> datasetLoader,
            Iterable<GroupOfUsers> groupsOfUsers)
            throws CannotLoadRatingsDataset, CannotLoadContentDataset;

    /**
     * Devuelve el número de particiones que se realizan por ejecución aplicando
     * esta validación.
     *
     * @return
     */
    public abstract int getNumberOfSplits();

    @Override
    public void setSeedValue(long seedValue) {
        setParameterValue(SEED, seedValue);
    }

    @Override
    public long getSeedValue() {
        return (Long) getParameterValue(SEED);
    }

    public PairOfTrainTestRatingsDataset[] shuffle(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers... groups) throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        return shuffle(datasetLoader, Arrays.asList(groups));
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.GROUP_VALIDATION_TECHNIQUE;
    }

    public static void checkGroupsSharingUsers(Iterable<GroupOfUsers> groupsOfUsers) throws IllegalArgumentException {
        //Compruebo que cada usuario está únicamente en un grupo.
        Set<Integer> users = new TreeSet<>();
        int numUsersInGroups = 0;

        for (GroupOfUsers g : groupsOfUsers) {
            users.addAll(g.getIdMembers());
            numUsersInGroups += g.size();
        }

        if (users.size() != numUsersInGroups) {
            throw new IllegalArgumentException("Groups are sharing users, can't perform this validation.");
        }
    }
}
