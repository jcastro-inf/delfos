package delfos.dataset.mockdatasets;

import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.dataset.basic.rating.domain.Domain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Clase que implementa un Mock de un dataset de valoraciones. Valoraciones que
 * contiene:
 * <p>
 * <p>
 * ...|..I1.|..I2.|..I3.|..I4.|..I5.|..I6.|..I7.|..I8.|..I9.|.I10.|.I11.|.I12.|
 * <p>
 * U1.|..1..|.....|..5..|.....|..5..|..4..|..4..|.....|.....|.....|.....|.....|
 * <p>
 * U2.|.....|..4..|.....|..3..|.....|.....|.....|..5..|..5..|.....|.....|.....|
 * <p>
 * U3.|.....|.....|..4..|..4..|.....|.....|..4..|..5..|.....|.....|.....|.....|
 * <p>
 * U4.|..4..|..4..|..1..|.....|.....|.....|.....|.....|..5..|.....|.....|.....|
 * <p>
 * U5.|.....|.....|.....|.....|.....|.....|.....|.....|.....|..5..|..3..|..4..|
 * <p>
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 10-Octubre-2013
 *
 */
public class MockRatingsDataset implements RatingsDataset<Rating> {

    private final Map<Long, Map<Long, Long>> ratings;
    private final Set<Long> users;
    private final Set<Long> items;

    public MockRatingsDataset() {
        ratings = new TreeMap<>();

        //User1 ratings
        ratings.put(1l, new TreeMap<>());
        ratings.get(1l).put(1l, 1l);
        ratings.get(1l).put(3l, 5l);
        ratings.get(1l).put(5l, 5l);
        ratings.get(1l).put(6l, 4l);
        ratings.get(1l).put(7l, 4l);

        //User2 ratings
        ratings.put(2l, new TreeMap<>());
        ratings.get(2l).put(2l, 4l);
        ratings.get(2l).put(4l, 3l);
        ratings.get(2l).put(8l, 5l);
        ratings.get(2l).put(9l, 5l);

        //User3 ratings
        ratings.put(3l, new TreeMap<>());
        ratings.get(3l).put(3l, 4l);
        ratings.get(3l).put(4l, 4l);
        ratings.get(3l).put(7l, 5l);
        ratings.get(3l).put(8l, 5l);

        //User4 ratings
        ratings.put(4l, new TreeMap<>());
        ratings.get(4l).put(1l, 4l);
        ratings.get(4l).put(2l, 4l);
        ratings.get(4l).put(3l, 1l);
        ratings.get(4l).put(9l, 5l);

        //User5 ratings
        ratings.put(5l, new TreeMap<>());
        ratings.get(5l).put(10l, 5l);
        ratings.get(5l).put(11l, 3l);
        ratings.get(5l).put(12l, 4l);

        users = new TreeSet<>(ratings.keySet());

        items = new TreeSet<>();
        for (long idItem = 1; idItem <= 12; idItem++) {
            items.add(idItem);
        }

    }

    private void checkUser(long idUser) throws UserNotFound {
        if (!users.contains(idUser)) {
            throw new UserNotFound(idUser);
        }
    }

    private void checkItem(long idItem) throws ItemNotFound {
        if (!items.contains(idItem)) {
            throw new ItemNotFound(idItem);
        }
    }

    @Override
    public Rating getRating(long idUser, long idItem) throws UserNotFound, ItemNotFound {
        checkUser(idUser);
        checkItem(idItem);

        if (ratings.containsKey(idUser)) {
            if (ratings.get(idUser).containsKey(idItem)) {
                Long ratingValue = ratings.get(idUser).get(idItem);
                return new Rating(idUser, idItem, ratingValue);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public Set<Long> allUsers() {
        return new TreeSet<>(users);
    }

    @Override
    public Set<Long> allRatedItems() {
        return new TreeSet<>(items);
    }

    @Override
    public Set<Long> getUserRated(long idUser) throws UserNotFound {
        return getUserRatingsRated(idUser).keySet();
    }

    @Override
    public Set<Long> getItemRated(long idItem) throws ItemNotFound {
        return getItemRatingsRated(idItem).keySet();
    }

    @Override
    public Map<Long, Rating> getUserRatingsRated(long idUser) throws UserNotFound {
        checkUser(idUser);

        Map<Long, Rating> ret = new TreeMap<>();
        ratings.get(idUser).entrySet().stream().forEach((entry) -> {
            long idItem = entry.getKey();
            long ratingValue = entry.getValue();
            ret.put(idItem, new Rating(idUser, idItem, ratingValue));
        });

        return ret;
    }

    @Override
    public Map<Long, Rating> getItemRatingsRated(long idItem) throws ItemNotFound {
        checkItem(idItem);

        Map<Long, Rating> ret = new TreeMap<>();
        ratings.keySet().stream().filter((idUser) -> (ratings.get(idUser).containsKey(idItem))).forEach((idUser) -> {
            ret.put(idUser, new Rating(idUser, idItem, idUser));
        });

        return ret;
    }

    @Override
    public double getMeanRatingItem(long idItem) throws ItemNotFound {

        checkItem(idItem);
        Collection<Rating> itemRatings = getItemRatingsRated(idItem).values();

        double mean = 0;
        mean = itemRatings.stream()
                .mapToDouble(rating -> rating.getRatingValue().doubleValue())
                .average()
                .getAsDouble();

        mean = mean / itemRatings.size();
        return (double) mean;
    }

    @Override
    public double getMeanRatingUser(long idUser) throws UserNotFound {
        checkUser(idUser);

        Collection<Rating> userRatings = getUserRatingsRated(idUser).values();

        double mean = 0;
        mean = userRatings.stream()
                .mapToDouble(rating -> rating.getRatingValue().doubleValue())
                .average()
                .getAsDouble();

        mean = mean / userRatings.size();
        return (double) mean;
    }

    @Override
    public Domain getRatingsDomain() {
        return new DecimalDomain(1, 5);
    }

    @Override
    public long getNumRatings() {
        int numRatings = 0;
        numRatings = users.stream()
                .mapToInt((idUser) -> ratings.get(idUser).size())
                .sum();

        return numRatings;
    }

    @Override
    public long sizeOfUserRatings(long idUser) throws UserNotFound {
        checkUser(idUser);
        return getUserRated(idUser).size();
    }

    @Override
    public long sizeOfItemRatings(long idItem) throws ItemNotFound {
        checkItem(idItem);
        return getItemRated(idItem).size();
    }

    @Override
    public boolean isRatedUser(long idUser) throws UserNotFound {
        checkUser(idUser);
        return !ratings.get(idUser).isEmpty();
    }

    @Override
    public boolean isRatedItem(long idItem) throws ItemNotFound {
        checkItem(idItem);
        return !getItemRated(idItem).isEmpty();
    }

    @Override
    public double getMeanRating() {
        double mean = 0;
        int count = 0;

        for (Rating rating : this) {
            mean += rating.getRatingValue().doubleValue();
            count++;
        }

        return mean / count;
    }

    @Override
    public Iterator<Rating> iterator() {
        List<Rating> _ratings = new ArrayList<Rating>((int) getNumRatings());

        ratings.keySet().stream().forEach((idUser) -> {
            ratings.get(idUser).keySet().stream().forEach((idItem) -> {
                Long ratingValue = ratings.get(idUser).get(idItem);
                _ratings.add(new Rating(idUser, idItem, ratingValue));
            });
        });

        Collections.sort(_ratings);
        return _ratings.iterator();
    }
}
