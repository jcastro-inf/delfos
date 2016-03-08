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
package delfos.dataset.generated.random;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.Constants;
import delfos.common.Global;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.ParameterListener;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingWithTimestamp;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RatingsDatasetAdapter;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.experiment.SeedHolder;
import delfos.dataset.basic.rating.domain.Domain;
import delfos.dataset.basic.rating.domain.IntegerDomain;

/**
 * Dataset generado aleatoriamente. Sirve para realizar pruebas previas a
 * utilizar un dataset real mientras que no se dispone de los datos reales.
 * Genera un dataset aleatorio en el que se puede configurar el número de
 * usuarios, número de productos, porcentaje de valoraciones definidas y rango
 * en que se da la valoración.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 (21-01-2013)
 * @version 1.0.1 15-Noviembre-2013
 */
public class RandomRatingsDataset extends ParameterOwnerAdapter implements RatingsDataset<RatingWithTimestamp>, SeedHolder {

    private static final long serialVersionUID = 1L;

    private static final IntegerDomain timestampDomain;

    static {
        Calendar oldestTime = GregorianCalendar.getInstance();
        oldestTime.set(2012, 01, 01);
        long minTimestamp = oldestTime.getTimeInMillis();

        Calendar newestTime = GregorianCalendar.getInstance();
        newestTime.set(2015, 12, 31);
        long maxTimestamp = newestTime.getTimeInMillis();

        timestampDomain = new IntegerDomain(minTimestamp, maxTimestamp);
    }

    /**
     * Dataset en que se guardan las valoraciones.
     */
    private BothIndexRatingsDataset<RatingWithTimestamp> dataset;

    /**
     * Genera un dataset aleatorio con 1000 usuarios, 1000 productos, un 20% de
     * valoraciones definidas en el rango [1,5].
     */
    public RandomRatingsDataset() {
        this(RandomRatingsDatasetFactory.DEFAULT_USER_SET,
                RandomRatingsDatasetFactory.DEFAULT_ITEM_SET,
                RandomRatingsDatasetFactory.DEFAULT_LOAD_FACTOR,
                Rating.DEFAULT_DECIMAL_DOMAIN,
                Constants.getCurrentTimeMillis()
        );
    }

    public RandomRatingsDataset(Set<Integer> users, Set<Integer> items, double loadFactor, Domain ratingsDomain, long seed) {
        super();

        init();
        Random random = new Random(seed);
        validateParametersLoadFactor(users, items, loadFactor);

        long generateRatingWithTimestamps = (long) (users.size() * items.size() * loadFactor);

        Map<Integer, Map<Integer, RatingWithTimestamp>> ratings_byUser = new TreeMap<>();

        int numGeneratedRatingWithTimestamps = 0;
        int valorAnterior = -1;
        while (numGeneratedRatingWithTimestamps < generateRatingWithTimestamps) {
            int idUser = (Integer) users.toArray()[random.nextInt(users.size())];
            Set<Integer> userNotRated = new TreeSet<>(items);
            if (ratings_byUser.containsKey(idUser)) {
                userNotRated.removeAll(ratings_byUser.get(idUser).keySet());
            } else {
                ratings_byUser.put(idUser, new TreeMap<>());
            }

            if (userNotRated.isEmpty()) {
                users.remove(idUser);
            } else {
                int idItem = (Integer) userNotRated.toArray()[random.nextInt(userNotRated.size())];
                Number rating = ratingsDomain.getValueAssociatedToProbability(random.nextDouble());
                long timestamp = timestampDomain.getValueAssociatedToProbability(random.nextDouble());

                ratings_byUser.get(idUser).put(idItem, new RatingWithTimestamp(idUser, idItem, rating, timestamp));

                numGeneratedRatingWithTimestamps++;
                int percent = (int) ((numGeneratedRatingWithTimestamps / (double) generateRatingWithTimestamps) * 100);
                if (percent != valorAnterior) {
                    Global.showInfoMessage(percent + "% generation of ratings dataset.\n");
                    valorAnterior = percent;
                }
            }
        }
        dataset = new BothIndexRatingsDataset<>(ratings_byUser);
    }

    public RandomRatingsDataset(Set<Integer> users, Set<Integer> items, int numRatingsPerUser, Domain ratingDomain, long seed) {
        super();

        init();
        Random random = new Random(seed);

        validateParametersNumRatings(users, items, numRatingsPerUser);

        Map<Integer, Map<Integer, RatingWithTimestamp>> ratings_byUser = new TreeMap<>();

        for (Integer idUser : users) {
            Set<Integer> userNotRated = new TreeSet<>(items);
            ratings_byUser.put(idUser, new TreeMap<>());

            for (int n = 0; n < numRatingsPerUser; n++) {
                int idItem = (Integer) userNotRated.toArray()[random.nextInt(userNotRated.size())];

                Number rating = ratingDomain.getValueAssociatedToProbability(random.nextDouble());
                long timestamp = timestampDomain.getValueAssociatedToProbability(random.nextDouble()).longValue();

                ratings_byUser.get(idUser).put(idItem, new RatingWithTimestamp(idUser, idItem, rating, timestamp));
            }
        }
        dataset = new BothIndexRatingsDataset<>(ratings_byUser);
    }

