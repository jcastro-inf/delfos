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
package delfos.group.experiment.validation.predictionvalidation;

import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.util.DatasetUtilities;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementa el protocolo de predicción HoldOut, que divide el conjunto de
 * productos valorados por todos los usuarios en dos conjuntos disjuntos:
 * entrenamiento y evaluación. Lo hace de manera que un cierto porcentaje de
 * productos estén en el conjunto de entrenamiento, y el resto en el de
 * evaluación
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 (10-12-2012)
 */
public class NoPredictionProtocol extends GroupPredictionProtocol {

    public NoPredictionProtocol() {
        super();
    }

    @Deprecated
    public NoPredictionProtocol(long seed) {
        this();
        setParameterValue(SEED, seed);
    }

    private Set<Integer> getRatedItems(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers group) throws CannotLoadRatingsDataset, UserNotFound {
        return DatasetUtilities.getMembersRatings_byItem(group, datasetLoader).keySet();
    }

    @Override
    public Collection<GroupRecommendationRequest> getGroupRecommendationRequests(
            DatasetLoader<? extends Rating> trainDatasetLoader, DatasetLoader<? extends Rating> testDatasetLoader, GroupOfUsers group) throws CannotLoadRatingsDataset, UserNotFound {
        return Arrays.asList(new GroupRecommendationRequest(
                group,
                trainDatasetLoader,
                getRatedItems(testDatasetLoader, group).stream().map(idItem -> trainDatasetLoader.getContentDataset().get(idItem)).collect(Collectors.toSet())
        ));

    }

}
