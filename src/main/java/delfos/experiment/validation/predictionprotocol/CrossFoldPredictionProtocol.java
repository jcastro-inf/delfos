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
package delfos.experiment.validation.predictionprotocol;

import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Esta técnica aplica la validación cruzada para la predicción de valoraciones.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class CrossFoldPredictionProtocol extends PredictionProtocol {

    private static final long serialVersionUID = 1L;
    public static final Parameter numFolds = new Parameter("c", new IntegerParameter(2, Integer.MAX_VALUE, 5));

    public CrossFoldPredictionProtocol() {
        super();

        addParameter(numFolds);
    }

    public CrossFoldPredictionProtocol(int c) {
        this();
        setParameterValue(numFolds, c);
    }

    @Override
    public <RatingType extends Rating> List<Set<Item>> getRecommendationRequests(
            DatasetLoader<RatingType> trainingDatasetLoader,
            DatasetLoader<RatingType> testDatasetLoader,
            User user) throws UserNotFound {

        Random random = new Random(getSeedValue());
        ArrayList<Set<Item>> ret = new ArrayList<>();
        Set<Item> items = new TreeSet<>(
                testDatasetLoader.getRatingsDataset().getUserRatingsRated(user.getId())
        .values().stream().map(rating -> rating.getItem()).collect(Collectors.toSet())
        );
        for (int i = 0; i < getNumPartitions(); i++) {
            ret.add(new TreeSet<>());
        }
        int n = 0;
        while (!items.isEmpty()) {
            Item item = items.toArray(new Item[0])[random.nextInt(items.size())];
            items.remove(item.getId());
            int partition = n % getNumPartitions();
            ret.get(partition).add(item);
            n++;
        }
        return ret;
    }

    protected int getNumPartitions() {
        return (int) getParameterValue(numFolds);
    }
}
