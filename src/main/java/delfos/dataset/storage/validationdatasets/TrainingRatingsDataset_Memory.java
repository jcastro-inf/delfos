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
package delfos.dataset.storage.validationdatasets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import delfos.common.Global;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RatingsDatasetAdapter;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.dataset.basic.rating.domain.Domain;

/**
 * Dataset para la validación de
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknow date
 * @version 1.1 (21-01-2013) Ahora implementa de {@link RatingsDatasetAdapter}
 * @version 1.2 06-Mar-2013 Modificación de los parámetros del constructor y
 * corrección de errores.
 * @param <RatingType>
 */
public class TrainingRatingsDataset_Memory<RatingType extends Rating> extends RatingsDatasetAdapter<RatingType> implements TrainingRatingsDataset<RatingType> {

    private final BothIndexRatingsDataset<RatingType> trainingRatingsDataset;
    private final RatingsDataset<RatingType> originalRatingsDataset;

    /**
     * Crea un buffer para no tener que recalcular los conjuntos indizados por
     * item. Acelera la ejecución del metodo item item
     */
    private final Map<Long, Map<Long, RatingType>> bufferItems = Collections.synchronizedMap(new TreeMap<Long, Map<Long, RatingType>>());

    public TrainingRatingsDataset_Memory(RatingsDataset<RatingType> originalRatingsDataset, Map<Long, Set<Long>> testSet) throws UserNotFound, ItemNotFound {
        super();

        checkParameters(originalRatingsDataset, testSet);

        this.originalRatingsDataset = originalRatingsDataset;

        List<RatingType> trainingRatings = new ArrayList<>();
        for (RatingType rating : originalRatingsDataset) {
            final long idUser = rating.getIdUser();
            final long idItem = rating.getIdItem();
            if (testSet.containsKey(idUser)) {
                //El usuario está en test
                if (testSet.get(idUser).contains(idItem)) {
                    //Este rating está en el testSet, no se añade.
                } else {
                    //Este rating no está en el testSet, se añade.
                    trainingRatings.add(rating);
                }
            } else {
                //El usuario no está en test, se añade sin problema.
                trainingRatings.add(rating);
            }
        }

        trainingRatingsDataset = new BothIndexRatingsDataset<>(trainingRatings);
    }

    public final void checkParameters(RatingsDataset<RatingType> originalDatset, Map<Long, Set<Long>> testSet) throws ItemNotFound, UserNotFound, IllegalArgumentException {
        for (long idUser : testSet.keySet()) {
            for (long idItem : testSet.get(idUser)) {
                if (originalDatset.getRating(idUser, idItem) == null) {
                    Collection<Long> userRated = originalDatset.getUserRated(idUser);
                    if (userRated.isEmpty()) {
                        Global.showWarning("User " + idUser + "hasn't rated any items.");
                    }
                    Collection<Long> itemRated = originalDatset.getItemRated(idItem);
                    if (itemRated.isEmpty()) {
                        Global.showWarning("Item " + idItem + "hasn't received any rating.");
                    }
                    throw new IllegalArgumentException("Specified rating (idUser=" + idUser + ",idItem=" + idItem + ") isn't found in trainingRatingsDataset");
                }
            }
        }
    }

    @Override
    public RatingType getRating(long idUser, long idItem) throws UserNotFound, ItemNotFound {
        return trainingRatingsDataset.getRating(idUser, idItem);
    }

    @Override
    public Set<Long> allUsers() {
        return trainingRatingsDataset.allUsers();
    }

    @Override
    public Set<Long> allRatedItems() {
        return trainingRatingsDataset.allRatedItems();
    }

    @Override
    public Set<Long> getUserRated(long idUser) throws UserNotFound {
        return trainingRatingsDataset.getUserRated(idUser);
    }

    @Override
    public Map<Long, RatingType> getUserRatingsRated(long idUser) throws UserNotFound {
        return trainingRatingsDataset.getUserRatingsRated(idUser);
    }

    @Override
    public Set<Long> getItemRated(long idItem) throws ItemNotFound {
        return trainingRatingsDataset.getItemRated(idItem);
    }

    @Override
    public Map<Long, RatingType> getItemRatingsRated(long idItem) throws ItemNotFound {
        return trainingRatingsDataset.getItemRatingsRated(idItem);
    }

    @Override
    public Domain getRatingsDomain() {
        return trainingRatingsDataset.getRatingsDomain();
    }

    @Override
    public RatingsDataset<RatingType> getOriginalDataset() {
        return originalRatingsDataset;
    }
}
