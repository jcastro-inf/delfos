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
import java.util.List;
import java.util.Map;
import java.util.Set;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RatingsDatasetAdapter;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.dataset.basic.rating.domain.Domain;

/**
 * Dataset que deja visibles las valoraciones especificadas en el conjunto de
 * datos indicado.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknow date
 * @version 1.2 06-Mar-2013 Modificación de los parámetros del constructor y
 * corrección de errores.
 * @version 1.1 (21-01-2013) Ahora implementa de {@link RatingsDatasetAdapter}
 * @param <RatingType>
 */
public class TestRatingsDataset_Memory<RatingType extends Rating> extends RatingsDatasetAdapter<RatingType> implements TestRatingsDataset<RatingType> {

    private final BothIndexRatingsDataset<RatingType> testRatingsDataset;
    /**
     * Dataset original que contiene el conjunto de datos completo.
     */
    private final RatingsDataset<RatingType> originalRatingsDataset;

    public TestRatingsDataset_Memory(RatingsDataset<RatingType> originalRatingsDataset, Map<Long, Set<Long>> testSet) throws UserNotFound, ItemNotFound {
        super();

        checkParameters(testSet, originalRatingsDataset);
        this.originalRatingsDataset = originalRatingsDataset;

        List<RatingType> testRatings = new ArrayList<>();
        for (RatingType rating : originalRatingsDataset) {
            final long idUser = rating.getIdUser();
            final long idItem = rating.getIdItem();
            if (testSet.containsKey(idUser) && testSet.get(idUser).contains(idItem)) {
                //Este rating no está en el testSet, se añade.
                testRatings.add(rating);
            } else {
                //Este rating está en el testSet, no se añade.

            }
        }

        testRatingsDataset = new BothIndexRatingsDataset<>(testRatings);
    }

    public final void checkParameters(Map<Long, Set<Long>> testSet, RatingsDataset<RatingType> originalDatset) throws UserNotFound, IllegalArgumentException, ItemNotFound {
        for (long idUser : testSet.keySet()) {
            for (long idItem : testSet.get(idUser)) {
                if (originalDatset.getRating(idUser, idItem) == null) {
                    throw new IllegalArgumentException("Specified rating isn't found in originalDataset");
                }
            }
        }
    }

    @Override
    public RatingType getRating(long idUser, long idItem) throws UserNotFound, ItemNotFound {
        return testRatingsDataset.getRating(idUser, idItem);
    }

    @Override
    public Set<Long> allUsers() {
        return testRatingsDataset.allUsers();
    }

    @Override
    public Set<Long> allRatedItems() {
        return testRatingsDataset.allRatedItems();
    }

    @Override
    public Set<Long> getUserRated(long idUser) throws UserNotFound {
        return testRatingsDataset.getUserRated(idUser);
    }

    @Override
    public Map<Long, RatingType> getUserRatingsRated(long idUser) throws UserNotFound {
        return testRatingsDataset.getUserRatingsRated(idUser);
    }

    @Override
    public Set<Long> getItemRated(long idItem) throws ItemNotFound {
        return testRatingsDataset.getItemRated(idItem);
    }

    @Override
    public Map<Long, RatingType> getItemRatingsRated(long idItem) throws ItemNotFound {
        return testRatingsDataset.getItemRatingsRated(idItem);
    }

    @Override
    public Domain getRatingsDomain() {
        return testRatingsDataset.getRatingsDomain();
    }

    @Override
    public RatingsDataset<RatingType> getOriginalDataset() {
        return originalRatingsDataset;
    }
}
