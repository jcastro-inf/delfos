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

import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.Set;

/**
 * Encapsula los resultados del protocolo de predicción para grupos.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class GroupRecommendationRequest {

    public final DatasetLoader<? extends Rating> predictionPhaseDatasetLoader;
    public final GroupOfUsers groupOfUsers;
    public final Set<Item> itemsToPredict;

    public GroupRecommendationRequest(
            GroupOfUsers groupOfUsers,
            DatasetLoader<? extends Rating> predictionPhaseDatasetLoader,
            Set<Item> itemsToPredict) {

        this.groupOfUsers = groupOfUsers;
        this.predictionPhaseDatasetLoader = predictionPhaseDatasetLoader;
        this.itemsToPredict = itemsToPredict;

    }

}
