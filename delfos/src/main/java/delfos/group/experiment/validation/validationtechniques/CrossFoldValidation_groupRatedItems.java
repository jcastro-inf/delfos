package delfos.group.experiment.validation.validationtechniques;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.dataset.storage.validationdatasets.ValidationDatasets;
import delfos.dataset.util.DatasetPrinterDeprecated;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Validación cruzada para sistemas de recomendación a grupos. Esta validación
 * elimina cierto porcentaje de items valorados por el grupo. No tiene en cuenta
 * si los productos han sido valorados por un solo miembro o por varios, por lo
 * que el número de ratings que caen en el conjunto de test no coincide con el
 * porcentaje de test.
 *
 * Esta validación solo se puede aplicar si los grupos formados no repiten un
 * mismo usuario, es decir, un usuario sólo se encuentra en un grupo. En caso de
 * que se de esta situación, el método {@link CrossFoldValidation_Items#shuffle(java.util.Collection)
 * } informa de esta situación y termina sin realizar la validación.
 *
 * @see HoldOutGroupRatedItems
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 19-Mayo-2013
 */
public class CrossFoldValidation_Items extends GroupValidationTechnique {

    /**
     * Número de particiones.
     */
    public static final Parameter NUMBER_OF_PARTITIONS = new Parameter(
            "NUMBER_OF_PARTITIONS",
            new IntegerParameter(1, Integer.MAX_VALUE, 5),
            "Número de particiones.");

    public CrossFoldValidation_Items() {
        super();
        addParameter(NUMBER_OF_PARTITIONS);
    }

    public CrossFoldValidation_Items(long seed) {
        this();
        setSeedValue(seed);
    }

    /**
     * {@inheritDoc }
     *
     * @return Número de particiones que realiza esta validación.
     */
    @Override
    public final int getNumberOfSplits() {
        return (Integer) getParameterValue(NUMBER_OF_PARTITIONS);
    }

    /**
     * {@inheritDoc }
     *
     * @param datasetLoader Dataset de entrada.
     * @return Pares de conjuntos de training y test. Este vector tendrá {@link GroupValidationTechnique#getNumberOfSplits()
     * }.
     * @throws IllegalArgumentException Cuando se especifica un conjunto de
     * grupos que compartan usuarios, es decir, un mismo usuario está en
     * distintos grupos.
     */
    @Override
    public PairOfTrainTestRatingsDataset[] shuffle(DatasetLoader<? extends Rating> datasetLoader, Iterable<GroupOfUsers> groupsOfUsers) throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        if (datasetLoader == null) {
            throw new IllegalArgumentException("DatasetLoader<? extends Rating> is null.");
        }

        checkGroupsSharingUsers(groupsOfUsers);

        Random random = new Random(getSeedValue());

        PairOfTrainTestRatingsDataset[] ret = new PairOfTrainTestRatingsDataset[getNumberOfSplits()];

        Set<Integer> allItems;
        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            allItems = new TreeSet<>(contentDatasetLoader.getContentDataset().allID());
        } else {
            allItems = new TreeSet<>(datasetLoader.getRatingsDataset().allRatedItems());
        }

        List<Map<Integer, Set<Integer>>> finalTestSets = new ArrayList<>(getNumberOfSplits());

        List<Set<Integer>> itemsTestSets = new ArrayList<>(getNumberOfSplits());

        for (int i = 0; i < getNumberOfSplits(); i++) {
            finalTestSets.add(new TreeMap<>());
            itemsTestSets.add(new TreeSet<>());
        }

        {
            //Hago la partición de los productos general, sin tener en cuenta valoraciones.
            Set<Integer> allItems_sub = new TreeSet<>(allItems);
            int partition = 0;
            while (!allItems_sub.isEmpty()) {

                int idItem = allItems_sub.toArray(new Integer[0])[random.nextInt(allItems_sub.size())];

                allItems_sub.remove(idItem);

                itemsTestSets.get(partition % getNumberOfSplits()).add(idItem);
                partition++;

            }
        }

        {
            //Construyo los conjuntos de test para cada usuario.
            for (int idPartition = 0; idPartition < getNumberOfSplits(); idPartition++) {
                Set<Integer> testItems = itemsTestSets.get(idPartition);
                if (Global.isVerboseAnnoying()) {
                    Global.showInfoMessage(testItems + "\n");
                }

                for (GroupOfUsers group : groupsOfUsers) {
                    for (int idUser : group) {
                        Set<Integer> itemsTest_user;
                        try {
                            itemsTest_user = new TreeSet<>(datasetLoader.getRatingsDataset().getUserRated(idUser));
                            itemsTest_user.retainAll(testItems);
                            finalTestSets.get(idPartition).put(idUser, itemsTest_user);
                        } catch (UserNotFound ex) {
                            ERROR_CODES.USER_NOT_FOUND.exit(ex);
                        }
                    }
                }
            }
        }
        for (int idPartition = 0; idPartition < getNumberOfSplits(); idPartition++) {
            try {

                ret[idPartition] = new PairOfTrainTestRatingsDataset(
                        datasetLoader,
                        ValidationDatasets.getInstance().createTrainingDataset(datasetLoader.getRatingsDataset(),
                                finalTestSets.get(idPartition)),
                        ValidationDatasets.getInstance().createTestDataset(datasetLoader.getRatingsDataset(), finalTestSets.get(idPartition)));

                if (Global.isVerboseAnnoying()) {

                    Global.showInfoMessage("==================================================== \n");

                    Set<Integer> allUsers = new TreeSet<>();
                    for (GroupOfUsers g : groupsOfUsers) {
                        allUsers.addAll(g.getIdMembers());
                    }

                    Global.showInfoMessage("Dataset de training " + idPartition + ".\n");
                    DatasetPrinterDeprecated.printCompactRatingTable(
                            ret[idPartition].train,
                            allUsers,
                            allItems);

                    Global.showInfoMessage("Dataset de test " + idPartition + ".\n");
                    DatasetPrinterDeprecated.printCompactRatingTable(
                            ret[idPartition].test,
                            allUsers,
                            allItems);
                    Global.showInfoMessage("==================================================== \n");
                }
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            } catch (ItemNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            }

        }
        return ret;
    }
}
