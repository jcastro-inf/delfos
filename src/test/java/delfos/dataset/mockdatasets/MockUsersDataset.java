package delfos.dataset.mockdatasets;

import delfos.common.exceptions.dataset.entity.EntityAlreadyExists;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureGenerator;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 *
 * @author Jorge
 */
public class MockUsersDataset implements UsersDataset {

    private final Map<Long, User> users = new TreeMap<>();
    private final FeatureGenerator featureGenerator = new FeatureGenerator();

    private void checkItemNotExists(int idUser) {
        if (users.containsKey(idUser)) {
            throw new IllegalArgumentException("The item " + idUser + " already exists.");
        }
    }

    private void checkItem(int idUser) throws UserNotFound {
        if (!users.containsKey(idUser)) {
            throw new UserNotFound(idUser);
        }
    }

    @Override
    public User getUser(long idUser) throws UserNotFound {
        try {
            return get(idUser);
        } catch (EntityNotFound ex) {
            throw new UserNotFound(idUser, ex);
        }

    }

    @Override
    public boolean add(User entity) throws EntityAlreadyExists {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public User get(long idUser) throws EntityNotFound {
        if (users.containsKey(idUser)) {
            return users.get(idUser);
        } else {
            throw new UserNotFound(idUser);
        }
    }

    @Override
    public Feature[] getFeatures() {
        return featureGenerator.getSortedFeatures().toArray(new Feature[0]);
    }

    @Override
    public Set<Object> getAllFeatureValues(Feature feature) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getMinValue(Feature feature) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getMaxValue(Feature feature) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Feature searchFeature(String featureName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Feature searchFeatureByExtendedName(String extendedName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Feature, Object> parseEntityFeatures(Map<String, String> features) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int size() {
        return users.size();
    }

    @Override
    public Collection<Long> allIDs() {
        return new TreeSet<>(users.keySet());
    }

    @Override
    public Map<Feature, Object> parseEntityFeaturesAndAddToExisting(long idEntity, Map<String, String> features) throws EntityNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterator<User> iterator() {
        return new ArrayList<>(users.values()).iterator();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Not allowed to delete entities.");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Collection<User> getAllItems() {
        return allIDs().stream().map((idUser) -> get(idUser)).collect(Collectors.toList());
    }

    @Override
    public Object[] toArray() {
        return getAllItems().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return getAllItems().toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.stream().allMatch(((element) -> this.contains(element)));
    }

    @Override
    public boolean addAll(Collection<? extends User> entitys) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