    private void validateParametersNumRatings(Set<Integer> users, Set<Integer> items, int numRatingsPerUser) throws IllegalArgumentException {
        if (users.isEmpty()) {
            throw new IllegalArgumentException("No users specified");
        }

        if (items.isEmpty()) {
            throw new IllegalArgumentException("No items specified");
        }

        if (numRatingsPerUser < 1) {
            throw new IllegalArgumentException("numRatingsPerUser >= 1  not satisfied!");
        }

        if (numRatingsPerUser > items.size()) {
            throw new IllegalArgumentException("numRatingsPerUser <= items.size()  not satisfied!");
        }
    }

    private void validateParametersLoadFactor(Set<Integer> users, Set<Integer> items, double loadFactor) throws IllegalArgumentException {
        if (users.isEmpty()) {
            throw new IllegalArgumentException("No users specified");
        }

        if (items.isEmpty()) {
            throw new IllegalArgumentException("No items specified");
        }

        if (loadFactor < 0) {
            throw new IllegalArgumentException("numRatingsPerUser >= 1  not satisfied!");
        }

        if (loadFactor > 1) {
            throw new IllegalArgumentException("numRatingsPerUser <= items.size()  not satisfied!");
        }
    }

    @Override
    public RatingWithTimestamp getRating(int idUser, int idItem) throws UserNotFound, ItemNotFound {
        return dataset.getRating(idUser, idItem);
    }

    @Override
    public Set<Integer> allUsers() {
        return dataset.allUsers();
    }

    @Override
    public Set<Integer> allRatedItems() {
        return dataset.allRatedItems();
    }

    @Override
    public Set<Integer> getUserRated(Integer idUser) throws UserNotFound {
        return dataset.getUserRated(idUser);
    }

    @Override
    public Set<Integer> getItemRated(Integer idItem) throws ItemNotFound {
        return dataset.getItemRated(idItem);
    }

    @Override
    public Map<Integer, RatingWithTimestamp> getUserRatingsRated(Integer idUser) throws UserNotFound {
        return dataset.getUserRatingsRated(idUser);
    }

    @Override
    public Map<Integer, RatingWithTimestamp> getItemRatingsRated(Integer idItem) throws ItemNotFound {
        return dataset.getItemRatingsRated(idItem);
    }

    @Override
    public Domain getRatingsDomain() {
        return dataset.getRatingsDomain();
    }

    @Override
    public Iterator<RatingWithTimestamp> iterator() {
        return dataset.iterator();
    }

    @Override
    public void setSeedValue(long seedValue) {
        setParameterValue(SEED, seedValue);
    }

    @Override
    public long getSeedValue() {
        return ((Number) getParameterValue(SEED)).longValue();
    }

    private void init() {
        addParameter(SEED);

        addParammeterListener(new ParameterListener() {
            private long valorAnterior = (Long) getParameterValue(SEED);

            @Override
            public void parameterChanged() {
                long newValue = (Long) getParameterValue(SEED);
                if (valorAnterior != newValue) {
                    Global.showWarning("Reset " + getName() + " to seed = " + newValue + "\n");
                    valorAnterior = newValue;
                    setSeedValue(newValue);
                }
            }
        });
    }

    @Override
    public double getMeanRatingItem(int idItem) throws ItemNotFound {
        return dataset.getMeanRatingItem(idItem);
    }

    @Override
    public double getMeanRatingUser(int idUser) throws UserNotFound {
        return dataset.getMeanRatingUser(idUser);
    }

    @Override
    public int getNumRatings() {
        return dataset.getNumRatings();
    }

    @Override
    public int sizeOfUserRatings(int idUser) throws UserNotFound {
        return dataset.sizeOfUserRatings(idUser);
    }

    @Override
    public int sizeOfItemRatings(int idItem) throws ItemNotFound {
        return dataset.sizeOfItemRatings(idItem);
    }

    @Override
    public boolean isRatedUser(int idUser) throws UserNotFound {
        return dataset.isRatedUser(idUser);
    }

    @Override
    public boolean isRatedItem(int idItem) throws ItemNotFound {
        return dataset.isRatedItem(idItem);
    }
    private double meanRating = Double.NaN;

    @Override
    public double getMeanRating() {
        if (Double.isNaN(meanRating)) {
            meanRating = RatingsDatasetAdapter.getMeanRating(this);
        }
        return meanRating;
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.RATINGS_DATASET;
    }
}
