/*
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package delfos.experiment.validation.validationtechnique;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.dataset.storage.validationdatasets.ValidationDatasets;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Clase que implementa el método de partición de datasets Cross Fold Validation tomando como entrada usuario-producto.
 * De esta manera, cada dato individual es una valoración por lo que al hacer las particiones se tienen en cuenta las
 * valoraciones (los usuarios o los items a los que pertenecen dichas valoraciones no se tienen en cuenta)
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 (19 Octubre 2011)
 */
public class CrossFoldValidation_Ratings extends ValidationTechnique {

    private static final long serialVersionUID = 1L;
    /**
     * Parámetro para almacenar el número de particiones que se realizan sobre el dataset original.
     */
    public static final Parameter NUM_PARTITIONS = new Parameter("NUM_PARTITIONS", new IntegerParameter(2, Integer.MAX_VALUE, 5), "Número de particiones que se realizan sobre el dataset original.");

    /**
     * Constructor de la clase que genera los conjuntos de validación cruzada. Por defecto tiene cinco particiones y la
     * semilla utilizada será la fecha actual {@link System#currentTimeMillis()}
     */
    public CrossFoldValidation_Ratings() {
        super();

        addParameter(NUM_PARTITIONS);
    }

    @Override
    public <RatingType extends Rating> PairOfTrainTestRatingsDataset<RatingType>[] shuffle(DatasetLoader<RatingType> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        long generalSeed = getSeedValue();

        int numSplit = getNumberOfPartitions();

        PairOfTrainTestRatingsDataset[] ret = new PairOfTrainTestRatingsDataset[numSplit];

        //cross validationDatasets initialization
        Map<Integer, Map<User, Set<Item>>> todosConjuntosTest = IntStream.range(0, numSplit).boxed()
                .collect(Collectors.toMap(Function.identity(), i -> new HashMap<>()));

        Map<Integer, List<RatingType>> ratingsPartition = getUsersInTestSet(datasetLoader)
                .map(user -> {
                    final long seedThisUser = generalSeed + user.getId();
                    Random randomThisUser = new Random(seedThisUser);

                    List<RatingType> userRatings = datasetLoader.getRatingsDataset()
                            .getUserRatingsRated(user.getId())
                            .values().stream()
                            .sorted((Comparator<? super RatingType>) Rating.SORT_BY_ID_ITEM)
                            .collect(Collectors.toList());

                    List<RatingType> randomizedRatings = userRatings.stream().collect(Collectors.toList());
                    Collections.shuffle(randomizedRatings, randomThisUser);

                    Map<Integer, List<Integer>> ratingsByPartitions = IntStream.range(0, randomizedRatings.size())
                            .boxed()
                            .collect(Collectors
                            .groupingBy( index -> index % numSplit));

                    Map<Integer, List<RatingType>> thisUserPartition = ratingsByPartitions.entrySet().parallelStream()
                            .collect(Collectors.toMap(
                                    entry -> entry.getKey(),
                                    entry -> entry.getValue().parallelStream()
                                            .map(index -> randomizedRatings.get(index))
                                            .collect(Collectors.toList())));
                    return thisUserPartition;

                })
                .flatMap(entry -> entry.entrySet().stream())
                .collect(Collectors.groupingBy(entry -> entry.getKey()))
                .entrySet().parallelStream()
                .flatMap(entry -> {

                    List<RatingType> ratingsThisPartition = entry.getValue().parallelStream()
                            .flatMap(entryValue ->entryValue.getValue().parallelStream())
                            .collect(Collectors.toList());

                    Map<Integer, List<RatingType>> map = new HashMap<>();
                    map.put(entry.getKey(), ratingsThisPartition);

                    return map.entrySet().parallelStream();
                })
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

        ratingsPartition.entrySet().forEach(partitionData -> {

            int idPartition = partitionData.getKey();

            Map<User, List<RatingType>> collect = partitionData.getValue().parallelStream()
                    .collect(Collectors.groupingBy(rating -> rating.getUser()));

            Map<User, Set<Item>> trainingSet = collect.entrySet().parallelStream().collect(Collectors.toMap(
                    userTestSetEntry -> {
                        User user = userTestSetEntry.getKey();
                        return user;
                    },
                    userTestSetEntry -> {
                        List<RatingType> ratingsInTestSet = userTestSetEntry.getValue();
                        Set<Item> idItem_testSet = ratingsInTestSet.stream()
                        .map(rating -> rating.getItem())
                        .collect(Collectors.toSet());

                        return idItem_testSet;
                    }));

            todosConjuntosTest.put(idPartition, trainingSet);

        });

        for (int idPartition = 0; idPartition < getNumberOfPartitions(); idPartition++) {
            try {
                ret[idPartition] = new PairOfTrainTestRatingsDataset(
                        datasetLoader,
                        ValidationDatasets.getInstance().createTrainingDataset(datasetLoader.getRatingsDataset(), todosConjuntosTest.get(idPartition)),
                        ValidationDatasets.getInstance().createTestDataset(datasetLoader.getRatingsDataset(), todosConjuntosTest.get(idPartition)),
                        "_" + this.getClass().getSimpleName() + "_seed=" + getSeedValue() + "_partition=" + idPartition);
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            } catch (ItemNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            }
            progressChanged("Copying partitions", (int) idPartition * 100 / getNumberOfPartitions());
        }
        return ret;
    }

    public <RatingType extends Rating> Stream<User> getUsersInTestSet(DatasetLoader<RatingType> datasetLoader) throws CannotLoadUsersDataset {
        return datasetLoader.getUsersDataset().parallelStream();
    }

    public int getNumberOfPartitions() {
        return (Integer) getParameterValue(CrossFoldValidation_Ratings.NUM_PARTITIONS);
    }

    @Override
    public int getNumberOfSplits() {
        return getNumberOfPartitions();
    }

    public CrossFoldValidation_Ratings setNumberOfPartitions(int numPartitions) {
        setParameterValue(CrossFoldValidation_Ratings.NUM_PARTITIONS, numPartitions);
        return this;
    }
}
