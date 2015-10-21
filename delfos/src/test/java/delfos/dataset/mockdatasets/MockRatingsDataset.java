package delfos.dataset.mockdatasets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.dataset.basic.rating.domain.Domain;

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
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * @version 10-Octubre-2013
 *
 */
public class MockRatingsDataset implements RatingsDataset<Rating> {

    private final Map<Integer, Map<Integer, Integer>> ratings;
    private final Set<Integer> users;
    private final Set<Integer> items;

    public MockRatingsDataset() {
        ratings = new TreeMap<>();

        //User1 ratings
        ratings.put(1, new TreeMap<>());
        ratings.get(1).put(1, 1);
        ratings.get(1).put(3, 5);
        ratings.get(1).put(5, 5);
        ratings.get(1).put(6, 4);
        ratings.get(1).put(7, 4);

        //User2 ratings
        ratings.put(2, new TreeMap<>());
        ratings.get(2).put(2, 4);
        ratings.get(2).put(4, 3);
        ratings.get(2).put(8, 5);
        ratings.get(2).put(9, 5);

        //User3 ratings
        ratings.put(3, new TreeMap<>());
        ratings.get(3).put(3, 4);
        ratings.get(3).put(4, 4);
        ratings.get(3).put(7, 5);
        ratings.get(3).put(8, 5);

        //User4 ratings
        ratings.put(4, new TreeMap<>());
        ratings.get(4).put(1, 4);
        ratings.get(4).put(2, 4);
        ratings.get(4).put(3, 1);
        ratings.get(4).put(9, 5);

        //User5 ratings
        ratings.put(5, new TreeMap<>());
        ratings.get(5).put(10, 5);
        ratings.get(5).put(11, 3);
        ratings.get(5).put(12, 4);

        users = new TreeSet<>(ratings.keySet());

        items = new TreeSet<>();
        for (int idItem = 1; idItem <= 12; idItem++) {
            items.add(idItem);
        }

    }

    private void checkUser(int idUser) throws UserNotFound {
        if (!users.contains(idUser)) {
            throw new UserNotFound(idUser);
        }
    }

    private void checkItem(int idItem) throws ItemNotFound {
        if (!items.contains(idItem)) {
            throw new ItemNotFound(idItem);
        }
    }

    @Override
    public Rating getRating(int idUser, int idItem) throws UserNotFound, ItemNotFound {
        checkUser(idUser);
        checkItem(idItem);

        if (ratings.containsKey(idUser)) {
            if (ratings.get(idUser).containsKey(idItem)) {
                Integer ratingValue = ratings.get(idUser).get(idItem);
                return new Rating(idUser, idItem, ratingValue);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public Set<Integer> allUsers() {
        return new TreeSet<>(users);
    }

    @Override
    public Set<Integer> allRatedItems() {
        return new TreeSet<>(items);
    }

    @Override
    public Set<Integer> getUserRated(Integer idUser) throws UserNotFound {
        return getUserRatingsRated(idUser).keySet();
    }

    @Override
    public Set<Integer> getItemRated(Integer idItem) throws ItemNotFound {
        return getItemRatingsRated(idItem).keySet();
    }

    @Override
    public Map<Integer, Rating> getUserRatingsRated(Integer idUser) throws UserNotFound {
        checkUser(idUser);

        Map<Integer, Rating> ret = new TreeMap<>();
        ratings.get(idUser).entrySet().stream().forEach((entry) -> {
            int idItem = entry.getKey();
            int ratingValue = entry.getValue();
            ret.put(idItem, new Rating(idUser, idItem, ratingValue));
        });

        return ret;
    }

    @Override
    public Map<Integer, Rating> getItemRatingsRated(Integer idItem) throws ItemNotFound {
        checkItem(idItem);

        Map<Integer, Rating> ret = new TreeMap<>();
        ratings.keySet().stream().filter((idUser) -> (ratings.get(idUser).containsKey(idItem))).forEach((idUser) -> {
            ret.put(idUser, new Rating(idUser, idItem, idUser));
        });

        return ret;
    }

    @Override
    public float getMeanRatingItem(int idItem) throws ItemNotFound {

        checkItem(idItem);
        Collection<Rating> itemRatings = getItemRatingsRated(idItem).values();

        float mean = 0;
        mean = itemRatings.stream()
                .map((ratingValue) -> ratingValue.getRatingValue().floatValue())
                .reduce(mean, (accumulator, _item) -> accumulator + _item);

        mean = mean / itemRatings.size();
        return mean;
    }

    @Override
    public float getMeanRatingUser(int idUser) throws UserNotFound {
        checkUser(idUser);

        Collection<Rating> userRatings = getUserRatingsRated(idUser).values();

        float mean = 0;
        mean = userRatings.stream()
                .map((ratingValue) -> ratingValue.getRatingValue().floatValue())
                .reduce(mean, (accumulator, _item) -> accumulator + _item);

        mean = mean / userRatings.size();
        return mean;
    }

    @Override
    public Domain getRatingsDomain() {
        return new DecimalDomain(1, 5);
    }

    @Override
    public int getNumRatings() {
        int numRatings = 0;
        numRatings = users.stream()
                .map((idUser) -> ratings.get(idUser).size())
                .reduce(numRatings, Integer::sum);

        return numRatings;
    }

    @Override
    public int sizeOfUserRatings(int idUser) throws UserNotFound {
        checkUser(idUser);

        return getUserRated(idUser).size();
    }

    @Override
    public int sizeOfItemRatings(int idItem) throws ItemNotFound {

        checkItem(idItem);

        return getItemRated(idItem).size();
    }

    @Override
    public boolean isRatedUser(int idUser) throws UserNotFound {
        checkUser(idUser);
        return !ratings.get(idUser).isEmpty();
    }

    @Override
    public boolean isRatedItem(int idItem) throws ItemNotFound {
        checkItem(idItem);

        return !getItemRated(idItem).isEmpty();
    }

    @Override
    public float getMeanRating() {

        float mean = 0;
        int count = 0;

        for (Rating rating : this) {
            mean += rating.getRatingValue().floatValue();
            count++;
        }

        return mean / count;
    }

    @Override
    public Iterator<Rating> iterator() {

        List<Rating> _ratings = new ArrayList<>(getNumRatings());

        ratings.keySet().stream().forEach((idUser) -> {
            ratings.get(idUser).keySet().stream().forEach((idItem) -> {
                Integer ratingValue = ratings.get(idUser).get(idItem);
                _ratings.add(new Rating(idUser, idItem, ratingValue));
            });
        });

        Collections.sort(_ratings);
        return _ratings.iterator();
    }
}
